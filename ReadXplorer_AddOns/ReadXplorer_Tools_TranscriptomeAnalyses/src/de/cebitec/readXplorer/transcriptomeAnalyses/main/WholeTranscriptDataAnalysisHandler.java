package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.AnalysisStatus;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

/**
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
    private Statistics stats;
    private double backgroundCutoff;
    private ParameterSetWholeTranscriptAnalyses parameters;
    private GenomeFeatureParser featureParser;
    private RPKMValuesCalculation rpkmCalculation;
    private OperonDetection operonDetection;
    private NewRegionDetection newRegionDetection;
    private ResultPanelTranscriptionStart transcriptionStartResultPanel;
    private final ReferenceViewer refViewer;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private HashMap<Integer, PersistantTrack> trackMap;
    private ProgressHandle progressHandle;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;
    private ResultPanelRPKM rpkmResultPanel;
    private NovelRegionResultPanel novelRegionResult;
    private ResultPanelOperonDetection operonResultPanel;

    public WholeTranscriptDataAnalysisHandler(PersistantTrack selectedTrack, ParameterSetWholeTranscriptAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, HashMap<Integer, PersistantTrack> trackMap) {
        this.selectedTrack = selectedTrack;
        this.refGenomeID = refViewer.getReference().getId();
        this.fraction = parameterset.getFraction();
        this.parameters = parameterset;
        this.refViewer = refViewer;
        this.transcAnalysesTopComp = transcAnalysesTopComp;
        this.trackMap = trackMap;
    }

    private void startAnalysis() throws FileNotFoundException {

        try {
            this.trackConnector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(selectedTrack);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveTrackConnectorFetcherForGUI.showPathSelectionErrorMsg();
        }

        String handlerTitle = "Creating data structures from feature-information of the reference: " + trackConnector.getAssociatedTrackName();
        this.progressHandle = ProgressHandleFactory.createHandle(handlerTitle);
        this.progressHandle.start(120);
        this.featureParser = new GenomeFeatureParser(this.trackConnector, progressHandle);
        this.featureParser.parseFeatureInformation(this.featureParser.getGenomeFeatures());

        this.region2Exclude = this.featureParser.getRegion2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getReverseCDSs();
        this.allRegionsInHash = this.featureParser.getAllRegionsInHash();
        if (parameters.isPerformNovelRegionDetection()) {
            this.progressHandle.progress("Loading additional feature information ... ", 105);
            featureParser.generateAllFeatureStrandInformation();
            this.progressHandle.progress(120);
            this.progressHandle.finish();
        } else {
            this.progressHandle.progress(120);
            this.progressHandle.finish();
        }

        // geting Mappings and calculate statistics on mappings.
        try {
            trackConnector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(this.selectedTrack);
            this.stats = new Statistics(refViewer.getReference(), this.fraction, this.forwardCDSs,
                    this.reverseCDSs, this.allRegionsInHash, this.region2Exclude);
            AnalysesHandler handler = new AnalysesHandler(trackConnector, this, "Collecting coverage data of track number "
                    + this.selectedTrack.getId(), new ParametersReadClasses(true, false, false, false)); // TODO: ParameterReadClasses noch in den Wizard einbauen und die parameter hier mit übergeben!
            handler.setMappingsNeeded(true);
            handler.setDesiredData(Properties.REDUCED_MAPPINGS);
            handler.registerObserver(this.stats);
            handler.startAnalysis();
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            SaveTrackConnectorFetcherForGUI.showPathSelectionErrorMsg();
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
//        this.mappingResults = this.stats.getMappingResults();
//        this.stats.parseMappings(this.mappingResults);
        this.backgroundCutoff = this.stats.calculateBackgroundCutoff(this.parameters.getFraction());
        this.stats.setBg(this.backgroundCutoff);


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
            String panelName = "RPKM and read count values for " + trackNames + " (" + rpkmResultPanel.getResultSize() + " hits)";
            transcAnalysesTopComp.openAnalysisTab(panelName, rpkmResultPanel);
        }

        if (parameters.isPerformNovelRegionDetection()) {
            newRegionDetection = new NewRegionDetection(trackConnector.getRefGenome(), trackId);

            /* TODO: call this method once for each chromosome and generate a list of forwardCDSs and reverseCDSs
             * (one for each chromosome) like this: List<HashMap<Integer, List<Integer>>> forwardCDSs
             */
            int chromNo = 0; //TODO: correctly set these values
            int chromLength = 0;
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
            String panelName = "Novel region detection results" + trackNames + " (" + novelRegionResult.getResultSize() + " hits)";
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
            fwdOperons = operonDetection.concatOperonAdjacenciesToOperons(stats.getPutativeOperonAdjacenciesFWD(), this.trackConnector, stats.getBg());
            revOperons = operonDetection.concatOperonAdjacenciesToOperons(stats.getPutativeOperonAdjacenciesREV(), this.trackConnector, this.stats.getBg());
            List<Operon> detectedOperons = new ArrayList<>(fwdOperons);
            detectedOperons.addAll(revOperons);
            String trackNames;

            if (operonResultPanel == null) {
                operonResultPanel = new ResultPanelOperonDetection();
                operonResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
            }

            OperonDetectionResult operonDetectionResult = new OperonDetectionResult(this.stats, this.trackMap, detectedOperons, refGenomeID);
            operonDetectionResult.setParameters(this.parameters);
            operonResultPanel.addResult(operonDetectionResult);
            
            trackNames = GeneralUtils.generateConcatenatedString(operonDetectionResult.getTrackNameList(), 120);
            String panelName = "Operon detection results " + trackNames + " (" + operonResultPanel.getResultSize() + " hits)";
            transcAnalysesTopComp.openAnalysisTab(panelName, operonResultPanel);
        }

        notifyObservers(AnalysisStatus.FINISHED);
    }
}
