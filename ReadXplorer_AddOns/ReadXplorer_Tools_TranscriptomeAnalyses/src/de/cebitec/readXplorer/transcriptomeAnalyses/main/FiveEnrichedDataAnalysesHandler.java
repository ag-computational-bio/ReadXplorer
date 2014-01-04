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
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
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
    private Statistics stats;
    private double backgroundCutoff;
    private ParameterSetFiveEnrichedAnalyses parameters;
    private GenomeFeatureParser featureParser;
    private TssDetection tssDetection;
    private OperonDetection operonDetection;
    private ResultPanelTranscriptionStart transcriptionStartResultPanel;
    private NovelRegionResultPanel novelRegionResultPanel;
    private ResultsPanelAntisense antisenseResultPanel;
    private final ReferenceViewer refViewer;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private HashMap<Integer, PersistantTrack> trackMap;
    private ProgressHandle progressHandleParsingFeatures;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;

    public FiveEnrichedDataAnalysesHandler(PersistantTrack selectedTrack, ParameterSetFiveEnrichedAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, HashMap<Integer, PersistantTrack> trackMap) {

        this.selectedTrack = selectedTrack;
        this.refGenomeID = refViewer.getReference().getId();
        this.parameters = parameterset;
        this.refViewer = refViewer;
        this.transcAnalysesTopComp = transcAnalysesTopComp;
        this.trackMap = trackMap;
    }

    private void startAnalysis() {

        try {
            this.trackConnector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(selectedTrack);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
        }
        String handlerTitle = "Creating data structures from feature-information of the reference: " + trackConnector.getAssociatedTrackName();
        this.progressHandleParsingFeatures = ProgressHandleFactory.createHandle(handlerTitle);
        this.progressHandleParsingFeatures.start(100);
        this.featureParser = new GenomeFeatureParser(this.trackConnector, this.progressHandleParsingFeatures);
        this.featureParser.parseFeatureInformation(this.featureParser.getGenomeFeatures());

        // Initiation of important structures
        this.region2Exclude = this.featureParser.getRegion2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getReverseCDSs();
        this.allRegionsInHash = this.featureParser.getAllRegionsInHash();
        

        // Initiation of important structures
        this.region2Exclude = this.featureParser.getRegion2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getReverseCDSs();
        this.allRegionsInHash = this.featureParser.getAllRegionsInHash();
        this.progressHandleParsingFeatures.finish();

        // geting Mappings and calculate statistics on mappings.
        this.stats = new Statistics(trackConnector.getRefGenome(), parameters.getFraction(), this.forwardCDSs, this.reverseCDSs, this.allRegionsInHash, this.region2Exclude);
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
        final String dataType = dataTypePair.getSecond();

//        this.stats.parseMappings(this.stats.getMappingResults());
        this.backgroundCutoff = this.stats.calculateBackgroundCutoff(this.parameters.getFraction());
        this.stats.setBg(this.backgroundCutoff);

        this.stats.initMappingsStatistics();
        this.tssDetection = new TssDetection(this.trackConnector.getRefGenome(), trackId);
        this.tssDetection.runningTSSDetection(this.forwardCDSs, this.reverseCDSs,
                this.allRegionsInHash, this.stats, this.parameters);

        String trackNames;
        if (transcriptionStartResultPanel == null) {
            transcriptionStartResultPanel = new ResultPanelTranscriptionStart();
            transcriptionStartResultPanel.setReferenceViewer(refViewer);
        }

        TSSDetectionResults tssResult = new TSSDetectionResults(this.stats, this.tssDetection.getResults(), getTrackMap(), refGenomeID);
        tssResult.setParameters(this.parameters);
        transcriptionStartResultPanel.addResult(tssResult);

        trackNames = GeneralUtils.generateConcatenatedString(tssResult.getTrackNameList(), 120);
        String panelName = "Detected TSSs for " + trackNames + " (" + transcriptionStartResultPanel.getResultSize() + " hits)";
        transcAnalysesTopComp.openAnalysisTab(panelName, transcriptionStartResultPanel);

        notifyObservers(AnalysisStatus.FINISHED);
    }

    public HashMap<Integer, List<Integer>> getForwardCDSs() {
        return forwardCDSs;
    }

    public HashMap<Integer, List<Integer>> getReverseCDSs() {
        return reverseCDSs;
    }

    public List<int[]> getRegion2Exclude() {
        return region2Exclude;
    }

    public TssDetection getTssDetection() {
        return tssDetection;
    }

    public OperonDetection getOperonDetection() {
        return operonDetection;
    }

    public HashMap<Integer, PersistantTrack> getTrackMap() {
        return trackMap;
    }
}
