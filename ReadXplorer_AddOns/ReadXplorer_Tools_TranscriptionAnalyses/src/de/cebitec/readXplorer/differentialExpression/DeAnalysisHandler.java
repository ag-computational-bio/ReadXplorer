package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.databackend.AnalysesHandler;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.readXplorer.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.readXplorer.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.view.dialogMenus.SaveTrackConnectorFetcherForGUI;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * @author kstaderm
 */
public abstract class DeAnalysisHandler extends Thread implements Observable, DataVisualisationI {

    private ReferenceConnector referenceConnector;
    private List<PersistantFeature> genomeAnnos;
    private List<PersistantTrack> selectedTracks;
    private Map<Integer, CollectCoverageData> collectCoverageDataInstances;
    private int refGenomeID;
    private List<ResultDeAnalysis> results;
    private List<de.cebitec.readXplorer.util.Observer> observerList = new ArrayList<>();
    private File saveFile = null;
    private List<FeatureType> selectedFeatures;
    private Map<Integer, Map<PersistantFeature, Integer>> allCountData = new HashMap<>();
    private int resultsReceivedBack = 0;
    private int startOffset;
    private int stopOffset;
    private boolean regardReadOrientation;
    public static boolean TESTING_MODE = false;
    private final ParametersReadClasses readClassParams;

    public static enum Tool {

        ExpressTest("Express Test"), DeSeq("DESeq"), BaySeq("baySeq");

        private Tool(String stringRep) {
            this.stringRep = stringRep;
        }
        private String stringRep;

        @Override
        public String toString() {
            return stringRep;
        }

        public static Tool[] usableTools() {
            if (GnuR.SecureGnuRInitiliser.isGnuRSetUpCorrect() && GnuR.SecureGnuRInitiliser.isGnuRInstanceFree()) {
                return Tool.values();
            } else {
                Tool[] ret = new Tool[1];
                ret[0] = ExpressTest;
                return ret;
            }
        }
    }

    public static enum AnalysisStatus {

        RUNNING, FINISHED, ERROR;
    }

    public DeAnalysisHandler(List<PersistantTrack> selectedTracks, int refGenomeID,
            File saveFile, List<FeatureType> selectedFeatures, int startOffset, int stopOffset,
            ParametersReadClasses readClassParams, boolean regardReadOrientation) {
        ProcessingLog.getInstance().resetLog();
        this.selectedTracks = selectedTracks;
        this.refGenomeID = refGenomeID;
        this.saveFile = saveFile;
        this.selectedFeatures = selectedFeatures;
        this.startOffset = startOffset;
        this.stopOffset = stopOffset;
        this.readClassParams = readClassParams;
        this.regardReadOrientation = regardReadOrientation;
    }

    /**
     * Acutally starts the differential gene expression analysis.
     */
    private void startAnalysis() {
        collectCoverageDataInstances = new HashMap<>();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Starting to collect the necessary data for the differential gene expression analysis.", currentTimestamp);
        referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenomeID);
        List<AnalysesHandler> allHandler = new ArrayList<>();
        genomeAnnos = new ArrayList<>();
        
        for (PersistantChromosome chrom : referenceConnector.getRefGenome().getChromosomes().values()) {
            genomeAnnos.addAll(referenceConnector.getFeaturesForRegion(1, chrom.getLength(), selectedFeatures, chrom.getId()));
        }
        
        for (Iterator<PersistantTrack> it = selectedTracks.iterator(); it.hasNext();) {
            
            PersistantTrack currentTrack = it.next();
            try {
                TrackConnector tc = (new SaveTrackConnectorFetcherForGUI()).getTrackConnector(currentTrack);

                CollectCoverageData collCovData = new CollectCoverageData(genomeAnnos, startOffset, stopOffset, regardReadOrientation);
                collectCoverageDataInstances.put(currentTrack.getId(), collCovData);
                AnalysesHandler handler = new AnalysesHandler(tc, this, "Collecting coverage data for track "
                        + currentTrack.getDescription() + ".", readClassParams);
                handler.setMappingsNeeded(true);
                handler.setDesiredData(Properties.REDUCED_MAPPINGS);
                handler.registerObserver(collCovData);
                allHandler.add(handler);
            } catch (SaveTrackConnectorFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
                JOptionPane.showMessageDialog(null, "The path of one of the selected tracks could not be resolved. The analysis will be canceled now.", "Error resolving path to track", JOptionPane.INFORMATION_MESSAGE);
                ProcessingLog.getInstance().addProperty("Unresolved track", currentTrack);
                notifyObservers(AnalysisStatus.ERROR);
                this.interrupt();
                return;
            }
        }
        
        for (Iterator<AnalysesHandler> it = allHandler.iterator(); it.hasNext();) {
            AnalysesHandler handler = it.next();
            handler.startAnalysis();
        }
    }

    protected void prepareFeatures(DeAnalysisData analysisData) {
        analysisData.setFeatures(genomeAnnos);
        analysisData.setSelectedTracks(selectedTracks);
    }

    protected void prepareCountData(DeAnalysisData analysisData, Map<Integer, Map<PersistantFeature, Integer>> allCountData) {
        for (Iterator<PersistantTrack> it = selectedTracks.iterator(); it.hasNext();) {
            Integer key = it.next().getId();
            Integer[] data = new Integer[getPersAnno().size()];
            Map<PersistantFeature, Integer> currentTrack = allCountData.get(key);
            int j = 0;
            for (Iterator<PersistantFeature> it1 = getPersAnno().iterator(); it1.hasNext(); j++) {
                PersistantFeature persistantFeature = it1.next();
                data[j] = currentTrack.get(persistantFeature);
            }
            analysisData.addCountDataForTrack(data);
        }
        //Kill all the references to allCountData...
        allCountData = null;
        //...so the GC will clean them up and free lots of memory.
        System.gc();
    }

    /**
     * When all countData is collected this method is called and the processing
     * with the tool corresponding to the implementing class should start.
     *
     * @return
     */
    protected abstract List<ResultDeAnalysis> processWithTool() throws PackageNotLoadableException,
            JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException;

    /**
     * This is the final Method which is called when all windows associated with
     * the analysis are closed. So you should clean up everything and release
     * the Gnu R instance at this point.
     */
    public abstract void endAnalysis();

    public void setResults(List<ResultDeAnalysis> results) {
        this.results = results;
    }

    public Map<Integer, Map<PersistantFeature, Integer>> getAllCountData() {
        return allCountData;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public List<PersistantFeature> getPersAnno() {
        return genomeAnnos;
    }

    public List<PersistantTrack> getSelectedTracks() {
        return selectedTracks;
    }

    public List<ResultDeAnalysis> getResults() {
        return results;
    }

    public Map<Integer, CollectCoverageData> getCollectCoverageDataInstances() {
        return collectCoverageDataInstances;
    }

    @Override
    public void run() {
        notifyObservers(AnalysisStatus.RUNNING);
        startAnalysis();
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observerList.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observerList.remove(observer);
        if (this.observerList.isEmpty()) {
            endAnalysis();
            this.interrupt();
        }
    }

    @Override
    public void notifyObservers(Object data) {
        //Copy the observer list to avoid concurrent modification exception
        List<Observer> tmpObserver = new ArrayList<>(observerList);
        for (Iterator<Observer> it = tmpObserver.iterator(); it.hasNext();) {
            Observer currentObserver = it.next();
            currentObserver.update(data);
        }
    }

    @Override
    public synchronized void showData(Object data) {
        Pair<Integer, String> res = (Pair<Integer, String>) data;
        allCountData.put(res.getFirst(), getCollectCoverageDataInstances().get(res.getFirst()).getCountData());

        if (++resultsReceivedBack == getCollectCoverageDataInstances().size()) {
            try {
                results = processWithTool();
            } catch (PackageNotLoadableException | UnknownGnuRException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp);
                notifyObservers(AnalysisStatus.ERROR);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Gnu R Error", JOptionPane.WARNING_MESSAGE);
                this.interrupt();
            } catch (IllegalStateException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "{0}: " + ex.getMessage(), currentTimestamp);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Gnu R Error", JOptionPane.WARNING_MESSAGE);
            } catch (JRILibraryNotInPathException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "{0}: " + ex.getMessage(), currentTimestamp);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Gnu R Error", JOptionPane.WARNING_MESSAGE);
            }
            notifyObservers(AnalysisStatus.FINISHED);
        }
    }

    /**
     * @return Id of the reference, for which the analysis is carried out.
     */
    public int getRefGenomeID() {
        return refGenomeID;
    }    
}
