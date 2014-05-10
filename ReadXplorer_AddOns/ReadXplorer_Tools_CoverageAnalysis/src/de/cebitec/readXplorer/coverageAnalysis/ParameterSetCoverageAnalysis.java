package de.cebitec.readXplorer.coverageAnalysis;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;

/**
 * Data storage for all parameters associated with the coverage analysis.
 *
 * @author Tobias Zimmermann, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class ParameterSetCoverageAnalysis implements ParameterSetI<ParameterSetCoverageAnalysis> {

    private int minCoverageCount;
    private boolean isSumCoverage;
    private boolean detectCoveredIntervals;
    private ParametersReadClasses readClassesParams;

    /**
     * Data storage for all parameters associated with the coverage analysis.
     * @param minCoverageCount the minimum coverage at a position to be counted
     * as covered
     * @param detectCoveredIntervals <tt>true</tt>, if the covered intervals
     * for the reference shall be detected, <tt>false</tt>, if the uncovered
     * intervals of the reference shall be detected
     * @param readClassesParams the read classes taken into account in this 
     * analysis
     * @param isSumCoverage <tt>true</tt>, coverage of both strands is summed
     * up for this analysis, <tt>false</tt>, if both strands are treated 
     * separately
     */
    public ParameterSetCoverageAnalysis(int minCoverageCount, boolean isSumCoverage, boolean detectCoveredIntervals, ParametersReadClasses readClassesParams) {
        this.minCoverageCount = minCoverageCount;
        this.isSumCoverage = isSumCoverage;
        this.detectCoveredIntervals = detectCoveredIntervals;
        this.readClassesParams = readClassesParams;
    }

    
    /**
     * @param readClassesParams the read classes, which shall be taken into 
     * account for this analysis
     */
    public void setReadClassesParams(ParametersReadClasses readClassesParams) {
        this.readClassesParams = readClassesParams;
    }

    
    /**
     * @return the read classes, which shall be taken into account for this 
     * analysis
     */
    public ParametersReadClasses getReadClassParams() {
        return readClassesParams;
    }

    /**
     * @return the minimum coverage at a position to be counted as covered
     */
    public int getMinCoverageCount() {
        return minCoverageCount;
    }

    /**
     * Sets the minimum coverage at a position to be counted as covered
     * @param minCoverageCount the minimum coverage at a position to be counted
     * as covered
     */
    public void setMinCoverageCount(int minCoverageCount) {
        this.minCoverageCount = minCoverageCount;
    }

    /**
     * @return <code>true</code> coverage of both strands is summed up for this
     * analysis, <code>false</code>, if both strands are treated separately.
     */
    public boolean isSumCoverageOfBothStrands() {
        return isSumCoverage;
    }

    /**
     * @param isSumCoverage <code>true</code>, if coverage of both strands is
     * summed up for this analysis, <code>false</code>, if both strands are
     * treated separately.
     */
    public void setIsSumCoverageOfBothStrands(boolean isSumCoverage) {
        this.isSumCoverage = isSumCoverage;
    }

    /**
     * @param detectCoveredIntervals <tt>true</tt>, if the covered intervals for
     * the reference shall be detected, <tt>false</tt>, if the uncovered
     * intervals of the reference shall be detected
     */
    public void setIsDetectCoveredIntervals(boolean detectCoveredIntervals) {
        this.detectCoveredIntervals = detectCoveredIntervals;
    }

    /**
     * @return <tt>true</tt>, if the covered intervals for the reference shall
     * be detected, <tt>false</tt>, if the uncovered intervals of the reference
     * shall be detected
     */
    public boolean isDetectCoveredIntervals() {
        return this.detectCoveredIntervals;
    }
}