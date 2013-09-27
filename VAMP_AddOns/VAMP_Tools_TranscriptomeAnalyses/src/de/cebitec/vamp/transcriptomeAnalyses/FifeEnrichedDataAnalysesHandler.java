/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.ParameterSetI;
import de.cebitec.vamp.databackend.ParametersReadClasses;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptomeAnalyses.datastructure.ResultsOfTranskriptomeAnalyses;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class FifeEnrichedDataAnalysesHandler extends Thread implements Observable, DataVisualisationI {

    private ReferenceConnector referenceConnector;
    private TrackConnector trackConnector;
    private int genomeSize;
    private List<PersistantFeature> genomeFeatures;
    private PersistantTrack selectedTrack;
    private List<PersistantMapping> mappings;
    private Integer refGenomeID;
    private ResultsOfTranskriptomeAnalyses results;
    private double fraction;
    private List<de.cebitec.vamp.util.Observer> observer = new ArrayList<de.cebitec.vamp.util.Observer>();
    private int[] region2Exclude;
    protected HashMap<Integer, List<Integer>> forwardCDSs, reverseCDSs;
    private Statistics stats;
    private int backgroundCutoff;
    TssDetection tssDetection;
    ParameterSetFiveEnrichedAnalyses paramerters;
    private GenomeFeatureParser featureParser;
    /**
     * Key: featureID , Value: PersistantFeature
     */
    private HashMap<Integer, PersistantFeature> allRegionsInHash;

    public FifeEnrichedDataAnalysesHandler(PersistantTrack selectedTrack, Integer refGenomeID, ParameterSetFiveEnrichedAnalyses parameterset) {

        this.selectedTrack = selectedTrack;
        this.refGenomeID = refGenomeID;
        this.fraction = parameterset.getFraction();
        this.paramerters = parameterset;


    }

    private void startAnalysis() throws FileNotFoundException {
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Starting to collect the necessary data for the choosen transcriptome analyses.", currentTimestamp);
        this.referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(this.refGenomeID);
        this.genomeSize = this.referenceConnector.getRefGenome().getSequence().length();
        this.genomeFeatures = this.referenceConnector.getFeaturesForClosedInterval(0, this.genomeSize);


        // Initiation of important structures
        this.featureParser = new GenomeFeatureParser(this.genomeFeatures, this.genomeSize);
        this.region2Exclude = this.featureParser.getRegion2Exclude();
        this.forwardCDSs = this.featureParser.getForwardCDSs();
        this.reverseCDSs = this.featureParser.getReverseCDSs();
        this.allRegionsInHash = this.featureParser.getAllRegionsInHash();


        // geting Mappings and calculate statistics on mappings.
        try {
            trackConnector = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(this.selectedTrack);
            this.stats = new Statistics(this.genomeSize, this.fraction, this.forwardCDSs, this.reverseCDSs, this.allRegionsInHash, this.region2Exclude);
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
            return;
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
        List<de.cebitec.vamp.util.Observer> tmpObserver = new ArrayList<de.cebitec.vamp.util.Observer>(observer);
        for (Iterator<de.cebitec.vamp.util.Observer> it = tmpObserver.iterator(); it.hasNext();) {
            de.cebitec.vamp.util.Observer currentObserver = it.next();
            currentObserver.update(data);
        }
    }

    @Override
    public void showData(Object data) {
        this.mappings = this.stats.getMappings();
        this.stats.parseMappings(this.mappings);
        this.backgroundCutoff = (int) this.stats.calculateBackgroundCutoff(this.paramerters.getFraction(), this.genomeSize);

        System.out.println("BackgroundCutoff: " + backgroundCutoff);

        this.stats.initMappingsStatistics();
        if (paramerters.isPerformTSSAnalysis()) {
            TssDetection tssDetection = new TssDetection(this.referenceConnector.getRefGenome().getSequence());
            tssDetection.runningTSSDetection(this.genomeSize, forwardCDSs, reverseCDSs,
                    allRegionsInHash, this.stats.getForward(), this.stats.getReverse(),
                    this.fraction, this.stats.getMm(), this.backgroundCutoff, this.paramerters.getUpstreamRegion(), this.paramerters.getDownstreamRegion());
        }



        if (paramerters.isPerformLeaderlessAnalysis()) {
            // TODO Not yet implemented
        }
        if (paramerters.isPerformAntisenseAnalysis()) {
            // Not yet implemented
        }
        notifyObservers(AnalysisStatus.FINISHED);
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
}
