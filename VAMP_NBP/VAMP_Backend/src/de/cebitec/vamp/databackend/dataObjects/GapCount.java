package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * A GapCount is a data structure to store the base counts of a gap for each
 * gap order index. This means here we store consecutive gap information like
 * <br>"___"
 * <br>"AGC"
 * <br>"012" <- gap order index
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class GapCount {

    private static final int GAP_A = 0;
    private static final int GAP_C = 1;
    private static final int GAP_G = 2;
    private static final int GAP_T = 3;
    private static final int GAP_N = 4;
    private static final int NO_FIELDS = 5;
    
    private List<int[]> gapOrderCount; //The gap order count list containing the arrays for the base counts at each gap order index.

    /**
     * A GapCount is a data structure to store the base counts of a gap for each
     * gap order index. This means here we store consecutive gap information like 
     * <br>"___" - ref gaps
     * <br>"AGC" - read bases
     * <br>"012" - gap order index
     */
    public GapCount() {
        gapOrderCount = new ArrayList<>();
    }
    
    
    /**
     * Increases the count of the bases for the given gap. The gap order of
     * the gap is of course taken into consideration.
     * @param gap the gap whose base count shall be added
     */
    public void incCountFor(PersistantReferenceGap gap) {
        if (gapOrderCount.size() <= gap.getOrder()) {
            gapOrderCount.add(new int[NO_FIELDS]);
        }
        char base = gap.isForwardStrand() ? gap.getBase() : SequenceUtils.getDnaComplement(gap.getBase());
        int gapBaseIdx = this.getBaseInt(base);
        gapOrderCount.get(gap.getOrder())[gapBaseIdx] += gap.getCount();
    }

    /**
     * @return The gap order count list containing the arrays for the base
     * counts at each gap order index.
     */
    public List<int[]> getGapOrderCount() {
        return gapOrderCount;
    }
    
    /**
     * @param base the base whose integer value is needed
     * @return the integer value for the given base type
     */
    private int getBaseInt(char base) {

        int baseInt = 0;
        switch (base) {
            case 'A': baseInt = GAP_A; break;
            case 'C': baseInt = GAP_C; break;
            case 'G': baseInt = GAP_G; break;
            case 'T': baseInt = GAP_T; break;
            case 'N': baseInt = GAP_N; break;
        }

        return baseInt;
    }
}
