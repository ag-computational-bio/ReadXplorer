package de.cebitec.vamp.databackend.dataObjects;

import java.util.List;
import java.util.Map;

/**
 * A diff and gap result data storage for persistant objects for one mapping.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class PersistantDiffAndGapResult {
    private int errors;
    private Map<Integer, Integer> gapOrderIndex;
    private List<PersistantDiff> diffs;
    private List<PersistantReferenceGap> gaps;

    /**
     * A diff and gap result data storage for persistant objects for one mapping.
     * @param diffs list of diffs in a certain interval
     * @param gaps list of gaps in a certain interval
     * @param gapOrderIndex order of the gaps
     * @param errors total number of differences to the reference
     */
    public PersistantDiffAndGapResult(List<PersistantDiff> diffs, List<PersistantReferenceGap> gaps, Map<Integer, Integer> gapOrderIndex, int errors) {
        this.diffs = diffs;
        this.gaps = gaps;
        this.gapOrderIndex = gapOrderIndex;
        this.errors = errors;
    }

    /**
     * @return the diffs of one mapping.
     */
    public List<PersistantDiff> getDiffs() {
        return diffs;
    }

    /**
     * @return the gaps of one mapping
     */
    public List<PersistantReferenceGap> getGaps() {
        return gaps;
    }

    /**
     * @return the gap order index belonging to the gaps of the mapping to which this object belongs
     */
    public Map<Integer, Integer> getGapOrderIndex() {
        return gapOrderIndex;
    }

    /**
     * @return the number of differences the mapping, to which this object belongs,
     *      has.
     */
    public int getErrors() {
        return errors;
    }
}
