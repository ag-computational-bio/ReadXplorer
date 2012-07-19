package de.cebitec.vamp.differentialExpression;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Holds all the data necessary to carry out the analysis with baySeq.
 *
 * @author kstaderm
 */
public class BaySeqAnalysisData {

    /**
     * Start positions of the reference annotations.
     */
    private int[] start;
    /**
     * Stop positions of the reference annotations.
     */
    private int[] stop;
    /**
     * ID of the reference annotations.
     */
    private String[] loci;
    /**
     * Contains the count data for all the tracks. The first Integer array
     * represents the count data for the selected track with the lowest id. The
     * secound Integer array holds the count data for the selected track with
     * the secound lowest id an so on.
     */
    private Queue<Integer[]> countData;
    /**
     * The groups which should be taken into account by the analysis step.
     */
    private List<Group> groups;
    /**
     * The replicate structure of the selected tracks.
     */
    private int[] replicateStructure;

    /**
     * Creates a new instance of the BaySeqAnalysisData class.
     * @param start Start positions of the reference annotations.
     * @param stop Stop positions of the reference annotations.
     * @param capacity Number of selected tracks.
     * @param groups The groups which should be taken into account by the analysis step.
     * @param replicateStructure The replicate structure of the selected tracks.
     */
    public BaySeqAnalysisData(int[] start, int[] stop, String[] loci, int capacity, List<Group> groups, int[] replicateStructure) {
        this.start = start;
        this.stop = stop;
        this.loci = loci;
        countData = new ArrayBlockingQueue<>(capacity);
        this.groups = groups;
        this.replicateStructure = replicateStructure;
    }

    /**
     * Adds count data as an Integer array to a Queue holding all count data
     * necessary for the analysis. The data must be added in an ascending order
     * starting with the count data belonging to the track with the lowest ID.
     * @param data count data
     */
    public void addCountDataForTrack(Integer[] data) {
        countData.add(data);
    }

    /**
     * Return the first count data value on the Queue and removes it. So this
     * method will give you back the cound data added bei the @see addCountDataForTrack()
     * method. The count data added first will also be the first this method returns.
     * This method also converts the count data from an Integer array to an int
     * array so that they can be handed over to Gnu R directly.
     * @return count data as int[]
     */
    public int[] pollFirstCountData() {
        Integer[] cdata = countData.poll();
        int[] ret = new int[cdata.length];
        for (int i = 0; i < cdata.length; i++) {
            ret[i] = cdata[i].intValue();
        }
        return ret;
    }

    /**
     * Checks if there is still count data on the Queue
     * @return true if there is at least on count data on the Queue or false if it is empty.
     */
    public boolean hasCountData() {
        if (countData.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Return the start positions of the reference annotations.
     * @return Start positions of the reference annotations.
     */
    public int[] getStart() {
        return start;
    }

    /**
     * Return the stop positions of the reference annotations.
     * @return stop positions of the reference annotations.
     */
    public int[] getStop() {
        return stop;
    }
    /**
     * Return the Loci of the reference annotations.
     * @return Loci of the reference annotations as an String Array.
     */
    public String[] getLoci() {
        return loci;
    }
    
    private int nextGroup = 0;

    /**
     * Returns the next group that has not been returned yet.
     * @return the next unreturned group.
     */
    public int[] getNextGroup() {
        int[] ret = new int[0];
        if (!(nextGroup >= groups.size())) {
            Integer[] current = groups.get(nextGroup++).getIntegerRepresentation();
            ret = new int[current.length];
            for (int i = 0; i < current.length; i++) {
                ret[i] = current[i].intValue();
            }
        }
        return ret;
    }

    /**
     * Checks if there is still an unreturned group.
     * @return true if there is still at least one unreturned group otherwise false
     */
    public boolean hasGroups() {
        return !(nextGroup >= groups.size());
    }

    /**
     * Return the replicate structure.
     * @return int array representing the replicate structure of the data.
     */
    public int[] getReplicateStructure() {
        return replicateStructure;
    }
}