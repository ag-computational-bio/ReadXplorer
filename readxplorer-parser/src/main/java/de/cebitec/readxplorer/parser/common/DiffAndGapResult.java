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

package de.cebitec.readxplorer.parser.common;


import java.util.Collections;
import java.util.List;


/**
 * Stores the parsed results for diffs and gaps for one mapping.
 *
 * @author jwinneba, rhilker
 */
public class DiffAndGapResult {

    private final int differences;
    private final List<ParsedDiff> diffs;
    private final List<ParsedReferenceGap> gaps;


    /**
     * Stores the parsed results for diffs and gaps for one mapping.
     * <p>
     * @param diffs       differences to the reference
     * @param gaps        gaps in the reference (insertions of the read)
     * @param differences number of differences between the read and the
     *                    reference
     */
    public DiffAndGapResult( List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps, int differences ) {
        this.diffs = diffs;
        this.gaps = gaps;
        this.differences = differences;
    }


    /**
     * @return differences to the reference
     */
    public List<ParsedDiff> getDiffs() {
        return Collections.unmodifiableList( diffs );
    }


    /**
     * @return gaps in the reference (insertions of the read)
     */
    public List<ParsedReferenceGap> getGaps() {
        return Collections.unmodifiableList( gaps );
    }


    /**
     * @return number of differences between the read and the reference
     */
    public int getDifferences() {
        return differences;
    }


}
