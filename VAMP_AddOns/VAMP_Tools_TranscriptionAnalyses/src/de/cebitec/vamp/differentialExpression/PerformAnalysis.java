package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
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
    private List<PersistantTrack> selectedTraks;
    private List<Integer[]> groups;
    private Tool tool;
    private Integer refGenomeID;
    private IprogressMonitor monitor;

    public static enum Tool {

        BaySeq, EdgeR
    }        

    public PerformAnalysis(Tool tool, List<PersistantTrack> selectedTraks, List<Integer[]> groups, Integer refGenomeID, IprogressMonitor monitor) {
        this.selectedTraks = selectedTraks;
        this.groups=groups;
        this.tool = tool;
        this.refGenomeID=refGenomeID;
        this.monitor=monitor;
    }

    private void startUp() {
        Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0}: Starting to collect the necessary data for the differential expression analysis.", currentTimestamp);
        referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenomeID);
        genomeSize = referenceConnector.getRefGen().getSequence().length();
        persAnno = referenceConnector.getAnnotationsForRegion(1, genomeSize);
        monitor.setProgress(10);
        Map<Integer, Map<Integer, Integer>> allCountData = new HashMap<Integer, Map<Integer, Integer>>();
        for (Iterator<PersistantTrack> it = selectedTraks.iterator(); it.hasNext();) {
            PersistantTrack currentTrack = it.next();
            CollectCoverageData collCovData = new CollectCoverageData(currentTrack.getId(), this);
            allCountData.put(currentTrack.getId(), collCovData.startCollecting());
        }
        monitor.setProgress(25);
        if (tool.equals(Tool.BaySeq)) {
            processWithBaySeq(allCountData);
        }
        monitor.setProgress(100);
    }

    private void processWithBaySeq(Map<Integer, Map<Integer, Integer>> allCountData) {
        BaySeqAnalysisData bseqData = prepareAnnotationsForBaySeq();
        bseqData = prepareDataForBaySeq(bseqData, allCountData);
        GnuR gnuR = new GnuR();
        gnuR.process(bseqData, persAnno.size(), selectedTraks.size());
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

        BaySeqAnalysisData ret = new BaySeqAnalysisData(annotationsStart, annotationsStop, selectedTraks.size(), groups);
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

    public List<PersistantAnnotation> getPersAnno() {
        return persAnno;
    }

    @Override
    public void run() {
        super.run();
        startUp();
    }
}
