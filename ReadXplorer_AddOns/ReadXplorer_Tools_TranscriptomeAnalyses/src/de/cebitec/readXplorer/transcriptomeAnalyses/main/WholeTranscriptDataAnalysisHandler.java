package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.AnalysisStatus;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class WholeTranscriptDataAnalysisHandler extends Thread implements Observable, DataVisualisationI {

    private TrackConnector trackConnector;
    private PersistantTrack selectedTrack;
    private List<PersistantMapping> mappings;
    private Integer refGenomeID;
    private double fraction;
    private List<de.cebitec.readXplorer.util.Observer> observer = new ArrayList<>();
    private int[] region2Exclude;
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
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;
    private ResultPanelRPKM rpkmResultPanel;
    private NovelRegionResultPanel novelRegionResult;
    private ResultPanelOperonDetection operonResultPanel;

    public WholeTranscriptDataAnalysisHandler(PersistantTrack selectedTrack, Integer refGenomeID, ParameterSetWholeTranscriptAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp, HashMap<Integer, PersistantTrack> trackMap) {
        this.selectedTrack = selectedTrack;
        this.refGenomeID = refGenomeID;
        this.fraction = parameterset.getFraction();
        this.parameters = parameterset;
        this.refViewer = refViewer;
        this.transcAnalysesTopComp = transcAnalysesTopComp;
        this.trackMap = trackMap;
    }

    private void startAnalysis() throws FileNotFoundException {

        TrackConnector connector = null;
        try {
            connector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(selectedTrack);
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            JOptionPane.showMessageDialog(null, "You did not complete the track path selection. The track panel cannot be opened.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
        }
        this.featureParser = new GenomeFeatureParser(connector);
        this.featureParser.parseFeatureInformation(featureParser.getGenomeFeatures());

        this.region2Exclude = this.featureParser.getRegion2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getReverseCDSs();
        this.allRegionsInHash = this.featureParser.getAllRegionsInHash();

        // geting Mappings and calculate statistics on mappings.
        try {
            trackConnector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(this.selectedTrack);
            this.stats = new Statistics(featureParser.getRefSeqLength(), this.fraction, this.forwardCDSs, this.reverseCDSs, this.allRegionsInHash, this.region2Exclude);
            de.cebitec.readXplorer.databackend.AnalysesHandler handler = new de.cebitec.readXplorer.databackend.AnalysesHandler(trackConnector, this, "Collecting coverage data of track number "
                    + this.selectedTrack.getId(), new ParametersReadClasses(true, false, false, false)); // TODO: ParameterReadClasses noch in den Wizard einbauen und die parameter hier mit übergeben!
            handler.setMappingsNeeded(true);
            handler.setDesiredData(Properties.REDUCED_MAPPINGS);
            handler.registerObserver(this.stats);
            handler.startAnalysis();
        } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            JOptionPane.showMessageDialog(null, "The path of one of the selected tracks could not be resolved. The analysis will be canceled now.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
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
        this.mappings = this.stats.getMappings();
        this.stats.parseMappings(this.mappings);
        this.backgroundCutoff = this.stats.calculateBackgroundCutoff(this.parameters.getFraction(), this.featureParser.getRefSeqLength());
        this.stats.setBg(this.backgroundCutoff);


        this.stats.initMappingsStatistics();
        if (parameters.isPerformingRPKMs()) {
            rpkmCalculation = new RPKMValuesCalculation(this.allRegionsInHash, this.stats, selectedTrack.getId());
            rpkmCalculation.calculationExpressionValues();

            String trackNames;

            if (rpkmResultPanel == null) {
                rpkmResultPanel = new ResultPanelRPKM();
                rpkmResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
            }

            RPKMAnalysisResult rpkmAnalysisResult = new RPKMAnalysisResult(trackMap, rpkmCalculation.getRpkmValues(), false);
            rpkmResultPanel.addResult(rpkmAnalysisResult);
            trackNames = GeneralUtils.generateConcatenatedString(rpkmAnalysisResult.getTrackNameList(), 120);
            String panelName = "RPKM and read count values for " + trackNames + " (" + rpkmResultPanel.getResultSize() + " hits)";
            transcAnalysesTopComp.openAnalysisTab(panelName, rpkmResultPanel);
        }

        if (parameters.isPerformNovelRegionDetection()) {
            newRegionDetection = new NewRegionDetection();
            newRegionDetection.runningNewRegionsDetection(featureParser.getRefSeqLength(), forwardCDSs, 
                    reverseCDSs, allRegionsInHash, this.stats.getFwdCoverage(), this.stats.getRevCoverage(), 
                    this.stats.getForward(), this.stats.getReverse(), this.stats.getMm(), this.stats.getBg());
            
            String trackNames;

            if (novelRegionResult == null) {
                novelRegionResult = new NovelRegionResultPanel();
                novelRegionResult.setBoundsInfoManager(refViewer.getBoundsInformationManager());
            }

            NovelRegionResult newRegionResult = new NovelRegionResult(trackMap, newRegionDetection.getNovelRegions(), false);
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
            operonDetection = new OperonDetection();
            fwdOperons = operonDetection.concatOperonAdjacenciesToOperons(stats.getPutativeOperonAdjacenciesFWD(), this.trackConnector, stats.getBg());
            revOperons = operonDetection.concatOperonAdjacenciesToOperons(stats.getPutativeOperonAdjacenciesREV(), this.trackConnector, this.stats.getBg());
            List<Operon> detectedOpeons = new ArrayList<>(fwdOperons);
            detectedOpeons.addAll(revOperons);
            String trackNames;

            if (operonResultPanel == null) {
                operonResultPanel = new ResultPanelOperonDetection(parameters);
                operonResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
            }

            OperonDetectionResult operonDetectionResult = new OperonDetectionResult(trackMap, detectedOpeons, false);
            operonResultPanel.addResult(operonDetectionResult);
            trackNames = GeneralUtils.generateConcatenatedString(operonDetectionResult.getTrackNameList(), 120);
            String panelName = "Operon detection results " + trackNames + " (" + operonResultPanel.getResultSize() + " hits)";
            transcAnalysesTopComp.openAnalysisTab(panelName, operonResultPanel);
        }

        notifyObservers(AnalysisStatus.FINISHED);
    }
}
