/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class FifeEnrichedDataAnalysesHandler extends Thread implements Observable, DataVisualisationI {

    private TrackConnector trackConnector;
    private PersistantTrack selectedTrack;
    private List<PersistantMapping> mappings;
    private Integer refGenomeID;
    private double fraction;
    private List<de.cebitec.vamp.util.Observer> observer = new ArrayList<>();
    private int[] region2Exclude;
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    private Statistics stats;
    private double backgroundCutoff;
    private ParameterSetFiveEnrichedAnalyses paramerters;
    private GenomeFeatureParser featureParser;
    private TssDetection tssDetection;
    private OperonDetection operonDetection;
     private ResultPanelTranscriptionStart transcriptionStartResultPanel;
     private final ReferenceViewer refViewer;
     private TranscriptomeAnalysesTopComponent transcAnalysesTopComp;
     private HashMap<Integer, PersistantTrack> trackMap;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;

    public FifeEnrichedDataAnalysesHandler(GenomeFeatureParser featureParser, PersistantTrack selectedTrack, Integer refGenomeID, ParameterSetFiveEnrichedAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponent transcAnalysesTopComp, HashMap<Integer, PersistantTrack> trackMap) {

        this.featureParser = featureParser;
        this.selectedTrack = selectedTrack;
        this.refGenomeID = refGenomeID;
        this.fraction = parameterset.getFraction();
        this.paramerters = parameterset;
        this.refViewer = refViewer;
        this.transcAnalysesTopComp = transcAnalysesTopComp;
        this.trackMap = trackMap;
    }

    private void startAnalysis() throws FileNotFoundException {

        // Initiation of important structures
        this.region2Exclude = this.featureParser.getRegion2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getReverseCDSs();
        this.allRegionsInHash = this.featureParser.getAllRegionsInHash();


        // geting Mappings and calculate statistics on mappings.
        try {
            trackConnector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(this.selectedTrack);
            this.stats = new Statistics(featureParser.getRefSeqLength(), this.fraction, this.forwardCDSs, this.reverseCDSs, this.allRegionsInHash, this.region2Exclude);
            de.cebitec.vamp.databackend.AnalysesHandler handler = new de.cebitec.vamp.databackend.AnalysesHandler(trackConnector, this, "Collecting coverage data of track number "
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

        // Next Steps in showData method!
    }

    @Override
    public void registerObserver(de.cebitec.vamp.util.Observer observer) {
        this.observer.add(observer);
    }

    @Override
    public void removeObserver(de.cebitec.vamp.util.Observer observer) {
        this.observer.remove(observer);
        if (this.observer.isEmpty()) {
            this.interrupt();
        }
    }

    public static enum AnalysisStatus {

        RUNNING, FINISHED, ERROR;
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
        List<de.cebitec.vamp.util.Observer> tmpObserver = new ArrayList<>(observer);
        for (Iterator<de.cebitec.vamp.util.Observer> it = tmpObserver.iterator(); it.hasNext();) {
            de.cebitec.vamp.util.Observer currentObserver = it.next();
            currentObserver.update(data);
        }
    }

    @Override
    public void showData(Object data) {
        this.mappings = this.stats.getMappings();
        this.stats.parseMappings(this.mappings);
        this.backgroundCutoff = this.stats.calculateBackgroundCutoff(this.paramerters.getFraction(), this.featureParser.getRefSeqLength());

        System.out.println("BackgroundCutoff: " + backgroundCutoff);

        this.stats.initMappingsStatistics();
        if (paramerters.isPerformTSSAnalysis()) {
            tssDetection = new TssDetection(this.trackConnector.getRefGenome().getSequence());
            tssDetection.runningTSSDetection(this.featureParser.getRefSeqLength(), this.forwardCDSs, this.reverseCDSs,
                    this.allRegionsInHash, this.stats.getForward(), this.stats.getReverse(), this.stats.getFwdCoverage(), this.stats.getRevCoverage(),
                    this.paramerters.getRatio(), this.stats.getMm(), this.backgroundCutoff, this.paramerters.getUpstreamRegion(), this.paramerters.getDownstreamRegion());
        }



        if (paramerters.isPerformLeaderlessAnalysis()) {
            // TODO Not yet implemented
        }
        if (paramerters.isPerformAntisenseAnalysis()) {
            // Not yet implemented
        }


        notifyObservers(AnalysisStatus.FINISHED);

        System.out.println("We are in Show data of StartTranscriptomeAnalysesAction!");
        try {
            @SuppressWarnings("unchecked")
            Pair<Integer, String> dataTypePair = (Pair<Integer, String>) data;
            final int trackId = dataTypePair.getFirst();
            final String dataType = dataTypePair.getSecond();

            SwingUtilities.invokeLater(new Runnable() { 
                private Statistics stats = this.stats;
//because it is not called from the swing dispatch thread
                @Override
                public void run() {

                    //get track name(s) for tab descriptions
                    String trackNames;
                    
                    if (paramerters.isPerformTSSAnalysis()) {

                        TssDetection analysisTSS = getTssDetection();
                        if (transcriptionStartResultPanel == null) {
                            transcriptionStartResultPanel = new ResultPanelTranscriptionStart();
                            transcriptionStartResultPanel.setReferenceViewer(refViewer);
                        }
                        
                        TSSDetectionResults tssResult = new TSSDetectionResults(this.stats, analysisTSS.getResults(), getTrackMap());
                        transcriptionStartResultPanel.addResult(tssResult);

                            trackNames = GeneralUtils.generateConcatenatedString(tssResult.getTrackNameList(), 120);
                            String panelName = "Detected TSSs for " + trackNames + " (" + transcriptionStartResultPanel.getResultSize() + " hits)";
                            transcAnalysesTopComp.openAnalysisTab(panelName, transcriptionStartResultPanel);
                    }
//                    if (dataType.equals(AnalysesHandler.DATA_TYPE_MAPPINGS)) {
//                        ++finishedMappingAnalyses;
//
//                        if (parametersOperonDet.isPerformOperonAnalysis()) {
//
//                            if (operonResultPanel == null) {
//                                operonResultPanel = new ResultPanelOperonDetection(parametersOperonDet);
//                                operonResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
//                            }
//                            OperonDetectionResult operonDetectionResult = new OperonDetectionResult(trackMap,
//                                    trackToAnalysisMap.get(trackId).getAnalysisOperon().getResults());
//                            operonDetectionResult.setParameters(parametersOperonDet);
//                            operonResultPanel.addResult(operonDetectionResult);
//
//                            if (finishedMappingAnalyses >= tracks.size()) {
//                                trackNames = GeneralUtils.generateConcatenatedString(operonDetectionResult.getTrackNameList(), 120);
//                                String panelName = "Detected operons for " + trackNames + " (" + operonResultPanel.getResultSize() + " hits)";
//                                transcAnalysesTopComp.openAnalysisTab(panelName, operonResultPanel);
//                            }
//                        }
//
//                        if (parametersRPKM.isPerformRPKMAnalysis()) {
//                            AnalysisRPKM rpkmAnalysis = trackToAnalysisMap.get(trackId).getAnalysisRPKM();
//                            if (rpkmResultPanel == null) {
//                                rpkmResultPanel = new ResultPanelRPKM();
//                                rpkmResultPanel.setBoundsInfoManager(refViewer.getBoundsInformationManager());
//                            }
//                            RPKMAnalysisResult rpkmAnalysisResult = new RPKMAnalysisResult(trackMap,
//                                    trackToAnalysisMap.get(trackId).getAnalysisRPKM().getResults());
//                            rpkmAnalysisResult.setParameters(parametersRPKM);
//                            rpkmAnalysisResult.setNoGenomeFeatures(rpkmAnalysis.getNoGenomeFeatures());
//                            rpkmResultPanel.addResult(rpkmAnalysisResult);
//
//                            if (finishedMappingAnalyses >= tracks.size()) {
//                                trackNames = GeneralUtils.generateConcatenatedString(rpkmAnalysisResult.getTrackNameList(), 120);
//                                String panelName = "RPKM and read count values for " + trackNames + " (" + rpkmResultPanel.getResultSize() + " hits)";
//                                transcAnalysesTopComp.openAnalysisTab(panelName, rpkmResultPanel);
//                            }
//                        }
//                    }
                }
            });
        } catch (ClassCastException e) {
            //do nothing, we dont handle other data in this class
        }
     
    }

    public HashMap<Integer, List<Integer>> getForwardCDSs() {
        return forwardCDSs;
    }

    public HashMap<Integer, List<Integer>> getReverseCDSs() {
        return reverseCDSs;
    }

    public int[] getRegion2Exclude() {
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
