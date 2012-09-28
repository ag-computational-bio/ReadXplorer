package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Observable;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public abstract class AnalysisHandler extends Thread implements Observable {

    private ReferenceConnector referenceConnector;
    private int genomeSize;
    private List<PersistantAnnotation> persAnno;
    private List<PersistantTrack> selectedTraks;
    private Integer refGenomeID;
    private List<Result> results;
    private List<de.cebitec.vamp.util.Observer> observer = new ArrayList<>();
    private File saveFile = null;
    public static boolean TESTING_MODE = false;

    public static enum Tool {

        DeSeq, BaySeq
    }

    public AnalysisHandler(List<PersistantTrack> selectedTraks, Integer refGenomeID, File saveFile) {
        this.selectedTraks = selectedTraks;
        this.refGenomeID = refGenomeID;
        this.saveFile = saveFile;
    }

    protected Map<Integer, Map<Integer, Integer>> collectCountData() {
        Map<Integer, Map<Integer, Integer>> allCountData = new HashMap<>();
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Starting to collect the necessary data for the differential expression analysis.", currentTimestamp);
        referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenomeID);
        genomeSize = referenceConnector.getRefGen().getSequence().length();
        persAnno = referenceConnector.getAnnotationsForRegion(1, genomeSize);
        for (Iterator<PersistantTrack> it = selectedTraks.iterator(); it.hasNext();) {
            PersistantTrack currentTrack = it.next();
            CollectCoverageData collCovData = new CollectCoverageData(currentTrack.getId(), this);
            allCountData.put(currentTrack.getId(), collCovData.startCollecting());
        }
        return allCountData;
    }

    protected void prepareAnnotations(AnalysisData analysisData) {
        int[] annotationsStart = new int[getPersAnno().size()];
        int[] annotationsStop = new int[getPersAnno().size()];
        String[] loci = new String[getPersAnno().size()];
        int i = 0;
        for (Iterator<PersistantAnnotation> it = getPersAnno().iterator(); it.hasNext(); i++) {
            PersistantAnnotation persistantAnnotation = it.next();
            annotationsStart[i] = persistantAnnotation.getStart();
            annotationsStop[i] = persistantAnnotation.getStop();
            loci[i] = persistantAnnotation.getLocus();
        }

        analysisData.setStart(annotationsStart);
        analysisData.setStop(annotationsStop);
        analysisData.setLoci(loci);
        analysisData.setSelectedTraks(selectedTraks);
    }

    protected void prepareCountData(AnalysisData analysisData, Map<Integer, Map<Integer, Integer>> allCountData) {
        for (Iterator<PersistantTrack> it = selectedTraks.iterator(); it.hasNext();) {
            Integer key = it.next().getId();
            Integer[] data = new Integer[getPersAnno().size()];
            Map<Integer, Integer> currentTrack = allCountData.get(key);
            int j = 0;
            for (Iterator<PersistantAnnotation> it1 = getPersAnno().iterator(); it1.hasNext(); j++) {
                PersistantAnnotation persistantAnnotation = it1.next();
                data[j] = currentTrack.get(persistantAnnotation.getId());
            }
            analysisData.addCountDataForTrack(data);
        }
        //Kill all the references to allCountData...
        allCountData = null;
        //...so the GC will clean them up and free lots of memory.
        System.gc();
    }

    /**
     * All steps necessary for the analysis. This Method is called when start()
     * is calles on the instance of this class.
     */
    public abstract void performAnalysis();

    /**
     * This is the final Method which is called when all windows associated with
     * the analysis are closed. So you should clean up everything and release
     * the Gnu R instance at this point.
     */
    public abstract void endAnalysis();

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public int getGenomeSize() {
        return genomeSize;
    }

    public Integer getRefGenomeID() {
        return refGenomeID;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public List<PersistantAnnotation> getPersAnno() {
        return persAnno;
    }

    public List<PersistantTrack> getSelectedTraks() {
        return selectedTraks;
    }

    public List<Result> getResults() {
        return results;
    }

    @Override
    public void run() {
        super.run();
        performAnalysis();
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
        for (Iterator<de.cebitec.vamp.util.Observer> it = observer.iterator(); it.hasNext();) {
            de.cebitec.vamp.util.Observer currentObserver = it.next();
            currentObserver.update(data);
        }
    }

    public static class Result {

        private RVector rawTableContents;
        private Object[][] tableContents = null;
        private REXP raqColNames;
        private Object[] colNames = null;
        private REXP rawRowNames;
        private Object[] rowNames = null;

        public Result(RVector tableContents, REXP colnames, REXP rownames) {
            rawTableContents = tableContents;
            raqColNames = colnames;
            rawRowNames = rownames;
        }

        public Object[][] getTableContents() {
            if (tableContents == null) {
                tableContents = convertRresults(rawTableContents);
            }
            return tableContents;
        }

        public Object[] getColnames() {
            if (colNames == null) {
                colNames = convertNames(raqColNames);
            }
            return colNames;
        }

        public Object[] getRownames() {
            if (rowNames == null) {
                rowNames = convertNames(rawRowNames);
            }
            return rowNames;
        }

        private Object[] convertNames(REXP currentValues) {
            int currentType = currentValues.getType();
            Object[] current = null;;
            switch (currentType) {
                case REXP.XT_ARRAY_DOUBLE:
                    double[] currentDoubleValues = currentValues.asDoubleArray();
                    if (current == null) {
                        current = new Object[currentDoubleValues.length];
                    }
                    for (int j = 0; j < currentDoubleValues.length; j++) {
                        current[j] = currentDoubleValues[j];
                    }
                    break;
                case REXP.XT_ARRAY_INT:
                    int[] currentIntValues = currentValues.asIntArray();
                    if (current == null) {
                        current = new Object[currentIntValues.length];
                    }
                    for (int j = 0; j < currentIntValues.length; j++) {
                        current[j] = currentIntValues[j];
                    }
                    break;
                case REXP.XT_ARRAY_STR:
                    String[] currentStringValues = currentValues.asStringArray();
                    if (current == null) {
                        current = new Object[currentStringValues.length];
                    }
                    for (int j = 0; j < currentStringValues.length; j++) {
                        current[j] = currentStringValues[j];
                    }
                    break;
                case REXP.XT_ARRAY_BOOL_INT:
                    int[] currentBoolValues = currentValues.asIntArray();
                    if (current == null) {
                        current = new Object[currentBoolValues.length];
                    }
                    for (int j = 0; j < currentBoolValues.length; j++) {
                        if (currentBoolValues[j] == 1) {
                            current[j] = true;
                        } else {
                            current[j] = false;
                        }
                    }
                    break;
                case REXP.XT_FACTOR:
                    RFactor factor = currentValues.asFactor();
                    if (current == null) {
                        current = new Object[factor.size()];
                    }
                    for (int j = 0; j < factor.size(); j++) {
                        current[j] = factor.at(j);
                    }
                    break;

            }
            return current;
        }

        private Object[][] convertRresults(RVector currentRVector) {
            Object[][] current = null;
            for (int i = 0; i < currentRVector.size(); i++) {
                REXP currentValues = currentRVector.at(i);

                Object[] converted = convertNames(currentValues);
                if (current == null) {
                    current = new Object[converted.length][currentRVector.size()];
                }
                for (int j = 0; j < converted.length; j++) {
                    current[j][i] = converted[j];
                }
            }
            return current;
        }
    }
}
