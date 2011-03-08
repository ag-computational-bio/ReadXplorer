package de.cebitec.vamp.parser.common;

import java.util.List;

/**
 *
 * @author jwinneba
 */
public class DiffAndGapResult {

    private List<ParsedDiff> diffs;
    private List<ParsedReferenceGap> gaps;

    public DiffAndGapResult(List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps) {
        this.diffs = diffs;
        this.gaps = gaps;
    }

    public List<ParsedDiff> getDiffs() {
        return diffs;
    }

    public List<ParsedReferenceGap> getGaps() {
        return gaps;
    }

}
