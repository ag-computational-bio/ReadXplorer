package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 * This class is an analysis handler for analyses which has to be performed on a
 * 5'-enriched dataset.
 *
 * @author jritter
 */
public class FiveEnrichedDataAnalysesHandler extends Thread implements Observable, DataVisualisationI {

    private TrackConnector trackConnector;
    private final PersistentTrack selectedTrack;
    private final PersistentReference reference;
    private final List<de.cebitec.readXplorer.util.Observer> observer = new ArrayList<>();
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    private StatisticsOnMappingData stats;
    private double backgroundCutoff;
    private final ParameterSetFiveEnrichedAnalyses parameters;
    private GenomeFeatureParser featureParser;
    private TssDetection tssDetection;
    private ResultPanelTranscriptionStart transcriptionStartResultPanel;
    private final ReferenceViewer refViewer;
    private final TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private Map<Integer, PersistentTrack> trackMap;
    private ProgressHandle progressHandleParsingFeatures;
    /**
     * Key: featureID , Value: PersistentFeature
     */
    private Map<Integer, PersistentFeature> allRegionsInHash;

    /**
     * Constructor for FiveEnrichedDataAnalysesHandler.
     *
     * @param selectedTrack Track the analysis is based on.
     * @param parameterset ParameterSetFiveEnrichedAnalyses stores all
     * paramaters for 5'-enriched based datasets analysis.
     * @param refViewer ReferenceViewer
     * @param transcAnalysesTopComp
     * TranscriptomeAnalysesTopComponentTopComponent output widow for computed
     * results.
     * @param trackMap contains all PersistentTracks used for this analysis-run.
     */
    public FiveEnrichedDataAnalysesHandler(PersistentTrack selectedTrack, ParameterSetFiveEnrichedAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, Map<Integer, PersistentTrack> trackMap) {
        this.selectedTrack = selectedTrack;
        this.reference = refViewer.getReference();
        this.parameters = parameterset;
        this.refViewer = refViewer;
        this.transcAnalysesTopComp = transcAnalysesTopComp;
        this.trackMap = trackMap;
    }

    /**
     * Starts the analysis.
     */
    private void startAnalysis() {

        try {
            this.trackConnector = (new SaveFileFetcherForGUI()).getTrackConnector(selectedTrack);
        } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveFileFetcherForGUI.showPathSelectionErrorMsg();
        }
        String handlerTitle = "Creating data structures from feature-information of the reference: " + trackConnector.getAssociatedTrackName();
        this.progressHandleParsingFeatures = ProgressHandleFactory.createHandle(handlerTitle);
        this.progressHandleParsingFeatures.start(100);
        this.featureParser = new GenomeFeatureParser(this.trackConnector, this.progressHandleParsingFeatures);
        this.allRegionsInHash = this.featureParser.getGenomeFeaturesInHash(this.featureParser.getGenomeFeatures());
        this.featureParser.parseFeatureInformation(this.featureParser.getGenomeFeatures());

        this.progressHandleParsingFeatures.finish();

        // geting mappings and calculate statistics
        this.stats = new StatisticsOnMappingData(trackConnector, parameters.getFraction(), this.featureParser.getForwardCDSs(), this.featureParser.getRevFeatures(), this.allRegionsInHash, this.featureParser.getPositions2Exclude());
        List<Classification> excludedClasses = new ArrayList<>();
        excludedClasses.add(MappingClass.COMMON_MATCH);
        excludedClasses.add(FeatureType.MULTIPLE_MAPPED_READ);
        if (!this.parameters.isIncludeBestMatchedReads()) { excludedClasses.add(FeatureType.MULTIPLE_MAPPED_READ); }
        AnalysesHandler handler = new AnalysesHandler(trackConnector, this, "Collecting coverage data of track number "
                + this.selectedTrack.getId(), new ParametersReadClasses(excludedClasses, new Byte("0")));
        handler.setMappingsNeeded(true);
        handler.setDesiredData(Properties.REDUCED_MAPPINGS);
        handler.registerObserver(this.stats);
        handler.startAnalysis();
    }

    @Override
    public void registerObserver(de.cebitec.readXplorer.util.Observer observer) {
        this.observer.add(observer);
    }

    @Override
    public void removeObserver(de.cebitec.readXplorer.util.Observer observer) {
        this.observer.remove(observer);
        if (this.observer.isEmpty()) {
            this.interrupt();
        }
    }

    @Override
    public void run() {
        notifyObservers(AnalysisStatus.RUNNING);
        startAnalysis();
    }

    @Override
    public void notifyObservers(Object data) {
        List<Observer> tmpObserver = new ArrayList<>(observer);
        for (Observer currentObserver : tmpObserver) {
            currentObserver.update(data);
        }
    }

    @Override
    public void showData(Object data) {
        Pair<Integer, String> dataTypePair = (Pair<Integer, String>) data;
        final int trackId = dataTypePair.getFirst();

        this.backgroundCutoff = this.stats.calculateBackgroundCutoff(this.parameters.getFraction());
        System.out.println("Simulated BG-Threshold: " + this.stats.simulateBackgroundThreshold(this.parameters.getFraction()));
        this.stats.setBgThreshold(this.backgroundCutoff);
        this.stats.initMappingsStatistics();
        List<TranscriptionStart> postProcessedTss = new ArrayList<>();

        this.tssDetection = new TssDetection(trackId);
        for (PersistentChromosome chrom : this.trackConnector.getRefGenome().getChromosomes().values()) {
            int chromId = chrom.getId();
            int chromNo = chrom.getChromNumber();
            int chromLength = chrom.getLength();
            if (parameters.isThresholdManuallySet()) {
                stats.setBgThreshold((double) parameters.getManuallySetThreshold());
            }
            this.tssDetection.runningTSSDetection(refViewer.getReference(),
                    this.featureParser.getForwardCDSs(),
                    this.featureParser.getRevFeatures(),
                    this.allRegionsInHash, this.stats, chromId, this.parameters);

//            List<TranscriptionStart> tssList = this.tssDetection.tssDetermination(stats, chromNo, chromNo, chromId, chromLength);
//            postProcessedTss = this.tssDetection.postProcessing(chrom, tssList, stats.getMappingsPerMillion(), chromLength, this.featureParser.getForwardCDSs(), this.featureParser.getRevFeatures(), allRegionsInHash, parameters);
        }

        String trackNames;
        if (this.transcriptionStartResultPanel == null) {
            this.transcriptionStartResultPanel = new ResultPanelTranscriptionStart();
            this.transcriptionStartResultPanel.setReferenceViewer(this.refViewer);
            this.transcriptionStartResultPanel.setPersistentReference(this.reference);
        }

        this.stats.clearMemory();
        this.clearMemory();

        TSSDetectionResults tssResult = new TSSDetectionResults(this.stats, this.tssDetection.getResults(), getTrackMap(), this.reference);
//        TSSDetectionResults tssResult = new TSSDetectionResults(this.stats, postProcessedTss, getTrackMap(), this.refGenomeID);
        tssResult.setParameters(this.parameters);
        this.transcriptionStartResultPanel.addResult(tssResult);

        trackNames = GeneralUtils.generateConcatenatedString(tssResult.getTrackNameList(), 120);
        String panelName = "TSS detection results for " + trackNames + " Hits: " + transcriptionStartResultPanel.getDataSize();
        this.transcAnalysesTopComp.openAnalysisTab(panelName, this.transcriptionStartResultPanel);

        notifyObservers(AnalysisStatus.FINISHED);
    }

    /**
     * Getter for the trackMap.
     *
     * @return HashMap<Integer, PersistentTrack> containing all PersistentTracks
     * used for this analysis-run.
     */
    public Map<Integer, PersistentTrack> getTrackMap() {
        return trackMap;
    }

    /**
     * Clear the flash memory.
     */
    private void clearMemory() {
        this.allRegionsInHash = null;
        this.forwardCDSs = null;
        this.reverseCDSs = null;
    }
}
