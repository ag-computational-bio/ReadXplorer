/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.util.SequenceUtils;
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
    /** 5 */
    private static final int NO_BASES = 5;
    /** 3 = Three values are needed: the count, the base  quality and the mapping quality. */
    private static final int NO_VALUES = 3;
    /** 0 = index for the count of a diff in all mappings at that position. */
    private static final int COUNT_IDX = 0;
    /** 1 = index for the average base quality of a diff base in all mappings for that position. */
    private static final int BASE_QUAL_IDX = 1;
    /** 2 = index for the average mapping quality of a diff base in all mappings for that position. */
    private static final int MAP_QUAL_IDX = 2;
    /** -1 = Default mapping quality value, if it is unknown according to SAM spec. */
    private static final int UNKNOWN_MAP_QUAL = -1;
    
    private List<int[][]> gapOrderCount; //The gap order count list containing the arrays for the base counts at each gap order index.

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
    public void incCountFor(ReferenceGap gap) {
        while (gapOrderCount.size() <= gap.getOrder()) {
            gapOrderCount.add(new int[NO_BASES][NO_VALUES]);
        }
        //filtered gaps appear as empty (=0) entries in the arrays
        char base = gap.isForwardStrand() ? gap.getBase() : SequenceUtils.getDnaComplement(gap.getBase());
        int gapBaseIdx = this.getBaseInt(base);
        gapOrderCount.get(gap.getOrder())[gapBaseIdx][COUNT_IDX] += gap.getCount();
        if (gap.getBaseQuality() > -1) {
            gapOrderCount.get(gap.getOrder())[gapBaseIdx][BASE_QUAL_IDX] += gap.getBaseQuality(); //can be -1 if unknown
        }
        if (gap.getMappingQuality() != UNKNOWN_MAP_QUAL) {
            gapOrderCount.get(gap.getOrder())[gapBaseIdx][MAP_QUAL_IDX] += gap.getMappingQuality(); //can be 255 if unknown
        }
    }

    /**
     * @return The gap order count list containing the arrays for the base
     * counts at each gap order index.
     */
    public List<int[][]> getGapOrderCount() {
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
