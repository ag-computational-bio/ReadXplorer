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

package de.cebitec.readxplorer.tools.coverageanalysis.featureCoverageAnalysis;


import de.cebitec.readxplorer.databackend.ParameterSetI;
import de.cebitec.readxplorer.databackend.ParametersFeatureTypesAndReadClasses;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.Set;


/**
 * Data storage for all parameters associated with feature coverage analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetCoveredFeatures extends ParametersFeatureTypesAndReadClasses
        implements ParameterSetI<ParameterSetCoveredFeatures> {

    private int minCoveredPercent;
    private int minCoverageCount;
    private boolean getCoveredFeatures;


    /**
     * Data storage for all parameters associated with filtering features.
     * <p>
     * @param performFilterAnalysis true, if the filtering should be carries out
     * @param minCoveredPercent     the minimum percent of an feature that has
     *                              to be
     *                              covered with at least minCoverageCount reads at each position
     * @param minCoverageCount      the minimum coverage at a position to be
     *                              counted
     *                              as covered
     * @param getCoveredFeatures    <code>true</code> if the covered features
     *                              should be returned, <code>false</code> if the uncovered features should
     *                              be returned
     * @param readClassesParams     The parameter set for the used read classes
     *                              for
     *                              this analysis instance
     * @param selectedFeatureTypes  The set of selected feature types to use for
     *                              this analysis instance
     */
    public ParameterSetCoveredFeatures( int minCoveredPercent, int minCoverageCount, boolean getCoveredFeatures,
                                        ParametersReadClasses readClassesParams, Set<FeatureType> selectedFeatureTypes ) {
        super( selectedFeatureTypes, readClassesParams );
        this.minCoveredPercent = minCoveredPercent;
        this.minCoverageCount = minCoverageCount;
        this.getCoveredFeatures = getCoveredFeatures;
    }


    /**
     * @return the minimum percent of an feature that has to be covered with
     *         at least minCoverageCount reads at each position
     */
    public int getMinCoveredPercent() {
        return minCoveredPercent;
    }


    /**
     * Set the minimum percent of an feature that has to be covered with at
     * least minCoverageCount reads at each position
     * <p>
     * @param minCoverageCount the minimum percent of an feature that has to
     *                         be covered with at least minCoverageCount reads at each position
     */
    public void setMinCoveredPercent( int minCoveredPercent ) {
        this.minCoveredPercent = minCoveredPercent;
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
     * @return <code>true</code> if the covered features should be returned,
     *         <code>false</code> if the uncovered features should be returned
     */
    public boolean isGetCoveredFeatures() {
        return getCoveredFeatures;
    }


    /**
     * @param getCoveredFeatures <code>true</code> if the covered features
     *                           should be returned,
     *                           <code>false</code> if the uncovered features should be returned
     */
    public void setGetCoveredFeatures( boolean getCoveredFeatures ) {
        this.getCoveredFeatures = getCoveredFeatures;
    }


}
