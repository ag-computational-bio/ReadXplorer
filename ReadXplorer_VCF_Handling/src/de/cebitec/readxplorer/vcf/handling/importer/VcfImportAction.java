
package de.cebitec.readxplorer.vcf.handling.importer;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.api.cookies.LoginCookie;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.datavisualisation.basePanel.BasePanel;
import de.cebitec.readxplorer.ui.datavisualisation.basePanel.BasePanelFactory;
import de.cebitec.readxplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import de.cebitec.readxplorer.vcf.handling.visualization.Snp_VcfResult;
import de.cebitec.readxplorer.vcf.handling.visualization.Snp_VcfResultPanel;
import de.cebitec.readxplorer.vcf.handling.visualization.Snp_VcfResultTopComponent;
import de.cebitec.readxplorer.vcf.handling.visualization.Snp_VcfViewer;
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
         id = "de.cebitec.readxplorer.vcf.handling.importer.VcfImportAction"
)
@ActionRegistration(
         iconBase = "de/cebitec/readxplorer/vcf/handling/importer/import.png",
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
    private Snp_VcfViewer snpVcfViewer;
    private Snp_VcfResultTopComponent vcfResultTopComp;
    private final Snp_VcfResultPanel resultPanel = new Snp_VcfResultPanel();
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

        // Saves required information for generating a Snp_VcfResult Object
        reference = (PersistentReference) wiz.getProperty( VcfImportWizardPanel.PROP_SELECTED_REF );
        combineTracks = false;

        // Opens VcfResultPanel
        if( vcfResultTopComp == null ) {
            vcfResultTopComp = (Snp_VcfResultTopComponent) WindowManager.getDefault().findTopComponent( "Snp_VcfResultTopComponent" );
        }
        vcfResultTopComp.open();

        Snp_VcfResult vcfResult = new Snp_VcfResult( variantCList, trackMap, reference, combineTracks );
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

        snpVcfViewer = new Snp_VcfViewer( viewController.getBoundsManager(), basePanel, reference );
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
