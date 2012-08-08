package de.cebitec.vamp.differentialExpression;

import java.util.List;

/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisData extends AnalysisData {

    /**
     * The groups which should be taken into account by the analysis step.
     */
    private List<Group> groups;
    /**
     * The replicate structure of the selected tracks.
     */
    private int[] replicateStructure;

    public BaySeqAnalysisData(int capacity, List<Group> groups, int[] replicateStructure) {
        super(capacity);
        this.groups = groups;
        this.replicateStructure = replicateStructure;
    }
    private int nextGroup = 0;

    /**
     * Returns the next group that has not been returned yet.
     *
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
     *
     * @return true if there is still at least one unreturned group otherwise
     * false
     */
    public boolean hasGroups() {
        return !(nextGroup >= groups.size());
    }

    /**
     * Return the replicate structure.
     *
     * @return int array representing the replicate structure of the data.
     */
    public int[] getReplicateStructure() {
        return replicateStructure;
    }
}
