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

package de.cebitec.readxplorer.tools.coverageanalysis;


import de.cebitec.readxplorer.databackend.ParameterSetI;
import de.cebitec.readxplorer.databackend.ParameterSetWithReadClasses;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;


/**
 * Data storage for all parameters associated with the coverage analysis.
 *
 * @author Tobias Zimmermann, Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParameterSetCoverageAnalysis extends ParameterSetWithReadClasses
        implements ParameterSetI<ParameterSetCoverageAnalysis> {

    private int minCoverageCount;
    private boolean isSumCoverage;
    private boolean detectCoveredIntervals;


    /**
     * Data storage for all parameters associated with the coverage analysis.
     * <p>
     * @param minCoverageCount       the minimum coverage at a position to be
     *                               counted
     *                               as covered
     * @param detectCoveredIntervals <tt>true</tt>, if the covered intervals
     *                               for the reference shall be detected, <tt>false</tt>, if the uncovered
     *                               intervals of the reference shall be detected
     * @param readClassesParams      the read classes taken into account in this
     *                               analysis
     * @param isSumCoverage          <tt>true</tt>, coverage of both strands is
     *                               summed
     *                               up for this analysis, <tt>false</tt>, if both strands are treated
     *                               separately
     */
    public ParameterSetCoverageAnalysis( int minCoverageCount, boolean isSumCoverage, boolean detectCoveredIntervals, ParametersReadClasses readClassesParams ) {
        super( readClassesParams );
        this.minCoverageCount = minCoverageCount;
        this.isSumCoverage = isSumCoverage;
        this.detectCoveredIntervals = detectCoveredIntervals;
    }


    /**
     * @return the minimum coverage at a position to be counted as covered
     */
    public int getMinCoverageCount() {
        return minCoverageCount;
    }


    /**
     * Sets the minimum coverage at a position to be counted as covered
     * <p>
     * @param minCoverageCount the minimum coverage at a position to be counted
     *                         as covered
     */
    public void setMinCoverageCount( int minCoverageCount ) {
        this.minCoverageCount = minCoverageCount;
    }


    /**
     * @return <code>true</code> coverage of both strands is summed up for this
     *         analysis, <code>false</code>, if both strands are treated separately.
     */
    public boolean isSumCoverageOfBothStrands() {
        return isSumCoverage;
    }


    /**
     * @param isSumCoverage <code>true</code>, if coverage of both strands is
     *                      summed up for this analysis, <code>false</code>, if both strands are
     *                      treated separately.
     */
    public void setIsSumCoverageOfBothStrands( boolean isSumCoverage ) {
        this.isSumCoverage = isSumCoverage;
    }


    /**
     * @param detectCoveredIntervals <tt>true</tt>, if the covered intervals for
     *                               the reference shall be detected, <tt>false</tt>, if the uncovered
     *                               intervals of the reference shall be detected
     */
    public void setIsDetectCoveredIntervals( boolean detectCoveredIntervals ) {
        this.detectCoveredIntervals = detectCoveredIntervals;
    }


    /**
     * @return <tt>true</tt>, if the covered intervals for the reference shall
     *         be detected, <tt>false</tt>, if the uncovered intervals of the reference
     *         shall be detected
     */
    public boolean isDetectCoveredIntervals() {
        return this.detectCoveredIntervals;
    }


}
