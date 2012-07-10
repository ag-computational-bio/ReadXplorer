package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.util.Observable;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class PerformAnalysis extends Thread implements Observable {

    private ReferenceConnector referenceConnector;
    private int genomeSize;
    private List<PersistantAnnotation> persAnno;
    private List<PersistantTrack> selectedTraks;
    private List<Group> groups;
    private Tool tool;
    private Integer refGenomeID;
    private int[] replicateStructure;
    private List<Object[][]> results;
    private List<de.cebitec.vamp.util.Observer> observer = new ArrayList<>();
    private GnuR gnuR;
    private File saveFile = null;
    public static final boolean TESTING_MODE = false;

    public static enum Tool {

        BaySeq, EdgeR, DeSeq
    }

    public static enum Plot {

        Priors, MACD, Posteriors
    }

    public PerformAnalysis(Tool tool, List<PersistantTrack> selectedTraks, List<Group> groups, Integer refGenomeID, int[] replicateStructure) {
        this.selectedTraks = selectedTraks;
        this.groups = groups;
        this.tool = tool;
        this.refGenomeID = refGenomeID;
        this.replicateStructure = replicateStructure;
    }

    public PerformAnalysis(Tool tool, List<PersistantTrack> selectedTraks, List<Group> groups, Integer refGenomeID, int[] replicateStructure, File saveFile) {
        this.selectedTraks = selectedTraks;
        this.groups = groups;
        this.tool = tool;
        this.refGenomeID = refGenomeID;
        this.replicateStructure = replicateStructure;
        this.saveFile = saveFile;
    }

    private void startUp() {
        Map<Integer, Map<Integer, Integer>> allCountData = new HashMap<>();
        if (!TESTING_MODE) {
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
        }
        if (tool.equals(Tool.BaySeq)) {
            results = processWithBaySeq(allCountData);
            notifyObservers(this);
        }
    }

    private List<Object[][]> processWithBaySeq(Map<Integer, Map<Integer, Integer>> allCountData) {
        BaySeqAnalysisData bseqData = null;
        if (!TESTING_MODE) {
            bseqData = prepareAnnotationsForBaySeq();
            bseqData = prepareDataForBaySeq(bseqData, allCountData);
        }
        gnuR = new GnuR();
        List<RVector> ret;
        if (!TESTING_MODE) {
            ret = gnuR.process(bseqData, persAnno.size(), selectedTraks.size(), saveFile);
        } else {
            //You must enter the number of annatations by hand for the
            //testing scenario.
            ret = gnuR.process(bseqData, 3232, selectedTraks.size(), saveFile);
        }
        return convertRresults(ret);
    }

    private List<Object[][]> convertRresults(List<RVector> results) {
        List<Object[][]> ret = new ArrayList<>();
        for (Iterator<RVector> it = results.iterator(); it.hasNext();) {
            RVector currentRVector = it.next();
            Object[][] current = new Object[currentRVector.at(0).asIntArray().length][currentRVector.size()];
            for (int i = 0; i < currentRVector.size(); i++) {
                double[] currentValues = currentRVector.at(i).asDoubleArray();
                for (int j = 0; j < currentValues.length; j++) {
                    current[j][i] = currentValues[j];
                }
            }
            ret.add(current);
        }
        return ret;
    }

    private BaySeqAnalysisData prepareAnnotationsForBaySeq() {
        int[] annotationsStart = new int[persAnno.size()];
        int[] annotationsStop = new int[persAnno.size()];
        int i = 0;
        for (Iterator<PersistantAnnotation> it = persAnno.iterator(); it.hasNext(); i++) {
            PersistantAnnotation persistantAnnotation = it.next();
            annotationsStart[i] = persistantAnnotation.getStart();
            annotationsStop[i] = persistantAnnotation.getStop();
        }

        BaySeqAnalysisData ret = new BaySeqAnalysisData(annotationsStart, annotationsStop, selectedTraks.size(), groups, replicateStructure);
        return ret;
    }

    private BaySeqAnalysisData prepareDataForBaySeq(BaySeqAnalysisData bSeqData, Map<Integer, Map<Integer, Integer>> allCountData) {
        for (Iterator<PersistantTrack> it = selectedTraks.iterator(); it.hasNext();) {
            Integer key = it.next().getId();
            Integer[] data = new Integer[persAnno.size()];
            Map<Integer, Integer> currentTrack = allCountData.get(key);
            int j = 0;
            for (Iterator<PersistantAnnotation> it1 = persAnno.iterator(); it1.hasNext(); j++) {
                PersistantAnnotation persistantAnnotation = it1.next();
                data[j] = currentTrack.get(persistantAnnotation.getId());
            }
            bSeqData.addCountDataForTrack(data);
        }
        //Kill all the references to allCountData...
        allCountData = null;
        //...so the GC will clean them up and free lots of memory.
        System.gc();
        return bSeqData;
    }

    public File plot(Plot plot, Group group, int[] samplesA, int[] samplesB) throws IOException {
        File file = File.createTempFile("VAMP_Plot_", ".svg");
        file.deleteOnExit();
        if (plot == Plot.MACD) {
            gnuR.plotMACD(file, samplesA, samplesB);
        }
        if (plot == Plot.Posteriors) {
            gnuR.plotPosteriors(file, group, samplesA, samplesB);
        }
        if (plot == Plot.Priors) {
            gnuR.plotPriors(file, group);
        }
        return file;
    }

    public List<PersistantAnnotation> getPersAnno() {
        return persAnno;
    }

    public List<PersistantTrack> getSelectedTraks() {
        return selectedTraks;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<Object[][]> getResults() {
        return results;
    }

    @Override
    public void run() {
        super.run();
        startUp();
    }

    @Override
    public void registerObserver(de.cebitec.vamp.util.Observer observer) {
        this.observer.add(observer);
    }

    @Override
    public void removeObserver(de.cebitec.vamp.util.Observer observer) {
        this.observer.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Iterator<de.cebitec.vamp.util.Observer> it = observer.iterator(); it.hasNext();) {
            de.cebitec.vamp.util.Observer currentObserver = it.next();
            currentObserver.update(data);
        }
    }
}
