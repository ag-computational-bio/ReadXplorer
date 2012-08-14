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
    private List<Object[][]> results;
    private List<de.cebitec.vamp.util.Observer> observer = new ArrayList<>();
    private File saveFile = null;
    public static final boolean TESTING_MODE = true;

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
     * All steps necessary for the analysis.
     * This Method is called when start() is calles on the instance of this class.
     */
    public abstract void performAnalysis();
    
    /**
     * This is the final Method which is called when all windows associated with
     * the analysis are closed. So you should clean up everything and release the
     * Gnu R instance at this point.
     */
    public abstract void endAnalysis();

    public void setResults(List<Object[][]> results) {
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

    public List<Object[][]> getResults() {
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
        if(this.observer.isEmpty()){
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
}
