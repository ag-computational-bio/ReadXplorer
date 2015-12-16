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

package de.cebitec.readxplorer.tools.coverageanalysis.featurecoverageanalysis;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.dialogmenus.OpenTracksVisualPanel;
import de.cebitec.readxplorer.ui.dialogmenus.OpenTracksWizardPanel;
import de.cebitec.readxplorer.ui.dialogmenus.SelectFeatureTypeWizardPanel;
import de.cebitec.readxplorer.ui.dialogmenus.SelectReadClassWizardPanel;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.cebitec.readxplorer.tools.coverageanalysis.featurecoverageanalysis.Bundle.MSG_AnalysesWorker_progress_name;


/**
 * Action for opening the feature coverage analysis. It opens the wizard and
 * runs the analysis after successfully finishing the wizard.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID(
         category = "Tools",
         id = "de.cebitec.readxplorer.tools.coverageanalysis.featurecoverageanalysis.OpenCoveredFeaturesAction" )
@ActionRegistration(
         iconBase = "de/cebitec/readxplorer/tools/coverageanalysis/featurecoverageanalysis/coveredFeatures.png",
         displayName = "#CTL_OpenCoveredFeaturesAction" )
@ActionReferences( {
    @ActionReference( path = "Menu/Tools", position = 146 ),
    @ActionReference( path = "Toolbars/Tools", position = 231 )
} )
@Messages( "CTL_OpenCoveredFeaturesAction=Feature Coverage Analysis" )
public final class OpenCoveredFeaturesAction implements ActionListener,
                                                        DataVisualisationI {

    private static final Logger LOG = LoggerFactory.getLogger( OpenCoveredFeaturesAction.class.getName() );
    private static final String PROP_WIZARD_NAME = "FeatureCoverageWiz";

    private final ReferenceViewer context;
    private final PersistentReference reference;
    private List<PersistentTrack> tracks;
    private CoveredFeaturesAnalysisTopComponent coveredAnnoAnalysisTopComp;
    private Map<Integer, AnalysisCoveredFeatures> trackToAnalysisMap;
    private int finishedCovAnalyses = 0;
    private ResultPanelCoveredFeatures coveredFeaturesResultPanel;
    ParameterSetCoveredFeatures parameters;
    private boolean combineTracks;
    private Map<Integer, PersistentTrack> trackMap;
    private OpenTracksWizardPanel openTracksWizPanel;
    private SelectReadClassWizardPanel readClassWizPanel;
    private SelectFeatureTypeWizardPanel featTypeWizPanel;


    /**
     * Action for opening the feature coverage analysis. It opens the wizard and
     * runs the analysis after successfully finishing the wizard.
     */
    public OpenCoveredFeaturesAction( ReferenceViewer context ) {
        this.context = context;
        this.reference = this.context.getReference();
        this.trackToAnalysisMap = new HashMap<>();
    }


    /**
     * Carries out the logic behind the feature coverage analysis action. This
     * means, it opens a configuration wizard and starts the analysis after
     * successfully finishing the wizard.
     * <p>
     * @param ev the event, which is currently not used
     */
    @Override
    public void actionPerformed( ActionEvent ev ) {
        this.runWizarAndCoveredAnnoAnalsysis();
    }


    /**
     * Runs the wizard and starts the feature coverage anaylsis, if the wizard
     * finished successfully.
     */
    private void runWizarAndCoveredAnnoAnalsysis() {

        @SuppressWarnings( "unchecked" )
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        this.openTracksWizPanel = new OpenTracksWizardPanel( PROP_WIZARD_NAME, reference.getId() );
        this.readClassWizPanel = new SelectReadClassWizardPanel( PROP_WIZARD_NAME, true );
        this.featTypeWizPanel = new SelectFeatureTypeWizardPanel( PROP_WIZARD_NAME, false );
        panels.add( openTracksWizPanel );
        panels.add( new CoveredFeaturesWizardPanel() );
        panels.add( readClassWizPanel );
        panels.add( featTypeWizPanel );
        WizardDescriptor wiz = new WizardDescriptor( new WizardDescriptor.ArrayIterator<>( VisualisationUtils.getWizardPanels( panels ) ) );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( NbBundle.getMessage( OpenCoveredFeaturesAction.class, "TTL_CoveredFeaturesWizardTitle" ) );

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
        if( !cancelled ) {
            OpenTracksVisualPanel tracksVisPanel = openTracksWizPanel.getComponent();
            if( !tracksVisPanel.getAllMarkedNodes().isEmpty() ) {
                this.tracks = tracksVisPanel.getSelectedTracks();
                this.trackMap = ProjectConnector.getTrackMap( tracks );
                this.trackToAnalysisMap = new HashMap<>();
                this.finishedCovAnalyses = 0;
                this.coveredFeaturesResultPanel = null;

                if( this.coveredAnnoAnalysisTopComp == null ) {
                    this.coveredAnnoAnalysisTopComp = (CoveredFeaturesAnalysisTopComponent) WindowManager.getDefault().findTopComponent( "CoveredFeaturesAnalysisTopComponent" );
                }
                this.coveredAnnoAnalysisTopComp.open();
                this.startCoveredFeaturesDetection( wiz );

            } else {
                String msg = NbBundle.getMessage( OpenCoveredFeaturesAction.class, "CTL_OpenCoveredFeaturesDetectionInfo",
                                                  "No track selected. To start a feature coverage analysis at least one track has to be selected." );
                String title = NbBundle.getMessage( OpenCoveredFeaturesAction.class, "CTL_OpenCoveredFeaturesDetectionInfoTitle", "Info" );
                JOptionPane.showMessageDialog( this.context, msg, title, JOptionPane.INFORMATION_MESSAGE );
            }
        }
    }


    /**
     * Starts the feature coverage analysis.
     * <p>
     * @param wiz the wizard containing the feature coverage analysis parameters
     */
    private void startCoveredFeaturesDetection( WizardDescriptor wiz ) {
        //get parameters
        boolean getCoveredFeatures = (boolean) wiz.getProperty( CoveredFeaturesWizardPanel.PROP_GET_COVERED_FEATURES );
        int minCoveredPercent = (int) wiz.getProperty( CoveredFeaturesWizardPanel.PROP_MIN_COVERED_PERCENT );
        int minCoverageCount = (int) wiz.getProperty( CoveredFeaturesWizardPanel.PROP_MIN_COVERAGE_COUNT );
        ParametersReadClasses readClassesParams = (ParametersReadClasses) wiz.getProperty( readClassWizPanel.getPropReadClassParams() );
        this.combineTracks = (boolean) wiz.getProperty( openTracksWizPanel.getPropCombineTracks() );
        @SuppressWarnings( "unchecked" )
        Set<FeatureType> selFeatureTypes = (Set<FeatureType>) wiz.getProperty( featTypeWizPanel.getPropSelectedFeatTypes() );
        parameters = new ParameterSetCoveredFeatures( minCoveredPercent, minCoverageCount,
                                                      getCoveredFeatures, readClassesParams, selFeatureTypes );

        if( !combineTracks ) {
            for( PersistentTrack track : this.tracks ) {
                try {
                    TrackConnector connector = (new SaveFileFetcherForGUI()).getTrackConnector( track );
                    //every track has its own analysis handlers
                    this.createAnalysis( connector, readClassesParams );

                } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                    SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                } catch( DatabaseException e ) {
                    ErrorHelper.getHandler().handle( e );
                }
            }
        } else {
            try {
                TrackConnector connector = (new SaveFileFetcherForGUI()).getTrackConnector( tracks, combineTracks );
                this.createAnalysis( connector, readClassesParams );  //every track has its own analysis handlers

            } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                SaveFileFetcherForGUI.showPathSelectionErrorMsg();
            } catch( DatabaseException e ) {
                ErrorHelper.getHandler().handle( e );
            }
        }
    }


    /**
     * Creates the analysis for a TrackConnector.
     * <p>
     * @param connector         the connector
     * @param readClassesParams the read class parameters
     */
    @Messages( "MSG_AnalysesWorker.progress.name=Running Feature Coverage Analysis..." )
    private void createAnalysis( TrackConnector connector, ParametersReadClasses readClassesParams ) {

        AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler( this, //every track has its own analysis handlers
                                                                              MSG_AnalysesWorker_progress_name(),
                                                                              readClassesParams );
        AnalysisCoveredFeatures analysisCoveredFeatures = new AnalysisCoveredFeatures( connector, parameters );
        covAnalysisHandler.registerObserver( analysisCoveredFeatures );
        covAnalysisHandler.setCoverageNeeded( true );

        trackToAnalysisMap.put( connector.getTrackID(), analysisCoveredFeatures );
        covAnalysisHandler.startAnalysis();
    }


    /**
     * Shows the results in the corresponding top component.
     * <p>
     * @param dataTypeObject the pair describing the result data. It has to
     *                       consist of the track id as the first element and
     *                       the data type string as the second element.
     */
    @Override
    public void showData( Object dataTypeObject ) {
        try {
            @SuppressWarnings( "unchecked" )
            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) dataTypeObject;
            int trackId = dataTypePair.getFirst();
            String dataType = dataTypePair.getSecond();

            if( dataType.equals( AnalysesHandler.DATA_TYPE_COVERAGE ) ) {

                ++finishedCovAnalyses;

                AnalysisCoveredFeatures analysisCoveredFeatures = trackToAnalysisMap.get( trackId );
                final CoveredFeatureResult result = new CoveredFeatureResult( analysisCoveredFeatures.getResults(),
                                                                              trackMap, reference, combineTracks, 1, 0 );
                result.setParameters( parameters );
                Map<String, Integer> statsMap = new HashMap<>();
                statsMap.put( ResultPanelCoveredFeatures.FEATURES_TOTAL, analysisCoveredFeatures.getNoGenomeFeatures() );
                result.setStatsMap( statsMap );

                SwingUtilities.invokeLater( new Runnable() { //because it is not called from the swing dispatch thread
                    @Override
                    public void run() {

                        if( coveredFeaturesResultPanel == null ) {
                            coveredFeaturesResultPanel = new ResultPanelCoveredFeatures( context.getBoundsInformationManager() );
                        }
                        coveredFeaturesResultPanel.addResult( result );

                        if( finishedCovAnalyses >= tracks.size() || combineTracks ) {

                            //get track name(s) for tab descriptions
                            String trackNames = "";
                            if( tracks != null && !tracks.isEmpty() ) {
                                for( PersistentTrack track : tracks ) {
                                    trackNames = trackNames.concat( track.getDescription() ).concat( " and " );
                                }
                                trackNames = trackNames.substring( 0, trackNames.length() - 5 );
                                if( trackNames.length() > 120 ) {
                                    trackNames = trackNames.substring( 0, 120 ).concat( "..." );
                                }
                            }

                            String title;
                            if( parameters.isGetCoveredFeatures() ) {
                                title = "Detected covered features for ";
                            } else {
                                title = "Detected uncovered features for ";
                            }
                            String panelName = title + trackNames + " (" + coveredFeaturesResultPanel.getDataSize() + " hits)";
                            coveredAnnoAnalysisTopComp.openAnalysisTab( panelName, coveredFeaturesResultPanel );
                        }
                    }


                } );
            }

        } catch( ClassCastException e ) {
            LOG.error( "Unknown data received in " + this.getClass().getName() );
        }
    }


}
