/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readxplorer.vcfhandling.importer;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.datavisualisation.basepanel.BasePanel;
import de.cebitec.readxplorer.ui.datavisualisation.basepanel.BasePanelFactory;
import de.cebitec.readxplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import de.cebitec.readxplorer.vcfhandling.visualization.SnpVcfResult;
import de.cebitec.readxplorer.vcfhandling.visualization.SnpVcfResultPanel;
import de.cebitec.readxplorer.vcfhandling.visualization.SnpVcfResultTopComponent;
import de.cebitec.readxplorer.vcfhandling.visualization.SnpVcfViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingWorker;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


@ActionID(
         category = "File",
         id = "de.cebitec.readxplorer.vcfhandling.importer.VcfImportAction"
)
@ActionRegistration(
         iconBase = "de/cebitec/readxplorer/vcfhandling/importer/import.png",
         displayName = "#CTL_VcfImportAction"
)
@ActionReference( path = "Menu/File", position = 1487, separatorAfter = 1493 )
@Messages( "CTL_VcfImportAction=Import VCF file" )

/**
 * @author marend, vetz
 */
public final class VcfImportAction implements ActionListener {


//    private final LoginCookie context;
    private List<VariantContext> variantCList;

    private final Map<Integer, PersistentTrack> trackMap = new HashMap<>( 24 );
    private PersistentReference reference;
    private boolean combineTracks;

    private AppPanelTopComponent appPanelTopComp;
    private SnpVcfViewer snpVcfViewer;
    private SnpVcfResultTopComponent vcfResultTopComp;
    private final SnpVcfResultPanel resultPanel = new SnpVcfResultPanel();
    private WizardDescriptor wiz;

    //private WizardDescriptor.Panel<WizardDescriptor>[] panel;
    //private VcfImportVisualPanel visualPanel = new VcfImportVisualPanel();

    public VcfImportAction( LoginCookie context ) {
//        this.context = context;
    }


    /**
     * Reads in the given VCF-file.
     * The data of the VCF-file is saved in a list
     * of VariantContexts, then the dashboard, SNP-Viewer, Reference-Viewer and
     * Result table are opened.
     * <p>
     * @param e
     */
    @Messages( { "TTL_ImportVcfWizardTitle=VCF import wizard" } )
    @Override
    public void actionPerformed( ActionEvent e ) {
        if( CentralLookup.getDefault().lookup( SwingWorker.class ) != null ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message( NbBundle.getMessage( VcfImportAction.class, "MSG_BackgroundActivity" ), NotifyDescriptor.WARNING_MESSAGE );
            DialogDisplayer.getDefault().notify( nd );
            return;
        }

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>( 3 );
        panels.add( new VcfImportWizardPanel() );
        wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( Bundle.TTL_ImportVcfWizardTitle() );
//        Dialog dialog = DialogDisplayer.getDefault().createDialog(wiz);
//        dialog.setVisible(true);
//        dialog.toFront();

        if( DialogDisplayer.getDefault().notify( wiz ) == WizardDescriptor.FINISH_OPTION ) {

            File vcfFile = (File) wiz.getProperty( VcfImportWizardPanel.PROP_SELECTED_FILE );
            VcfParser vcfP = new VcfParser( vcfFile );
            variantCList = vcfP.getVariantContextList();

            openResultWindow();
            openView( variantCList );

        }
    }


    /**
     * Opens the Result table.
     */
    private void openResultWindow() {

        // Saves required information for generating a SnpVcfResult Object
        reference = (PersistentReference) wiz.getProperty( VcfImportWizardPanel.PROP_SELECTED_REF );
        combineTracks = false;

        // Opens VcfResultPanel
        if( vcfResultTopComp == null ) {
            vcfResultTopComp = (SnpVcfResultTopComponent) WindowManager.getDefault().findTopComponent( "Snp_VcfResultTopComponent" );
        }
        vcfResultTopComp.open();

        SnpVcfResult vcfResult = new SnpVcfResult( variantCList, trackMap, reference, combineTracks );
        resultPanel.addResult( vcfResult );
        vcfResultTopComp.openAnalysisTab( "Result Panel", resultPanel );
    }


    /**
     * Opens the SNP-Viewer.
     * <p>
     * @param variantList
     */
    private void openView( List<VariantContext> variantList ) {

        // Saves required information for generating a VcfViewer-Object
        ViewController viewController = this.checkAndOpenRefViewer( reference );
        resultPanel.setBoundsInfoManager( viewController.getBoundsManager() );
        BasePanelFactory basePanelFac = viewController.getBasePanelFac();
        BasePanel basePanel = basePanelFac.getGenericBasePanel( false, false, false, null );
        viewController.addMousePositionListener( basePanel );


        // Opens VcfViewer
        if( appPanelTopComp == null ) {
            Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
            for( TopComponent topComp : topComps ) {
                if( topComp instanceof AppPanelTopComponent ) {
                    AppPanelTopComponent appTopComp = (AppPanelTopComponent) topComp;
                    if( appTopComp.getReferenceViewer().getReference().getId() == reference.getId() ) {
                        appPanelTopComp = appTopComp;
                    }
                }
            }
        }

        snpVcfViewer = new SnpVcfViewer( viewController.getBoundsManager(), basePanel, reference );
        snpVcfViewer.setVariants( variantList );
        basePanel.setViewer( snpVcfViewer );
        appPanelTopComp.showBasePanel( basePanel );

    }


    /**
     * Opens the dashboard and Reference-Viewer.
     * <p>
     * @param ref
     *            <p>
     * @return
     */
    private ViewController checkAndOpenRefViewer( PersistentReference ref ) {
        ViewController viewController = null;

        @SuppressWarnings( "unchecked" )
        Collection<ViewController> viewControllers = (Collection<ViewController>) CentralLookup.getDefault().lookupAll( ViewController.class );
        boolean alreadyOpen = false;
        for( ViewController tmpVCon : viewControllers ) {
            if( tmpVCon.getCurrentRefGen().equals( ref ) ) {
                alreadyOpen = true;
                viewController = tmpVCon;
                break;
            }
        }

        if( !alreadyOpen ) {
            //open reference genome now
            AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
            appPanelTopComponent.open();
            viewController = appPanelTopComponent.getLookup().lookup( ViewController.class );
            viewController.openGenome( ref );
            appPanelTopComponent.setName( viewController.getDisplayName() );
            appPanelTopComponent.requestActive();
        }
        return viewController;
    }


}
