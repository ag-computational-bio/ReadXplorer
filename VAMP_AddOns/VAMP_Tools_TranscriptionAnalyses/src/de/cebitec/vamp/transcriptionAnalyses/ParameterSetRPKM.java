package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ParameterSetI;
import de.cebitec.vamp.util.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with an RPKM and read count 
 * calculation.
 * 
 * @author Martin Tötsches, Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetRPKM implements ParameterSetI<ParameterSetRPKM> {
    
    private boolean performRPKMAnalysis;
    private int minReadCount;
    private int maxReadCount;
    private Set<FeatureType> selFeatureTypes;
    
    /**
     * Data storage for all parameters associated with an RPKM and read count
     * calculation.
     * @param performRPKMAnalysis true, if the RPKM analysis shall be performed
     * @param minReadCount minimum read count of a feature to return it in the result
     * @param maxReadCount maximum read count of a feature to return it in the result
     * @param selFeatureTypes the list of selected feature types for which the RPKM and
     * read count values shall be calculated
     */
    public ParameterSetRPKM(boolean performRPKMAnalysis, int minReadCount, int maxReadCount, Set<FeatureType> selFeatureTypes) {
        this.performRPKMAnalysis = performRPKMAnalysis;
        this.minReadCount = minReadCount;
        this.maxReadCount = maxReadCount;
        this.selFeatureTypes = selFeatureTypes;
    }

    /**
     * @return true, if the RPKM analysis shall be performed
     */
    public boolean isPerformRPKMAnalysis() {
        return performRPKMAnalysis;
    }

    /**
     * @return the minimum read count of a feature to return it in the result
     */
    public int getMinReadCount() {
        return minReadCount;
    }

    /**
     * @return the maximum read count of a feature to return it in the result
     */
    public int getMaxReadCount() {
        return maxReadCount;
    }

    /**
     * @param minRPKM the minimum read count of a feature to return it in the result
     */
    public void setMinReadCount(int minRPKM) {
        this.minReadCount = minRPKM;
    }

    /**
     * @param maxRPKM the maximum read count of a feature to return it in the result
     */
    public void setMaxReadCount(int maxRPKM) {
        this.maxReadCount = maxRPKM;
    }

    /**
     * @return the list of selected feature types for which the RPKM and
     * read count values shall be calculated.
     */
    public Set<FeatureType> getSelFeatureTypes() {
        return selFeatureTypes;
    }

    /**
     * @param selFeatureTypes the list of selected feature types for which the RPKM and
     * read count values shall be calculated
     */
    public void setSelFeatureTypes(Set<FeatureType> selFeatureTypes) {
        this.selFeatureTypes = selFeatureTypes;
    }
    
}
