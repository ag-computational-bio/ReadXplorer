package de.cebitec.vamp.parser.common;

import java.util.List;
import java.util.Map;

/**
 * Stores the parsed results for diffs and gaps for one mapping.
 *
 * @author jwinneba, rhilker
 */
public class DiffAndGapResult {

    private int errors;
    private Map<Integer, Integer> gapOrderIndex;
    private List<ParsedDiff> diffs;
    private List<ParsedReferenceGap> gaps;

    /**
     * Stores the parsed results for diffs and gaps for one mapping.
     */
    public DiffAndGapResult(List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps, Map<Integer, Integer> gapOrderIndex, int errors) {
        this.diffs = diffs;
        this.gaps = gaps;
        this.gapOrderIndex = gapOrderIndex;
        this.errors = errors;
    }

    public List<ParsedDiff> getDiffs() {
        return diffs;
    }

    public List<ParsedReferenceGap> getGaps() {
        return gaps;
    }

    public Map<Integer, Integer> getGapOrderIndex() {
        return gapOrderIndex;
    }

    public int getErrors() {
        return errors;
    }
}
