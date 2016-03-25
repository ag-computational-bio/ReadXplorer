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

package de.cebitec.readxplorer.databackend.dataobjects;


import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * A diff and gap result data storage for persistent objects for one mapping.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class DiffAndGapResult {

    private final int errors;
    private final Map<Integer, Integer> gapOrderIndex;
    private final List<Difference> diffs;
    private final List<ReferenceGap> gaps;


    /**
     * A diff and gap result data storage for persistent objects for one
     * mapping.
     * <p>
     * @param diffs         list of diffs in a certain interval
     * @param gaps          list of gaps in a certain interval
     * @param gapOrderIndex order of the gaps
     * @param errors        total number of differences to the reference
     */
    public DiffAndGapResult( List<Difference> diffs, List<ReferenceGap> gaps, Map<Integer, Integer> gapOrderIndex, int errors ) {
        this.diffs = diffs;
        this.gaps = gaps;
        this.gapOrderIndex = gapOrderIndex;
        this.errors = errors;
    }


    /**
     * @return the diffs of one mapping.
     */
    public List<Difference> getDiffs() {
        return Collections.unmodifiableList( diffs );
    }


    /**
     * @return the gaps of one mapping
     */
    public List<ReferenceGap> getGaps() {
        return Collections.unmodifiableList( gaps );
    }


    /**
     * @return the gap order index belonging to the gaps of the mapping to which
     *         this object belongs
     */
    public Map<Integer, Integer> getGapOrderIndex() {
        return Collections.unmodifiableMap( gapOrderIndex );
    }


    /**
     * @return the number of differences the mapping, to which this object
     *         belongs,
     *         has.
     */
    public int getErrors() {
        return errors;
    }


}
