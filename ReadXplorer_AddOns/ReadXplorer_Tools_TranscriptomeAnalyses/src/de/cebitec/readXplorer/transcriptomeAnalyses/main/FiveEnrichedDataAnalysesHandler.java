package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.AnalysisStatus;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 * This class starts all analysis to be performed on a 5'-enriched dataset.
 *
 * @author jritter
 */
public class FiveEnrichedDataAnalysesHandler extends Thread implements Observable, DataVisualisationI {

    private TrackConnector trackConnector;
    private PersistantTrack selectedTrack;
    private int refGenomeID;
    private List<de.cebitec.readXplorer.util.Observer> observer = new ArrayList<>();
    private List<int[]> region2Exclude;
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    private StatisticsOnMappingData stats;
    private double backgroundCutoff;
    private ParameterSetFiveEnrichedAnalyses parameters;
    private GenomeFeatureParser featureParser;
    private TssDetection tssDetection;
    private ResultPanelTranscriptionStart transcriptionStartResultPanel;
    private final ReferenceViewer refViewer;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private HashMap<Integer, PersistantTrack> trackMap;
    private ProgressHandle progressHandleParsingFeatures;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;

    /**
     * Constructor for FiveEnrichedDataAnalysesHandler.
     *
     * @param selectedTrack PersistantTrack the analysis is based on.
     * @param parameterset ParameterSetFiveEnrichedAnalyses stores all
     * paramaters for 5'-enriched based datasets analysis.
     * @param refViewer ReferenceViewer
     * @param transcAnalysesTopComp
     * TranscriptomeAnalysesTopComponentTopComponent output widow for computed
     * results.
     * @param trackMap contains all PersistantTracks used for this analysis-run.
     */
    public FiveEnrichedDataAnalysesHandler(PersistantTrack selectedTrack, ParameterSetFiveEnrichedAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, HashMap<Integer, PersistantTrack> trackMap) {
        this.selectedTrack = selectedTrack;
        this.refGenomeID = refViewer.getReference().getId();
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

        // Initiation of important structures
        this.region2Exclude = this.featureParser.getRegion2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getReverseCDSs();

        this.featureParser = null;

        // Finish Progress of parsing genome features
        this.progressHandleParsingFeatures.finish();

        // geting Mappings and calculate statistics on them.
        this.stats = new StatisticsOnMappingData(trackConnector.getRefGenome(), parameters.getFraction(), this.forwardCDSs, this.reverseCDSs, this.allRegionsInHash, this.region2Exclude);
        de.cebitec.readXplorer.databackend.AnalysesHandler handler = new de.cebitec.readXplorer.databackend.AnalysesHandler(trackConnector, this, "Collecting coverage data of track number "
                + this.selectedTrack.getId(), new ParametersReadClasses(true, false, false, false)); // TODO: ParameterReadClasses noch in den Wizard einbauen und die parameter hier mit übergeben!
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


        this.tssDetection = new TssDetection(this.trackConnector.getRefGenome(), trackId);
        this.tssDetection.runningTSSDetection(this.forwardCDSs, this.reverseCDSs,
                this.allRegionsInHash, this.stats, this.parameters);

        String trackNames;
        if (this.transcriptionStartResultPanel == null) {
            this.transcriptionStartResultPanel = new ResultPanelTranscriptionStart();
            this.transcriptionStartResultPanel.setReferenceViewer(this.refViewer);
        }

        this.stats.clearMemory();
        this.clearMemory();

        TSSDetectionResults tssResult = new TSSDetectionResults(this.stats, this.tssDetection.getResults(), getTrackMap(), this.refGenomeID);
        tssResult.setParameters(this.parameters);
        this.transcriptionStartResultPanel.addResult(tssResult);

        trackNames = GeneralUtils.generateConcatenatedString(tssResult.getTrackNameList(), 120);
        String panelName = "Detected TSSs for " + trackNames + " (" + transcriptionStartResultPanel.getDataSize() + " hits)";
        this.transcAnalysesTopComp.openAnalysisTab(panelName, this.transcriptionStartResultPanel);

        notifyObservers(AnalysisStatus.FINISHED);
    }

    /**
     * Getter for the trackMap.
     *
     * @return HashMap<Integer, PersistantTrack> containing all PersistantTracks
     * used for this analysis-run.
     */
    public HashMap<Integer, PersistantTrack> getTrackMap() {
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
