package de.cebitec.vamp.differentialExpression;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisData {

    private int[] start;
    private int[] stop;
    private Queue<Integer[]> countData;
    private List<Integer[]> groups;

    public BaySeqAnalysisData(int[] start, int[] stop, int capacity, List<Integer[]> groups) {
        this.start = start;
        this.stop = stop;
        countData = new ArrayBlockingQueue<Integer[]>(capacity);
        this.groups = groups;
    }

    public void addCountDataForTrack(Integer[] data) {
        countData.add(data);
    }

    public int[] pollFirstCountData() {
        Integer[] cdata = countData.poll();
        int[] ret = new int[cdata.length];
        for (int i = 0; i < cdata.length; i++) {
            ret[i] = cdata[i].intValue();
        }
        return ret;
    }

    public boolean hasCountData() {
        if (countData.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public int[] getStart() {
        return start;
    }

    public int[] getStop() {
        return stop;
    }

    public List<Integer[]> getGroups() {
        return groups;
    }
}