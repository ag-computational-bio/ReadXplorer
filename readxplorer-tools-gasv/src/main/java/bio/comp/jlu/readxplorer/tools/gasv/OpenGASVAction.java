/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.tools.gasv;

import bio.comp.jlu.readxplorer.tools.gasv.wizard.BamToGASVWizardPanel;
import bio.comp.jlu.readxplorer.tools.gasv.wizard.GASVMainWizardPanel;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.dialogmenus.OpenTracksWizardPanel;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/**
 * Action for opening a genome rearrangement detection using the integrated
 * version of GASV. It opens a track list containing all tracks of the selected
 * reference and creates a new GASV configuration setup wizard, runs the
 * analysis and opens the result TopComponent.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID(
         category = "Tools",
         id = "bio.comp.jlu.readxplorer.tools.gasv.OpenGASVAction"
)
@ActionRegistration(
         iconBase = "bio/comp/jlu/readxplorer/tools/gasv/gasv.png",
         displayName = "#CTL_OpenGASVAction"
)
@ActionReferences( {
    @ActionReference( path = "Menu/Tools", position = 225 ),
    @ActionReference( path = "Toolbars/Tools", position = 150 )
} )
@Messages( "CTL_OpenGASVAction=Run GASV" )
public final class OpenGASVAction implements ActionListener, DataVisualisationI {

    private static final Logger LOG = Logger.getLogger( OpenGASVAction.class.getName() );
    private static final String PROP_WIZARD_NAME = "GASV_Wizard";

    private final ReferenceViewer context;

    private final PersistentReference reference;
    private List<PersistentTrack> tracks;
    private Map<Integer, PersistentTrack> trackMap;
    private int finishedCovAnalyses = 0;

    private Map<Integer, GASVCaller> trackToAnalysisMap;
    private WizardDescriptor wiz;
    private OpenTracksWizardPanel openTracksPanel;


    /**
     * Action for opening a genome rearrangement detection using the integrated
     * version of GASV. It opens a track list containing all tracks of the
     * selected reference and creates a new GASV configuration setup wizard,
     * runs the analysis and opens the result TopComponent.
     * <p>
     * @param context The ReferenceViewer for which the analysis is carried out.
     */
    public OpenGASVAction( ReferenceViewer context ) {
        this.context = context;
        reference = context.getReference();
    }


    /**
     * Carries out the calculations for a complete genome rearrangement
     * detection using GASV + opening the corresponding TopComponent.
     * <p>
     * @param ev the event itself, which is not used currently
     */
    @Override
    public void actionPerformed( ActionEvent ev ) {

        trackToAnalysisMap = new HashMap<>();

        boolean cancelled = runWizard();
        if( !cancelled ) {
            runAnalysis();
        }
    }


    /**
     * Initializes the setup wizard for the genome rearrangement detection using
     * GASV.
     * <p>
     * @return <code>true</code>, if the wizard has been finished successfully,
     *         <code>false</code> otherwise
     */
    @Messages( { "TTL_GASVWizardTitle=GASV Genome Rearrangement Parameter Wizard" } )
    private boolean runWizard() {
        //Open track selection & option wizard.
        @SuppressWarnings( "unchecked" )
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        openTracksPanel = new OpenTracksWizardPanel( PROP_WIZARD_NAME, reference.getId() );

        panels.add( openTracksPanel );
        panels.add( new BamToGASVWizardPanel() );
        panels.add( new GASVMainWizardPanel() );

        wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );

        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( Bundle.TTL_GASVWizardTitle() );

        //action to perform after successfully finishing the wizard
        return DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
    }


    /**
     * Starts the genome rearrangement detection using GASV.
     */
    @Messages( { "TITLE_GASVTopComp=GASV Genome Rearrangements Window",
                 "HINT_GASVTopComp=This is a GASV Genome Rearrangements window" } )
    private void runAnalysis() {

        ParametersBamToGASV bamToGASVParams = (ParametersBamToGASV) wiz.getProperty( BamToGASVWizardPanel.PROP_BAM_TO_GASV_PARAMS );
        ParametersGASVMain gasvMainParams = (ParametersGASVMain) wiz.getProperty( GASVMainWizardPanel.PROP_GASV_MAIN_PARAMS );

        List<PersistentTrack> selectedTracks = openTracksPanel.getComponent().getSelectedTracks();
        if( !selectedTracks.isEmpty() ) {
            tracks = selectedTracks;
            trackMap = ProjectConnector.getTrackMap( tracks );

//            this.snpDetectionTopComp = (SnpDetectionTopComponent) WindowManager.getDefault().findTopComponent( "SnpDetectionTopComponent" );
//            this.snpDetectionTopComp.setName( Bundle.TITLE_SNPDetectionTopComp() );
//            this.snpDetectionTopComp.setToolTipText( Bundle.HINT_SNP_DetectionTopComp() );
//            this.snpDetectionTopComp.open();
//            this.startSNPDetection( wiz );

            //Afterwards run GASV with:
            for( PersistentTrack track : tracks ) {
                TrackConnector connector;
                try {
                    connector = (new SaveFileFetcherForGUI()).getTrackConnector( track );
                } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                    SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                    continue;
                }

                this.createAnalysis( connector, bamToGASVParams, gasvMainParams );
            }
        }
    }


    /**
     * Creates the actual analysis objects and runs them for a single track.
     * <p>
     * @param connector       The track connector for which the analysis shall
     *                        be run
     * @param bamToGASVParams BamToGASV parameter set to apply
     * @param gasvMainParams  GASVMain parameter set to apply
     */
    private void createAnalysis( TrackConnector connector, ParametersBamToGASV bamToGASVParams, ParametersGASVMain gasvMainParams ) {
        File trackFile = connector.getTrackFile();
        GASVCaller gasvCaller = new GASVCaller( reference, trackFile, bamToGASVParams, gasvMainParams );
        Thread thread = new Thread( gasvCaller );
        thread.start();
    }


    @Override
    public void showData( Object data ) {

    }


}
