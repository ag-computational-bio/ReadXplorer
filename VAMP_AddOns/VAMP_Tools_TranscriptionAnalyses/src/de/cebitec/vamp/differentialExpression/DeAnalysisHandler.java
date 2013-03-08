package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.AnalysesHandler;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.DataVisualisationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.GnuR.JRILibraryNotInPathException;
import de.cebitec.vamp.differentialExpression.GnuR.PackageNotLoadableException;
import de.cebitec.vamp.differentialExpression.GnuR.UnknownGnuRException;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Pair;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public abstract class DeAnalysisHandler extends Thread implements Observable, DataVisualisationI {

    private ReferenceConnector referenceConnector;
    private int genomeSize;
    private List<PersistantFeature> persAnno;
    private List<PersistantTrack> selectedTraks;
    private Map<Integer, CollectCoverageData> collectCoverageDataInstances;
    private Integer refGenomeID;
    private List<Result> results;
    private List<de.cebitec.vamp.util.Observer> observer = new ArrayList<>();
    private File saveFile = null;
    private FeatureType feature;
    private Map<Integer, Map<Integer, Integer>> allCountData = new HashMap<>();
    private int resultsReceivedBack = 0;
    public static boolean TESTING_MODE = false;

    public static enum Tool {

        SimpleTest("Simple Test"), DeSeq("DESeq"), BaySeq("baySeq");

        private Tool(String stringRep) {
            this.stringRep = stringRep;
        }
        private String stringRep;

        @Override
        public String toString() {
            return stringRep;
        }
    }

    public static enum AnalysisStatus {

        RUNNING, FINISHED, ERROR;
    }

    public DeAnalysisHandler(List<PersistantTrack> selectedTraks, Integer refGenomeID, File saveFile, FeatureType feature) {
        this.selectedTraks = selectedTraks;
        this.refGenomeID = refGenomeID;
        this.saveFile = saveFile;
        this.feature = feature;
    }

    private void startAnalysis() {
        collectCoverageDataInstances = new HashMap<>();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Starting to collect the necessary data for the differential expression analysis.", currentTimestamp);
        referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenomeID);
        genomeSize = referenceConnector.getRefGenome().getSequence().length();
        persAnno = referenceConnector.getFeaturesForRegion(1, genomeSize, feature);
        for (Iterator<PersistantTrack> it = selectedTraks.iterator(); it.hasNext();) {
            PersistantTrack currentTrack = it.next();
            TrackConnector connector = ProjectConnector.getInstance().getTrackConnector(currentTrack);
            CollectCoverageData collCovData = new CollectCoverageData(persAnno);
            collectCoverageDataInstances.put(currentTrack.getId(), collCovData);
            AnalysesHandler handler = new AnalysesHandler(connector, this, "Collecting coverage data of track number "+currentTrack.getId()+".");
            handler.setReducedMappingsNeeded(true);
            handler.registerObserver(collCovData);
            handler.startAnalysis();
        }
    }

    protected void prepareFeatures(DeAnalysisData analysisData) {
        int[] featuresStart = new int[getPersAnno().size()];
        int[] featuresStop = new int[getPersAnno().size()];
        String[] loci = new String[getPersAnno().size()];
        int i = 0;
        for (Iterator<PersistantFeature> it = getPersAnno().iterator(); it.hasNext(); i++) {
            PersistantFeature persistantFeature = it.next();
            featuresStart[i] = persistantFeature.getStart();
            featuresStop[i] = persistantFeature.getStop();
            loci[i] = persistantFeature.getLocus();
        }

        analysisData.setStart(featuresStart);
        analysisData.setStop(featuresStop);
        analysisData.setLoci(loci);
        analysisData.setSelectedTraks(selectedTraks);
    }

    protected void prepareCountData(DeAnalysisData analysisData, Map<Integer, Map<Integer, Integer>> allCountData) {
        for (Iterator<PersistantTrack> it = selectedTraks.iterator(); it.hasNext();) {
            Integer key = it.next().getId();
            Integer[] data = new Integer[getPersAnno().size()];
            Map<Integer, Integer> currentTrack = allCountData.get(key);
            int j = 0;
            for (Iterator<PersistantFeature> it1 = getPersAnno().iterator(); it1.hasNext(); j++) {
                PersistantFeature persistantFeature = it1.next();
                data[j] = currentTrack.get(persistantFeature.getId());
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
     * @return 
     */
    protected abstract List<Result> processWithTool() throws PackageNotLoadableException,
            JRILibraryNotInPathException, IllegalStateException, UnknownGnuRException;

    /**
     * This is the final Method which is called when all windows associated with
     * the analysis are closed. So you should clean up everything and release
     * the Gnu R instance at this point.
     */
    public abstract void endAnalysis();

    public abstract void saveResultsAsCSV(int selectedIndex, String path);

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public int getGenomeSize() {
        return genomeSize;
    }

    public Integer getRefGenomeID() {
        return refGenomeID;
    }

    public Map<Integer, Map<Integer, Integer>> getAllCountData() {
        return allCountData;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public List<PersistantFeature> getPersAnno() {
        return persAnno;
    }

    public List<PersistantTrack> getSelectedTracks() {
        return selectedTraks;
    }

    public List<Result> getResults() {
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
    public void registerObserver(de.cebitec.vamp.util.Observer observer) {
        this.observer.add(observer);
    }

    @Override
    public void removeObserver(de.cebitec.vamp.util.Observer observer) {
        this.observer.remove(observer);
        if (this.observer.isEmpty()) {
            endAnalysis();
            this.interrupt();
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
        }
        notifyObservers(AnalysisStatus.FINISHED);
    }

    public static class Result {

        private String description;
        private RVector rawTableContents;
        private Vector<Vector> tableContents = null;
        private REXP rawColNames;
        private Vector colNames = null;
        private REXP rawRowNames;
        private Vector rowNames = null;

        public Result(RVector tableContents, REXP colnames, REXP rownames, String description) {
            rawTableContents = tableContents;
            rawColNames = colnames;
            rawRowNames = rownames;
            this.description = description;
        }

        public Vector<Vector> getTableContentsContainingRowNames() {
            Vector rnames = getRownames();
            Vector<Vector> data = getTableContents();
            for (int i = 0; i < rnames.size(); i++) {
                data.get(i).add(0, rnames.get(i));
            }
            return data;
        }

        public Vector<Vector> getTableContents() {
            if (tableContents == null) {
                tableContents = convertRresults(rawTableContents);
            }
            return tableContents;
        }

        public Vector getColnames() {
            if (colNames == null) {
                colNames = convertNames(rawColNames);
            }
            return colNames;
        }

        public Vector getRownames() {
            if (rowNames == null) {
                rowNames = convertNames(rawRowNames);
            }
            return rowNames;
        }

        public String getDescription() {
            return description;
        }

        /*
         * The manual array copy used in this method several times is intended!
         * This way the primitive data types are automatically converted to their 
         * corresponding Object presentation.
         */
        private Vector convertNames(REXP currentValues) {
            int currentType = currentValues.getType();
            Vector current = new Vector();
            switch (currentType) {
                case REXP.XT_ARRAY_DOUBLE:
                    double[] currentDoubleValues = currentValues.asDoubleArray();
                    for (int j = 0; j < currentDoubleValues.length; j++) {
                        current.add(currentDoubleValues[j]);
                    }
                    break;
                case REXP.XT_ARRAY_INT:
                    int[] currentIntValues = currentValues.asIntArray();
                    for (int j = 0; j < currentIntValues.length; j++) {
                        current.add(currentIntValues[j]);
                    }
                    break;
                case REXP.XT_ARRAY_STR:
                    String[] currentStringValues = currentValues.asStringArray();
                    for (int j = 0; j < currentStringValues.length; j++) {
                        current.add(currentStringValues[j]);
                    }
                    break;
                case REXP.XT_ARRAY_BOOL_INT:
                    int[] currentBoolValues = currentValues.asIntArray();
                    for (int j = 0; j < currentBoolValues.length; j++) {
                        if (currentBoolValues[j] == 1) {
                            current.add(true);
                        } else {
                            current.add(false);
                        }
                    }
                    break;
                case REXP.XT_FACTOR:
                    RFactor factor = currentValues.asFactor();
                    for (int j = 0; j < factor.size(); j++) {
                        current.add(factor.at(j));
                    }
                    break;

            }
            return current;
        }

        private Vector<Vector> convertRresults(RVector currentRVector) {
            Vector<Vector> current = new Vector<>();
            for (int i = 0; i < currentRVector.size(); i++) {
                REXP currentValues = currentRVector.at(i);
                Vector converted = convertNames(currentValues);

                for (int j = 0; j < converted.size(); j++) {
                    try {
                        current.get(j);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        current.add(new Vector());
                    }
                    current.get(j).add(converted.get(j));
                }
            }
            return current;
        }
    }
}
