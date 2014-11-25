package de.cebitec.readXplorer.parser.common;

import java.util.List;

/**
 * Stores the parsed results for diffs and gaps for one mapping.
 *
 * @author jwinneba, rhilker
 */
public class DiffAndGapResult {

    private int differences;
    private List<ParsedDiff> diffs;
    private List<ParsedReferenceGap> gaps;

    /**
     * Stores the parsed results for diffs and gaps for one mapping.
     * @param diffs differences to the reference
     * @param gaps gaps in the reference (insertions of the read)
     * @param differences number of differences between the read and the reference
     */
    public DiffAndGapResult(List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps, int differences) {
        this.diffs = diffs;
        this.gaps = gaps;
        this.differences = differences;
    }

    /**
     * @return differences to the reference
     */
    public List<ParsedDiff> getDiffs() {
        return diffs;
    }

    /**
     * @return gaps in the reference (insertions of the read)
     */
    public List<ParsedReferenceGap> getGaps() {
        return gaps;
    }

    /**
     * @return number of differences between the read and the reference
     */
    public int getDifferences() {
        return differences;
    }
}
