package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Holds all the overlapping dataset between all analysis handlers.
 *
 * @author kstaderm
 */
public class DeAnalysisData {

    /**
     * Start positions of the reference features.
     */
    private int[] start;
    /**
     * Stop positions of the reference features.
     */
    private int[] stop;
    /**
     * ID of the reference features.
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
     * The tracks selected by the user to perform the analysis on.
     */
    private List<PersistantTrack> selectedTraks;
    
    /**
     * Track Descriptions. Each description just appears one time.
     */
    private String[] trackDescriptions;

    /**
     * Creates a new instance of the DeAnalysisData class.
     * @param capacity Number of selected tracks.
     */
    public DeAnalysisData(int capacity) {
        countData = new ArrayBlockingQueue<>(capacity);
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
     * Return the start positions of the reference features.
     * @return Start positions of the reference features.
     */
    public int[] getStart() {
        return start;
    }

    /**
     * Return the stop positions of the reference features.
     * @return stop positions of the reference features.
     */
    public int[] getStop() {
        return stop;
    }
    /**
     * Return the Loci of the reference features.
     * @return Loci of the reference features as an String Array.
     */
    public String[] getLoci() {
        return loci;
    }

    /**
     * Returns the tracks selected by the user to perform the analysis on.
     * @return List of PersistantTrack containing the selected tracks.
     */
    public List<PersistantTrack> getSelectedTraks() {
        return selectedTraks;
    }

    public String[] getTrackDescriptions() {
        return trackDescriptions;
    }

    public void setStart(int[] start) {
        this.start = start;
    }

    public void setStop(int[] stop) {
        this.stop = stop;
    }

    public void setLoci(String[] loci) {
        Set<String> tmpSet = new LinkedHashSet<>();
        int counter = 1;
        for (int i = 0; i < loci.length; i++) {
            if(!tmpSet.add(loci[i])){
                tmpSet.add(loci[i]+"_REPNR"+counter++);
            }
        }
        this.loci = tmpSet.toArray(new String[tmpSet.size()]);
    }

    public void setSelectedTraks(List<PersistantTrack> selectedTraks) {
        this.selectedTraks = selectedTraks;
        Set<String> tmpSet = new LinkedHashSet<>();
        int counter = 1;
        for (int i = 0; i < selectedTraks.size(); i++) {
            if(!tmpSet.add(selectedTraks.get(i).getDescription())){
                tmpSet.add(selectedTraks.get(i).getDescription()+"_"+counter++);
            }
        }
    }
}