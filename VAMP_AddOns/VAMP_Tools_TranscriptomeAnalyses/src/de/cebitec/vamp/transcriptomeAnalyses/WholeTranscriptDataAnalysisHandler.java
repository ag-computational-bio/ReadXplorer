/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import com.sun.glass.events.WheelEvent;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
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
    private List<de.cebitec.vamp.util.Observer> observer = new ArrayList<>();
    private int[] region2Exclude;
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    private Statistics stats;
    private double backgroundCutoff;
    private ParameterSetWholeTranscriptAnalyses paramerters;
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

    public WholeTranscriptDataAnalysisHandler(GenomeFeatureParser featureParser, PersistantTrack selectedTrack, Integer refGenomeID, ParameterSetWholeTranscriptAnalyses parameterset, ReferenceViewer refViewer, TranscriptomeAnalysesTopComponent transcAnalysesTopComp, HashMap<Integer, PersistantTrack> trackMap) {
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
            notifyObservers(FiveEnrichedDataAnalysesHandler.AnalysisStatus.ERROR);
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
        notifyObservers(FiveEnrichedDataAnalysesHandler.AnalysisStatus.RUNNING);
        try {
            startAnalysis();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static enum AnalysisStatus {

        RUNNING, FINISHED, ERROR;
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

    }
}
