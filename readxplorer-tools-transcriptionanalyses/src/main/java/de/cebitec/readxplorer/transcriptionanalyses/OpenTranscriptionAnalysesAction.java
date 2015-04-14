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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.DataVisualisationI;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.transcriptionanalyses.wizard.TranscriptionAnalysesWizardIterator;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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


/**
 * Action for opening a new transcription analyses frame. It opens a track list
 * containing all tracks of the selected reference and creates a new
 * transcription analyses frame when a track was chosen.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ActionID( category = "Tools",
           id = "de.cebitec.readxplorer.transcriptionanalyses.OpenTranscriptionAnalysesAction" )
@ActionRegistration( iconBase = "de/cebitec/readxplorer/transcriptionanalyses/transcriptionAnalyses.png",
                     displayName = "#CTL_OpenTranscriptionAnalysesAction" )
@ActionReferences( {
    @ActionReference( path = "Menu/Tools", position = 142, separatorAfter = 150 ),
    @ActionReference( path = "Toolbars/Tools", position = 187 )
} )
@Messages( "CTL_OpenTranscriptionAnalysesAction=Transcription Analyses" )
public final class OpenTranscriptionAnalysesAction implements ActionListener,
                                                              DataVisualisationI {

    private static final Logger LOG = Logger.getLogger( OpenTranscriptionAnalysesAction.class.getName() );

    private final TranscriptionAnalysesTopComponent transcAnalysesTopComp;
    private final ReferenceViewer refViewer;
    private final PersistentReference reference;
    private List<PersistentTrack> tracks;
    private int finishedCovAnalyses = 0;
    private int finishedMappingAnalyses = 0;
    private ParameterSetTSS parametersTss;
    private ParameterSetOperonDet parametersOperonDet;
    private ParameterSetNormalization parametersNormalization;
    private boolean combineTracks;
    private Map<Integer, PersistentTrack> trackMap;
    private final Map<Integer, AnalysisContainer> trackToAnalysisMap;
    private ResultPanelTranscriptionStart transcriptionStartResultPanel;
    private ResultPanelOperonDetection operonResultPanel;
    private ResultPanelNormalization normalizationResultPanel;

    private String readClassPropString;
    private String selNormFeatureTypesPropString;
    private String propStringNormFeatureStartOffset;
    private String propStringNormFeatureStopOffset;
    private String selOperonFeatureTypesPropString;
    private String combineTracksPropString;


    /**
     * Action for opening a new transcription analyses frame. It opens a track
     * list containing all tracks of the selected reference and creates a new
     * transcription analyses frame when a track was chosen.
     * <p>
     * @param context the context of the action: the reference viewer which is
     *                connected with this analysis
     */
    public OpenTranscriptionAnalysesAction( ReferenceViewer context ) {
        this.refViewer = context;
        this.reference = this.refViewer.getReference();
        this.transcAnalysesTopComp = (TranscriptionAnalysesTopComponent) WindowManager.getDefault().findTopComponent( "TranscriptionAnalysesTopComponent" );
        this.trackToAnalysisMap = new HashMap<>();
    }


    /**
     * Carries out the logic behind the transcription analyses action. This
     * means, it opens a configuration wizard and starts the analyses after
     * successfully finishing the wizard.
     * <p>
     * @param ev the event, which is currently not used
     */
    @Override
    public void actionPerformed( ActionEvent ev ) {
        this.runWizardAndTranscriptionAnalysis();
    }


    /**
     * Initializes the setup wizard for the transcription analyses.
     */
    private void runWizardAndTranscriptionAnalysis() {
        @SuppressWarnings( "unchecked" )
        TranscriptionAnalysesWizardIterator transWizardIterator = new TranscriptionAnalysesWizardIterator( reference.getId() );
        readClassPropString = transWizardIterator.getReadClassPropForWiz();
        selOperonFeatureTypesPropString = transWizardIterator.getPropSelectedOperonFeatTypes();
        selNormFeatureTypesPropString = transWizardIterator.getPropSelectedNormFeatTypes();
        propStringNormFeatureStartOffset = transWizardIterator.getPropNormFeatureStartOffset();
        propStringNormFeatureStopOffset = transWizardIterator.getPropNormFeatureStopOffset();
        combineTracksPropString = transWizardIterator.getCombineTracksPropForWiz();
        WizardDescriptor wiz = new WizardDescriptor( transWizardIterator );
        transWizardIterator.setWiz( wiz );
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat( new MessageFormat( "{0}" ) );
        wiz.setTitle( NbBundle.getMessage( OpenTranscriptionAnalysesAction.class, "TTL_TransAnalysesWizardTitle" ) );

        //action to perform after successfully finishing the wizard
        boolean cancelled = DialogDisplayer.getDefault().notify( wiz ) != WizardDescriptor.FINISH_OPTION;
        List<PersistentTrack> selectedTracks = transWizardIterator.getSelectedTracks();
        if( !cancelled && !selectedTracks.isEmpty() ) {
            tracks = selectedTracks;
            trackMap = ProjectConnector.getTrackMap( tracks );

            transcAnalysesTopComp.open();
            startTransciptionAnalyses( wiz );

        } else if( selectedTracks.isEmpty() && !cancelled ) {
            String msg = NbBundle.getMessage( OpenTranscriptionAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfo",
                                              "No track selected. To start a transcription analysis at least one track has to be selected." );
            String title = NbBundle.getMessage( OpenTranscriptionAnalysesAction.class, "CTL_OpenTranscriptionAnalysesInfoTitle", "Info" );
            JOptionPane.showMessageDialog( this.refViewer, msg, title, JOptionPane.INFORMATION_MESSAGE );
        }
    }


    /**
     * Starts the transcription analyses.
     * <p>
     * @param wiz the wizard containing the transcription analyses parameters
     */
    @SuppressWarnings( "unchecked" )
    private void startTransciptionAnalyses( WizardDescriptor wiz ) {
        boolean autoTssParamEstimation = false;
        int minTotalIncrease = 0;
        int minPercentIncrease = 0;
        int maxLowCovInitCount = 0;
        int minLowCovIncrease = 0;
        boolean performUnannotatedTranscriptDet = false;
        int minTranscriptExtensionCov = 0;
        int maxLeaderlessDistance = 0;
        int maxFeatureDistance = 0;
        boolean isAssociateTss = true;
        int associateTssWindow = 0;
        int minNumberReads = 0;
        int maxNumberReads = 0;
        boolean autoOperonParamEstimation = false;
        int minSpanningReads = 0;
        Set<FeatureType> selNormFeatureTypes = new HashSet<>();
        Set<FeatureType> selOperonFeatureTypes = new HashSet<>();
        int normFeatureStartOffset = 0;
        int normFeatureStopOffset = 0;

        //obtain all analysis parameters
        boolean performTSSAnalysis = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_TSS_ANALYSIS );
        boolean performOperonAnalysis = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_OPERON_ANALYSIS );
        boolean performNormAnalysis = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_NORM_ANALYSIS );

        ParametersReadClasses readClassParams = (ParametersReadClasses) wiz.getProperty( readClassPropString );
        this.combineTracks = (boolean) wiz.getProperty( combineTracksPropString );

        if( performTSSAnalysis ) { //set values depending on the selected analysis functions (avoiding null pointers)
            autoTssParamEstimation = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_AUTO_TSS_PARAMS );
            minTotalIncrease = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MIN_TOTAL_INCREASE );
            minPercentIncrease = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MIN_PERCENT_INCREASE );
            maxLowCovInitCount = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MAX_LOW_COV_INIT_COUNT );
            minLowCovIncrease = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MIN_LOW_COV_INC );
            performUnannotatedTranscriptDet = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_UNANNOTATED_TRANSCRIPT_DET );
            minTranscriptExtensionCov = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MIN_TRANSCRIPT_EXTENSION_COV );
            maxLeaderlessDistance = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MAX_LEADERLESS_DISTANCE );
            maxFeatureDistance = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MAX_FEATURE_DISTANCE );
            isAssociateTss = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_IS_ASSOCIATE_TSS );
            associateTssWindow = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_ASSOCIATE_TSS_WINDOW );
            boolean isFwdAnalysisDirection = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_ANALYSIS_DIRECTION );
            if( readClassParams.isStrandBothOption() ) {
                readClassParams.setStrandOption( isFwdAnalysisDirection ? Strand.BothForward : Strand.BothReverse );
            }
        }
        if( performOperonAnalysis ) {
            autoOperonParamEstimation = (boolean) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_AUTO_OPERON_PARAMS );
            minSpanningReads = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MIN_SPANNING_READS );
            selOperonFeatureTypes = (Set<FeatureType>) wiz.getProperty( selOperonFeatureTypesPropString );
        }
        if( performNormAnalysis ) {
            minNumberReads = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS );
            maxNumberReads = (int) wiz.getProperty( TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS );
            selNormFeatureTypes = (Set<FeatureType>) wiz.getProperty( selNormFeatureTypesPropString );
            normFeatureStartOffset = (int) wiz.getProperty( propStringNormFeatureStartOffset );
            normFeatureStopOffset = (int) wiz.getProperty( propStringNormFeatureStopOffset );
        }
        //create parameter set for each analysis
        parametersTss = new ParameterSetTSS( performTSSAnalysis, autoTssParamEstimation, performUnannotatedTranscriptDet,
                                             minTotalIncrease, minPercentIncrease, maxLowCovInitCount, minLowCovIncrease, minTranscriptExtensionCov,
                                             maxLeaderlessDistance, maxFeatureDistance, isAssociateTss, associateTssWindow, readClassParams );
        parametersOperonDet = new ParameterSetOperonDet( performOperonAnalysis, minSpanningReads, autoOperonParamEstimation, selOperonFeatureTypes, readClassParams );
        parametersNormalization = new ParameterSetNormalization( performNormAnalysis, minNumberReads, maxNumberReads,
                                                                 normFeatureStartOffset, normFeatureStopOffset,
                                                                 selNormFeatureTypes, readClassParams );

        TrackConnector connector;
        if( !combineTracks ) {
            for( PersistentTrack track : this.tracks ) {

                try {
                    connector = (new SaveFileFetcherForGUI()).getTrackConnector( track );
                } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                    SaveFileFetcherForGUI.showPathSelectionErrorMsg();
                    continue;
                }

                //every track has its own analysis handlers
                this.createAnalysis( connector, readClassParams );
            }
        } else {
            try {
                connector = (new SaveFileFetcherForGUI()).getTrackConnector( tracks, combineTracks );
                this.createAnalysis( connector, readClassParams ); //every track has its own analysis handlers

            } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
                SaveFileFetcherForGUI.showPathSelectionErrorMsg();
            }

        }
    }


    /**
     * Creates the analysis for a TrackConnector.
     * <p>
     * @param connector         the connector
     * @param readClassesParams the read class parameters
     */
    private void createAnalysis( TrackConnector connector, ParametersReadClasses readClassesParams ) {
        AnalysisTranscriptionStart analysisTSS = null;
        AnalysisOperon analysisOperon = null;
        AnalysisNormalization analysisNormalization = null;

        AnalysesHandler covAnalysisHandler = connector.createAnalysisHandler( this,
                                                                              NbBundle.getMessage( OpenTranscriptionAnalysesAction.class, "MSG_AnalysesWorker.progress.name" ),
                                                                              readClassesParams );
        AnalysesHandler mappingAnalysisHandler = connector.createAnalysisHandler( this,
                                                                                  NbBundle.getMessage( OpenTranscriptionAnalysesAction.class, "MSG_AnalysesWorker.progress.name" ),
                                                                                  readClassesParams );

        if( parametersTss.isPerformTSSAnalysis() ) {

            if( parametersTss.isPerformUnannotatedTranscriptDet() ) {
                analysisTSS = new AnalysisUnannotatedTransStart( connector, parametersTss );
            } else {
                analysisTSS = new AnalysisTranscriptionStart( connector, parametersTss );
            }
            covAnalysisHandler.registerObserver( analysisTSS );
            covAnalysisHandler.setCoverageNeeded( true );
            covAnalysisHandler.setDesiredData( Properties.READ_STARTS );
        }
        if( parametersOperonDet.isPerformOperonAnalysis() ) {
            analysisOperon = new AnalysisOperon( connector, parametersOperonDet );

            mappingAnalysisHandler.registerObserver( analysisOperon );
            mappingAnalysisHandler.setMappingsNeeded( true );
            mappingAnalysisHandler.setDesiredData( Properties.REDUCED_MAPPINGS );
        }
        if( parametersNormalization.isPerformNormAnalysis() ) {
            analysisNormalization = new AnalysisNormalization( connector, parametersNormalization );

            mappingAnalysisHandler.registerObserver( analysisNormalization );
            mappingAnalysisHandler.setMappingsNeeded( true );
            mappingAnalysisHandler.setDesiredData( Properties.REDUCED_MAPPINGS );
        }

        trackToAnalysisMap.put( connector.getTrackID(), new AnalysisContainer( analysisTSS, analysisOperon, analysisNormalization ) );
        covAnalysisHandler.startAnalysis();
        mappingAnalysisHandler.startAnalysis();
    }


    /**
     * Visualizes the data handed over to this method as defined by the
     * implementation.
     * <p>
     * @param dataTypeObject the data object to visualize.
     */
    @Override
    public void showData( Object dataTypeObject ) {
        try {
            @SuppressWarnings( "unchecked" )
            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) dataTypeObject;
            final int trackId = dataTypePair.getFirst();
            final String dataType = dataTypePair.getSecond();

            SwingUtilities.invokeLater( new Runnable() { //because it is not called from the swing dispatch thread
                @Override
                public void run() {

                    if( parametersTss.isPerformTSSAnalysis() && dataType.equals( AnalysesHandler.DATA_TYPE_COVERAGE ) ) {

                        ++finishedCovAnalyses;

                        //TODO: bp window of neighboring TSS parameter

                        AnalysisTranscriptionStart analysisTSS = trackToAnalysisMap.get( trackId ).getAnalysisTSS();
                        parametersTss = analysisTSS.getParametersTSS(); //if automatic is on, the parameters are different now
                        if( transcriptionStartResultPanel == null ) {
                            transcriptionStartResultPanel = new ResultPanelTranscriptionStart( refViewer );
                        }

                        TssDetectionResult tssResult = new TssDetectionResult( analysisTSS.getResults(), parametersTss, trackMap, reference, combineTracks, 1, 0 );
                        transcriptionStartResultPanel.addResult( tssResult );

                        if( finishedCovAnalyses >= tracks.size() || combineTracks ) {
                            String trackNames = GeneralUtils.generateConcatenatedString( tssResult.getTrackNameList(), 120 ); // get track name(s) for tab descriptions
                            String panelName = "Detected TSSs for " + trackNames + " (" + transcriptionStartResultPanel.getDataSize() + " hits)";
                            transcAnalysesTopComp.openAnalysisTab( panelName, transcriptionStartResultPanel );
                        }
                    }
                    if( dataType.equals( AnalysesHandler.DATA_TYPE_MAPPINGS ) ) {
                        ++finishedMappingAnalyses;

                        if( parametersOperonDet.isPerformOperonAnalysis() ) {

                            if( operonResultPanel == null ) {
                                operonResultPanel = new ResultPanelOperonDetection( parametersOperonDet, refViewer.getBoundsInformationManager() );
                            }
                            OperonDetectionResult operonDetectionResult = new OperonDetectionResult( trackMap,
                                                                                                     trackToAnalysisMap.get( trackId ).getAnalysisOperon().getResults(), reference, combineTracks, 2, 0 );
                            operonDetectionResult.setParameters( parametersOperonDet );
                            operonResultPanel.addResult( operonDetectionResult );

                            if( finishedMappingAnalyses >= tracks.size() || combineTracks ) {
                                String trackNames = GeneralUtils.generateConcatenatedString( operonDetectionResult.getTrackNameList(), 120 ); // get track name(s) for tab descriptions
                                String panelName = "Detected operons for " + trackNames + " (" + operonResultPanel.getDataSize() + " hits)";
                                transcAnalysesTopComp.openAnalysisTab( panelName, operonResultPanel );
                            }
                        }

                        if( parametersNormalization.isPerformNormAnalysis() ) {
                            AnalysisNormalization normalizationAnalysis = trackToAnalysisMap.get( trackId ).getAnalysisNorm();
                            if( normalizationResultPanel == null ) {
                                normalizationResultPanel = new ResultPanelNormalization( refViewer.getBoundsInformationManager() );
                            }
                            NormalizationAnalysisResult normAnalysisResult = new NormalizationAnalysisResult( trackMap,
                                                                                                              trackToAnalysisMap.get( trackId ).getAnalysisNorm().getResults(),
                                                                                                              reference, combineTracks, 1, 0 );
                            normAnalysisResult.setParameters( parametersNormalization );
                            normAnalysisResult.setNoGenomeFeatures( normalizationAnalysis.getNoGenomeFeatures() );
                            normAnalysisResult.setTotalMappings( normalizationAnalysis.getTotalMappings() );
                            normalizationResultPanel.addResult( normAnalysisResult );

                            if( finishedMappingAnalyses >= tracks.size() || combineTracks ) {
                                String trackNames = GeneralUtils.generateConcatenatedString( normAnalysisResult.getTrackNameList(), 120 ); // get track name(s) for tab descriptions
                                String panelName = "TPM, RPKM & read count values for " + trackNames + " (" + normalizationResultPanel.getDataSize() + " hits)";
                                transcAnalysesTopComp.openAnalysisTab( panelName, normalizationResultPanel );
                            }
                        }
                    }
                }


            } );
        } catch( ClassCastException e ) {
            LOG.log( Level.INFO, "Unknown data passed to {0}", getClass().getName() );
            //do nothing, we dont handle other data in this class
        }

    }


    /**
     * Container class for all available transcription analyses.
     */
    private class AnalysisContainer {

        private final AnalysisTranscriptionStart analysisTSS;
        private final AnalysisOperon analysisOperon;
        private final AnalysisNormalization analysisNormalization;


        /**
         * Container class for all available transcription analyses.
         */
        AnalysisContainer( AnalysisTranscriptionStart analysisTSS, AnalysisOperon analysisOperon,
                           AnalysisNormalization analysisNormalization ) {
            this.analysisTSS = analysisTSS;
            this.analysisOperon = analysisOperon;
            this.analysisNormalization = analysisNormalization;
        }


        /**
         * @return The transcription start site analysis stored in this
         *         container.
         */
        public AnalysisTranscriptionStart getAnalysisTSS() {
            return analysisTSS;
        }


        /**
         * @return The operon detection stored in this container.
         */
        public AnalysisOperon getAnalysisOperon() {
            return analysisOperon;
        }


        /**
         * @return The normalization analysis stored in this container.
         */
        public AnalysisNormalization getAnalysisNorm() {
            return analysisNormalization;
        }


    }

}
