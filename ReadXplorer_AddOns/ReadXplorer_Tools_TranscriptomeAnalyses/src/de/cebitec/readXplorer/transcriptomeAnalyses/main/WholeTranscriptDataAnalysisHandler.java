package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.AnalysisStatus;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

/**
 * This class starts all analysis to be performed on a whole transcript dataset.
 *
 * @author jritter
 */
public class WholeTranscriptDataAnalysisHandler extends Thread implements Observable, DataVisualisationI {

    private TrackConnector trackConnector;
    private PersistantTrack selectedTrack;
    private Integer refGenomeID;
    private double fraction;
    private List<de.cebitec.readXplorer.util.Observer> observer = new ArrayList<>();
    private List<int[]> region2Exclude;
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    private StatisticsOnMappingData stats;
    private double backgroundCutoff;
    private ParameterSetWholeTranscriptAnalyses parameters;
    private GenomeFeatureParser featureParser;
    private RPKMValuesCalculation rpkmCalculation;
    private OperonDetection operonDetection;
    private NovelTranscriptDetection newRegionDetection;
    private final ReferenceViewer refViewer;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private Map<Integer, PersistantTrack> trackMap;
    private ProgressHandle progressHandle;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;
    private ResultPanelRPKM rpkmResultPanel;
    private NovelRegionResultPanel novelRegionResult;
    private ResultPanelOperonDetection operonResultPanel;

    /**
     * Constructor for WholeTranscriptDataAnalysisHandler.
     *
     * @param selectedTrack PersistantTrack the analysis is based on.
     * @param parameterset ParameterSetWholeTranscriptAnalyses stores all
     * paramaters for whole transcript dataset analysis.
     * @param refViewer ReferenceViewer
     * @param transcAnalysesTopComp
     * TranscriptomeAnalysesTopComponentTopComponent output widow for computed
     * results.
     * @param trackMap contains all PersistantTracks used for this analysis-run.
     */
    public WholeTranscriptDataAnalysisHandler(PersistantTrack selectedTrack, ParameterSetWholeTranscriptAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, Map<Integer, PersistantTrack> trackMap) {
        this.selectedTrack = selectedTrack;
        this.refGenomeID = refViewer.getReference().getId();
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
            this.trackConnector = (new SaveFileFetcherForGUI()).getTrackConnector(selectedTrack);
        } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveFileFetcherForGUI.showPathSelectionErrorMsg();
        }

        String handlerTitle = "Creating data structures from feature-information of the reference: " + trackConnector.getAssociatedTrackName();
        this.progressHandle = ProgressHandleFactory.createHandle(handlerTitle);
        this.progressHandle.start(120);
        this.featureParser = new GenomeFeatureParser(this.trackConnector, progressHandle);
        this.allRegionsInHash = this.featureParser.getGenomeFeaturesInHash(this.featureParser.getGenomeFeatures());

        if (parameters.isPerformNovelRegionDetection()) {
            this.progressHandle.progress("Loading additional feature information ... ", 105);
            featureParser.generateAllFeatureStrandInformation(this.featureParser.getGenomeFeatures());
            this.region2Exclude = this.featureParser.getRegion2Exclude();
            this.forwardCDSs = this.featureParser.getForwardCDSs();
            this.reverseCDSs = this.featureParser.getReverseCDSs();
            this.progressHandle.progress(120);
            this.progressHandle.finish();
        } else {
            this.featureParser.parseFeatureInformation(this.featureParser.getGenomeFeatures());
            this.region2Exclude = this.featureParser.getRegion2Exclude();
            this.forwardCDSs = this.featureParser.getForwardCDSs();
            this.reverseCDSs = this.featureParser.getReverseCDSs();
            this.progressHandle.progress(120);
            this.progressHandle.finish();
        }

        // geting Mappings and calculate statistics on mappings.
        try {
            trackConnector = (new SaveFileFetcherForGUI()).getTrackConnector(this.selectedTrack);
            this.stats = new StatisticsOnMappingData(refViewer.getReference(), this.fraction, this.forwardCDSs,
                    this.reverseCDSs, this.allRegionsInHash, this.region2Exclude);
            AnalysesHandler handler = new AnalysesHandler(trackConnector, this, "Collecting coverage data of track number "
                    + this.selectedTrack.getId(), new ParametersReadClasses(true, false, false, false, new Byte("0"))); // TODO: ParameterReadClasses noch in den Wizard einbauen und die parameter hier mit übergeben!
            handler.setMappingsNeeded(true);
            handler.setDesiredData(Properties.REDUCED_MAPPINGS);
            handler.registerObserver(this.stats);
            handler.startAnalysis();
        } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveFileFetcherForGUI.showPathSelectionErrorMsg();
            notifyObservers(AnalysisStatus.ERROR);
            this.interrupt();
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observer.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observer.remove(observer);
        if (this.observer.isEmpty()) {
            this.interrupt();
        }
    }

    @Override
    public void run() {
        notifyObservers(AnalysisStatus.RUNNING);
        try {
            startAnalysis();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void notifyObservers(Object data) {
        List<de.cebitec.readXplorer.util.Observer> tmpObserver = new ArrayList<>(observer);
        for (Iterator<de.cebitec.readXplorer.util.Observer> it = tmpObserver.iterator(); it.hasNext();) {
            de.cebitec.readXplorer.util.Observer currentObserver = it.next();
            currentObserver.update(data);
        }
    }

    @Override
    public void showData(Object data) {
        Pair<Integer, String> dataTypePair = (Pair<Integer, String>) data;
        final int trackId = dataTypePair.getFirst();
        this.backgroundCutoff = this.stats.calculateBackgroundCutoff(this.parameters.getFraction());
        this.stats.setBgThreshold(this.backgroundCutoff);


        this.stats.initMappingsStatistics();
        if (parameters.isPerformingRPKMs()) {
            rpkmCalculation = new RPKMValuesCalculation(this.allRegionsInHash, this.stats, trackId);
            rpkmCalculation.calculationExpressionValues(trackConnector.getRefGenome());

            String trackNames;

            if (rpkmResultPanel == null) {
                rpkmResultPanel = new ResultPanelRPKM();
                rpkmResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
            }

            RPKMAnalysisResult rpkmAnalysisResult = new RPKMAnalysisResult(trackMap, rpkmCalculation.getRpkmValues(), refGenomeID);
            rpkmResultPanel.addResult(rpkmAnalysisResult);
            trackNames = GeneralUtils.generateConcatenatedString(rpkmAnalysisResult.getTrackNameList(), 120);
            String panelName = "RPKM and read count values for " + trackNames + " (" + rpkmResultPanel.getDataSize() + " hits)";
            transcAnalysesTopComp.openAnalysisTab(panelName, rpkmResultPanel);
        }

        if (parameters.isPerformNovelRegionDetection()) {
            newRegionDetection = new NovelTranscriptDetection(trackConnector.getRefGenome(), trackId);

            newRegionDetection.runningNewRegionsDetection(featureParser.getAllFwdFeatures(), featureParser.getAllRevFeatures(), allRegionsInHash,
                    this.stats, this.parameters);
            String trackNames;

            if (novelRegionResult == null) {
                novelRegionResult = new NovelRegionResultPanel();
                novelRegionResult.setReferenceViewer(refViewer);
            }

            NovelRegionResult newRegionResult = new NovelRegionResult(stats, trackMap, newRegionDetection.getNovelRegions(), false);
            newRegionResult.setParameters(this.parameters);
            novelRegionResult.addResult(newRegionResult);

            trackNames = GeneralUtils.generateConcatenatedString(newRegionResult.getTrackNameList(), 120);
            String panelName = "Novel region detection results" + trackNames + " (" + novelRegionResult.getDataSize() + " hits)";
            transcAnalysesTopComp.openAnalysisTab(panelName, novelRegionResult);
        }

        if (parameters.isPerformOperonDetection()) {
            /**
             * The List contains the featureID of the first and second feature.
             * The Integer represents the count mappings are span over this
             * Operon.
             */
            List<Operon> fwdOperons, revOperons;
            operonDetection = new OperonDetection(trackId);
            fwdOperons = operonDetection.concatOperonAdjacenciesToOperons(stats.getPutativeOperonAdjacenciesFWD(), this.trackConnector, stats.getBgThreshold());
            revOperons = operonDetection.concatOperonAdjacenciesToOperons(stats.getPutativeOperonAdjacenciesREV(), this.trackConnector, this.stats.getBgThreshold());
            List<Operon> detectedOperons = new ArrayList<>(fwdOperons);
            detectedOperons.addAll(revOperons);
            String trackNames;

            if (operonResultPanel == null) {
                operonResultPanel = new ResultPanelOperonDetection();
                operonResultPanel.setReferenceViewer(refViewer);
            }

            OperonDetectionResult operonDetectionResult = new OperonDetectionResult(this.stats, this.trackMap, detectedOperons, refGenomeID);
            operonDetectionResult.setParameters(this.parameters);
            operonResultPanel.addResult(operonDetectionResult);

            trackNames = GeneralUtils.generateConcatenatedString(operonDetectionResult.getTrackNameList(), 120);
            String panelName = "Operon detection results " + trackNames + " (" + operonResultPanel.getDataSize() + " hits)";
            transcAnalysesTopComp.openAnalysisTab(panelName, operonResultPanel);
        }

        this.stats.clearMemory();
        this.clearMemory();


        notifyObservers(AnalysisStatus.FINISHED);
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
