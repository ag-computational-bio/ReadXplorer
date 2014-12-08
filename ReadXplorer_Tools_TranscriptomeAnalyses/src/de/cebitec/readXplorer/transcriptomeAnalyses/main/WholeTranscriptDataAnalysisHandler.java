
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.AnalysisStatus;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.util.classification.FeatureType;
import de.cebitec.readXplorer.util.classification.MappingClass;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;


/**
 * This class starts all analysis to be performed on a whole transcript dataset.
 *
 * @author jritter
 */
public class WholeTranscriptDataAnalysisHandler extends Thread implements
        Observable, DataVisualisationI {

    private TrackConnector trackConnector;
    private final PersistentTrack selectedTrack;
    private final PersistentReference reference;
    private final double fraction;
    private final List<de.cebitec.readXplorer.util.Observer> observer = new ArrayList<>();
    private List<Set<Integer>> region2Exclude;
    protected Map<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    private StatisticsOnMappingData stats;
    private double backgroundCutoff;
    private final ParameterSetWholeTranscriptAnalyses parameters;
    private GenomeFeatureParser featureParser;
    private RPKMValuesCalculation rpkmCalculation;
    private OperonDetection operonDetection;
    private NovelTranscriptDetection newRegionDetection;
    private final ReferenceViewer refViewer;
    private final TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private Map<Integer, PersistentTrack> trackMap;
    private ProgressHandle progressHandle;
    /**
     * Key: featureID , Value: PersistentFeature
     */
    private Map<Integer, PersistentFeature> allRegionsInHash;
    private ResultPanelRPKM rpkmResultPanel;
    private NovelRegionResultPanel novelRegionResult;
    private ResultPanelOperonDetection operonResultPanel;


    /**
     * Constructor for WholeTranscriptDataAnalysisHandler.
     *
     * @param selectedTrack         Track the analysis is based on.
     * @param parameterset          ParameterSetWholeTranscriptAnalyses stores
     *                              all
     *                              paramaters for whole transcript dataset analysis.
     * @param refViewer             ReferenceViewer
     * @param transcAnalysesTopComp
     *                              TranscriptomeAnalysesTopComponentTopComponent output widow for computed
     *                              results.
     * @param trackMap              contains all PersistentTracks used for this
     *                              analysis-run.
     */
    public WholeTranscriptDataAnalysisHandler( PersistentTrack selectedTrack, ParameterSetWholeTranscriptAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, Map<Integer, PersistentTrack> trackMap ) {
        this.selectedTrack = selectedTrack;
        this.reference = refViewer.getReference();
        this.fraction = parameterset.getFraction();
        this.parameters = parameterset;
        this.refViewer = refViewer;
        this.transcAnalysesTopComp = transcAnalysesTopComp;
        this.trackMap = trackMap;
    }


    /**
     * Starts the analysis.
     *
     * @throws FileNotFoundException
     */
    private void startAnalysis() throws FileNotFoundException {

        try {
            this.trackConnector = (new SaveFileFetcherForGUI()).getTrackConnector( selectedTrack );
        }
        catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
            SaveFileFetcherForGUI.showPathSelectionErrorMsg();
        }

        String handlerTitle = "Creating data structures from feature-information of the reference: " + trackConnector.getAssociatedTrackName();
        this.progressHandle = ProgressHandleFactory.createHandle( handlerTitle );
        this.progressHandle.start( 120 );
        this.featureParser = new GenomeFeatureParser( this.trackConnector, progressHandle );
        this.allRegionsInHash = this.featureParser.getGenomeFeaturesInHash( this.featureParser.getGenomeFeatures() );

        this.featureParser.parseFeatureInformation( this.featureParser.getGenomeFeatures() );
        this.region2Exclude = this.featureParser.getPositions2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getRevFeatures();
        this.progressHandle.progress( 120 );
        this.progressHandle.finish();

        // geting Mappings and calculate statistics on mappings.
        try {
            trackConnector = (new SaveFileFetcherForGUI()).getTrackConnector( this.selectedTrack );
            this.stats = new StatisticsOnMappingData( trackConnector, this.fraction, this.forwardCDSs, this.reverseCDSs, this.allRegionsInHash, this.region2Exclude );
            boolean bestMatchesSelected = false;
            if( parameters.isPerformNovelRegionDetection() ) {
                if( parameters.isIncludeBestMatchedReadsNr() ) {
                    bestMatchesSelected = true;
                }
            }
            else if( parameters.isPerformOperonDetection() ) {
                if( parameters.isIncludeBestMatchedReadsOP() ) {
                    bestMatchesSelected = true;
                }
            }
            else if( parameters.isPerformRPKMs() ) {
                if( parameters.isIncludeBestMatchedReadsRpkm() ) {
                    bestMatchesSelected = true;
                }
            }

            List<Classification> excludedClasses = new ArrayList<>();
            excludedClasses.add( MappingClass.COMMON_MATCH );
            excludedClasses.add( FeatureType.MULTIPLE_MAPPED_READ );
            if( !bestMatchesSelected ) {
                excludedClasses.add( FeatureType.MULTIPLE_MAPPED_READ );
            }
            AnalysesHandler handler = new AnalysesHandler( trackConnector, this, "Collecting coverage data of track number "
                                                                                 + this.selectedTrack.getId(), new ParametersReadClasses( excludedClasses, new Byte( "0" ) ) ); // TODO: ParameterReadClasses noch in den Wizard einbauen und die parameter hier mit übergeben!
            handler.setMappingsNeeded( true );
            handler.setDesiredData( Properties.REDUCED_MAPPINGS );
            handler.registerObserver( this.stats );
            handler.startAnalysis();
        }
        catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
            SaveFileFetcherForGUI.showPathSelectionErrorMsg();
            notifyObservers( AnalysisStatus.ERROR );
            this.interrupt();
        }
    }


    @Override
    public void registerObserver( Observer observer ) {
        this.observer.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        this.observer.remove( observer );
        if( this.observer.isEmpty() ) {
            this.interrupt();
        }
    }


    @Override
    public void run() {
        notifyObservers( AnalysisStatus.RUNNING );
        try {
            startAnalysis();
        }
        catch( FileNotFoundException ex ) {
            Exceptions.printStackTrace( ex );
        }
    }


    @Override
    public void notifyObservers( Object data ) {
        List<de.cebitec.readXplorer.util.Observer> tmpObserver = new ArrayList<>( observer );
        for( Iterator<de.cebitec.readXplorer.util.Observer> it = tmpObserver.iterator(); it.hasNext(); ) {
            de.cebitec.readXplorer.util.Observer currentObserver = it.next();
            currentObserver.update( data );
        }
    }


    @Override
    public void showData( Object data ) {
        Pair<Integer, String> dataTypePair;
        dataTypePair = (Pair<Integer, String>) data;
        final int trackId = dataTypePair.getFirst();
        this.backgroundCutoff = this.stats.calculateBackgroundCutoff( this.parameters.getFraction() );
        this.stats.setBgThreshold( this.backgroundCutoff );

        this.stats.initMappingsStatistics();
        if( parameters.isPerformingRPKMs() ) {
            rpkmCalculation = new RPKMValuesCalculation( this.allRegionsInHash, this.stats, trackId );
            rpkmCalculation.calculationExpressionValues( trackConnector.getRefGenome(), parameters.getReferenceFile() );

            String trackNames;

            if( rpkmResultPanel == null ) {
                rpkmResultPanel = new ResultPanelRPKM();
                rpkmResultPanel.setReferenceViewer( refViewer );
            }

            RPKMAnalysisResult rpkmAnalysisResult = new RPKMAnalysisResult( trackMap, rpkmCalculation.getRpkmValues(), reference );
            rpkmAnalysisResult.setParameters( parameters );
            rpkmResultPanel.addResult( rpkmAnalysisResult );
            trackNames = GeneralUtils.generateConcatenatedString( rpkmAnalysisResult.getTrackNameList(), 120 );
            String panelName = "RPKM values for " + trackNames + " Hits: " + rpkmResultPanel.getDataSize();
            transcAnalysesTopComp.openAnalysisTab( panelName, rpkmResultPanel );
        }

        if( parameters.isPerformNovelRegionDetection() ) {
            newRegionDetection = new NovelTranscriptDetection( trackConnector.getRefGenome(), trackId );

            if( parameters.isThresholdManuallySet() ) {
                stats.setBgThreshold( parameters.getManuallySetThreshold() );
            }
            newRegionDetection.runningNewRegionsDetection( featureParser.getForwardCDSs(), featureParser.getRevFeatures(), allRegionsInHash,
                                                           this.stats, this.parameters );
            String trackNames;

            if( novelRegionResult == null ) {
                novelRegionResult = new NovelRegionResultPanel();
                novelRegionResult.setReferenceViewer( refViewer );
                novelRegionResult.setPersistentReference( reference );
            }

            NovelRegionResult newRegionResult = new NovelRegionResult( reference, stats, trackMap, newRegionDetection.getNovelRegions(), false );
            newRegionResult.setParameters( this.parameters );
            novelRegionResult.addResult( newRegionResult );

            trackNames = GeneralUtils.generateConcatenatedString( newRegionResult.getTrackNameList(), 120 );
            String panelName = "Novel region detection results for " + trackNames + " Hits: " + novelRegionResult.getDataSize();
            transcAnalysesTopComp.openAnalysisTab( panelName, novelRegionResult );
        }

        if( parameters.isPerformOperonDetection() ) {
            /**
             * The List contains the featureID of the first and second feature.
             * The Integer represents the count mappings are span over this
             * Operon.
             */
            List<Operon> fwdOperons, revOperons;
            operonDetection = new OperonDetection( trackId );
            if( parameters.isThresholdManuallySet() ) {
                stats.setBgThreshold( parameters.getManuallySetThreshold() );
            }
            fwdOperons = operonDetection.concatOperonAdjacenciesToOperons( stats.getPutativeOperonAdjacenciesFWD(), stats.getBgThreshold() );
            revOperons = operonDetection.concatOperonAdjacenciesToOperons( stats.getPutativeOperonAdjacenciesREV(), this.stats.getBgThreshold() );
            List<Operon> detectedOperons = new ArrayList<>( fwdOperons );
            detectedOperons.addAll( revOperons );
            String trackNames;

            if( operonResultPanel == null ) {
                operonResultPanel = new ResultPanelOperonDetection();
                operonResultPanel.setReferenceViewer( refViewer );
                operonResultPanel.setPersistentReference( reference );
            }

            OperonDetectionResult operonDetectionResult = new OperonDetectionResult( this.stats, this.trackMap, detectedOperons, reference );
            operonDetectionResult.setParameters( this.parameters );
            operonResultPanel.addResult( operonDetectionResult );

            trackNames = GeneralUtils.generateConcatenatedString( operonDetectionResult.getTrackNameList(), 120 );
            String panelName = "Operon detection results for " + trackNames + " Hits: " + operonResultPanel.getDataSize();
            transcAnalysesTopComp.openAnalysisTab( panelName, operonResultPanel );
        }

        this.stats.clearMemory();
        this.clearMemory();

        notifyObservers( AnalysisStatus.FINISHED );
    }


    /**
     * Clear flash memory.
     */
    private void clearMemory() {
        this.allRegionsInHash = null;
        this.forwardCDSs = null;
        this.reverseCDSs = null;
    }


}
