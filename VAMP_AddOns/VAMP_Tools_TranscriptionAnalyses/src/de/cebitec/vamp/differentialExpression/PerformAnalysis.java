package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kstaderm
 */
public class PerformAnalysis extends Thread {

    private ReferenceConnector referenceConnector;
    private int genomeSize;
    private List<PersistantAnnotation> persAnno;
    private List<Integer> trackIDs;
    private Tool tool;

    public static enum Tool {

        BaySeq, EdgeR
    }

    //For debugging and testing:
    public PerformAnalysis() {
        trackIDs = new ArrayList<Integer>();
        //trackIDs start at 1
        trackIDs.add(1);
        trackIDs.add(2);
//        trackIDs.add(3);
//        trackIDs.add(4);
        tool = Tool.BaySeq;
    }

    public PerformAnalysis(List<Integer> trackIDs, Tool tool) {
        this.trackIDs = trackIDs;
        this.tool = tool;
    }

    private void startUp() {
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Starting to collect the necessary data for the differential expression analysis.", currentTimestamp);
        referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(1);
        genomeSize = referenceConnector.getRefGen().getSequence().length();
        persAnno = referenceConnector.getAnnotationsForRegion(1, genomeSize);
        Map<Integer, Map<Integer, Integer>> allCountData = new HashMap<Integer, Map<Integer, Integer>>();
        for (Iterator<Integer> it = trackIDs.iterator(); it.hasNext();) {
            Integer trackID = it.next();
            CollectCoverageData collCovData = new CollectCoverageData(trackID, this);
            allCountData.put(trackID, collCovData.startCollecting());
        }
        if (tool.equals(Tool.BaySeq)) {
            processWithBaySeq(allCountData);
        }
    }

    private void processWithBaySeq(Map<Integer, Map<Integer, Integer>> allCountData) {
        BaySeqAnalysisData bseqData = prepareAnnotationsForBaySeq();
        bseqData = prepareDataForBaySeq(bseqData, allCountData);
        GnuR gnuR = new GnuR();
        gnuR.process(bseqData, persAnno.size(), trackIDs.size());
        gnuR.shutdown();
        System.out.println("FERTIG");
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

        BaySeqAnalysisData ret = new BaySeqAnalysisData(annotationsStart, annotationsStop, trackIDs.size());
        return ret;
    }

    private BaySeqAnalysisData prepareDataForBaySeq(BaySeqAnalysisData bSeqData, Map<Integer, Map<Integer, Integer>> allCountData) {
        for (Iterator<Integer> it = trackIDs.iterator(); it.hasNext();) {
            Integer key = it.next();
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

    public List<PersistantAnnotation> getPersAnno() {
        return persAnno;
    }

    @Override
    public void run() {
        super.run();
        startUp();
    }
}
