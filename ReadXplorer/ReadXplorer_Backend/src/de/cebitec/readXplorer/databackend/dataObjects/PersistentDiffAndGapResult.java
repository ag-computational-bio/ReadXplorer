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

import java.util.List;
import java.util.Map;

/**
 * A diff and gap result data storage for persistent objects for one mapping.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class PersistentDiffAndGapResult {
    private int errors;
    private Map<Integer, Integer> gapOrderIndex;
    private List<Difference> diffs;
    private List<PersistentReferenceGap> gaps;

    /**
     * A diff and gap result data storage for persistent objects for one mapping.
     * @param diffs list of diffs in a certain interval
     * @param gaps list of gaps in a certain interval
     * @param gapOrderIndex order of the gaps
     * @param errors total number of differences to the reference
     */
    public PersistentDiffAndGapResult(List<Difference> diffs, List<PersistentReferenceGap> gaps, Map<Integer, Integer> gapOrderIndex, int errors) {
        this.diffs = diffs;
        this.gaps = gaps;
        this.gapOrderIndex = gapOrderIndex;
        this.errors = errors;
    }

    /**
     * @return the diffs of one mapping.
     */
    public List<Difference> getDiffs() {
        return diffs;
    }

    /**
     * @return the gaps of one mapping
     */
    public List<PersistentReferenceGap> getGaps() {
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
