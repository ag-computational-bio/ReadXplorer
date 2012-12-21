package de.cebitec.vamp.databackend.dataObjects;

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
    
    private PersistantCoverage coverage;
    private List<PersistantDiff> diffs;
    private List<PersistantReferenceGap> gaps;
    private boolean diffsAndGapsUsed;

    /**
     * Data storage for coverage, diffs and gaps.
     * @param coverage the coverage container to store. If it is not used, you can
     *      add null or an empty coverage container.
     * @param diffs the list of diffs to store, if they are not used, you can add null
     *      or an empty list.
     * @param gaps the list of gaps to store, if they are not use, you can add null
     *      or an empty list
     * @param diffsAndGapsUsed true, if this is a result from querying also diffs and gaps
     * @param lowerBound the lower bound of the requested interval
     * @param upperBound the upper bound of the requested interval
     */
    public CoverageAndDiffResultPersistant(PersistantCoverage coverage, List<PersistantDiff> diffs, List<PersistantReferenceGap> gaps, 
            boolean diffsAndGapsUsed, int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        this.coverage = coverage;
        this.diffs = diffs;
        this.gaps = gaps;
        this.diffsAndGapsUsed = diffsAndGapsUsed;
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
     * @return true, if diffs and gaps were querried, false, if not
     */
    public boolean isDiffsAndGapsUsed() {
        return this.diffsAndGapsUsed;
    }       
}
