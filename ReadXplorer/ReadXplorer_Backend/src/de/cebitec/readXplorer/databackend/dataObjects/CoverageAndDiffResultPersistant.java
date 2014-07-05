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

import de.cebitec.readXplorer.databackend.IntervalRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Able to store the result for coverage, diffs and gaps. Called persistant, because
 * it needs the persistant data types from its own package.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoverageAndDiffResultPersistant extends PersistantResult implements Serializable {
    
    /** important: this id is used, when saving a CoverageAndDiffResultPersistant in the objectcache
     * raise this number, if any change to the class structure will be made in the future */
    public static final long serialVersionUID = 42L;
    
    private PersistantCoverage coverage;
    private PersistantCoverage readStarts;
    private List<PersistantDiff> diffs;
    private List<PersistantReferenceGap> gaps;
    
    /**
     * Data storage for coverage, diffs and gaps.
     * @param coverage the coverage container to store. If it is not used, you can
     *      add an empty coverage container.
     * @param diffs the list of diffs to store, if they are not used, you can add null
     *      or an empty list.
     * @param gaps the list of gaps to store, if they are not use, you can add null
     *      or an empty list
     * @param request the interval request for which this result was generated
     */
    public CoverageAndDiffResultPersistant(PersistantCoverage coverage, List<PersistantDiff> diffs, List<PersistantReferenceGap> gaps, 
            IntervalRequest request) {
        super(request);
        this.coverage = coverage;
        this.readStarts = null;
        this.diffs = diffs;
        this.gaps = gaps;
    }
    
    /**
     * @return the diffs, if they are stored. If they are not, 
     * the list is empty.
     */
    public List<PersistantDiff> getDiffs() {
        if (this.diffs != null) {
            return this.diffs;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * @return the gaps, if they are stored. If they are not, 
     * the list is empty.
     */
    public List<PersistantReferenceGap> getGaps() {
        if (this.gaps != null) {
            return this.gaps;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * @return the coverage container, if it is stored. If it is not, 
     * it returns an empty coverage container covering only 0.
     */
    public PersistantCoverage getCoverage() {
        if (this.coverage != null) {
            return this.coverage;
        } else {
            return new PersistantCoverage(0, 0);
        }
    }

    /**
     * @return the coverage object containing only the read start counts.
     */
    public PersistantCoverage getReadStarts() {
        return readStarts;
    }

    /**
     * @param readStarts The coverage object containing only the read start counts.
     */
    public void setReadStarts(PersistantCoverage readStarts) {
        this.readStarts = readStarts;
    }       
}
