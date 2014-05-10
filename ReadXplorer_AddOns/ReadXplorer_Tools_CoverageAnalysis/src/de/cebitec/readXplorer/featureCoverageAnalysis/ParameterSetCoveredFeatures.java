package de.cebitec.readXplorer.featureCoverageAnalysis;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.ParametersFeatureTypes;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.util.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with feature coverage analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetCoveredFeatures extends ParametersFeatureTypes implements ParameterSetI<ParameterSetCoveredFeatures> {
    
    private int minCoveredPercent;
    private int minCoverageCount;
    private boolean whateverStrand;
    private boolean getCoveredFeatures;
    private ParametersReadClasses readClassesParams;

    /**
     * Data storage for all parameters associated with filtering features.
     * @param performFilterAnalysis true, if the filtering should be carries out
     * @param minCoveredPercent the minimum percent of an feature that has to be
     * covered with at least minCoverageCount reads at each position
     * @param minCoverageCount the minimum coverage at a position to be counted
     * as covered
     * @param whateverStrand <tt>true</tt>, if the strand does not matter for
     * this analysis, false, if only mappings on the strand of the respective
     * feature should be considered.
     * @param getCoveredFeatures <code>true</code> if the covered features
     * should be returned, <code>false</code> if the uncovered features should
     * be returned
     * @param readClassesParams The parameter set for the used read classes for
     * this analysis instance
     * @param selectedFeatureTypes The set of selected feature types to use for
     * this analysis instance
     */
    public ParameterSetCoveredFeatures(int minCoveredPercent, int minCoverageCount, boolean whateverStrand,
            boolean getCoveredFeatures, ParametersReadClasses readClassesParams, Set<FeatureType> selectedFeatureTypes) {
        super(selectedFeatureTypes);
        this.minCoveredPercent = minCoveredPercent;
        this.minCoverageCount = minCoverageCount;
        this.whateverStrand = whateverStrand;
        this.getCoveredFeatures = getCoveredFeatures;
        this.readClassesParams = readClassesParams;
    }

    /**
     * @return the minimum percent of an feature that has to be covered with
     * at least minCoverageCount reads at each position
     */
    public int getMinCoveredPercent() {
        return minCoveredPercent;
    }

    /**
     * Set the minimum percent of an feature that has to be covered with at
     * least minCoverageCount reads at each position
     * @param minCoverageCount the minimum percent of an feature that has to
     * be covered with at least minCoverageCount reads at each position
     */
    public void setMinCoveredPercent(int minCoveredPercent) {
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
     * @param minCoverageCount the minimum coverage at a position to be counted
     *      as covered
     */
    public void setMinCoverageCount(int minCoverageCount) {
        this.minCoverageCount = minCoverageCount;
    } 

    /**
     * @return <tt>true</tt>, if the strand does not matter for this analysis,
     * false, if only mappings on the strand of the respective feature should be
     * considered.
     */
    public boolean isWhateverStrand() {
        return whateverStrand;
    }

    /**
     * @param whateverStrand <tt>true</tt>, if the strand does not matter for this analysis,
     * false, if only mappings on the strand of the respective feature should be
     * considered.
     */
    public void setWhateverStrand(boolean whateverStrand) {
        this.whateverStrand = whateverStrand;
    }

    /**
     * @return <code>true</code> if the covered features should be returned,
     * <code>false</code> if the uncovered features should be returned
     */
    public boolean isGetCoveredFeatures() {
        return getCoveredFeatures;
    }

    /**
     * @param getCoveredFeatures <code>true</code> if the covered features should be returned,
     * <code>false</code> if the uncovered features should be returned
     */
    public void setGetCoveredFeatures(boolean getCoveredFeatures) {
        this.getCoveredFeatures = getCoveredFeatures;
    }  
    
    /**
     * @return The parameter set for the used read classes for this analysis
     * instance.
     */
    public ParametersReadClasses getReadClassParams() {
        return readClassesParams;
    }
}
