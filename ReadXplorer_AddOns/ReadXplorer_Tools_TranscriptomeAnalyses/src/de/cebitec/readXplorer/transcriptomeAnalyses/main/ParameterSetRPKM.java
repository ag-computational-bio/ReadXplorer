package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.ParametersFeatureTypes;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with an RPKM and read count 
 * calculation.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetRPKM extends ParametersFeatureTypes implements ParameterSetI<ParameterSetRPKM> {
    
    private boolean performRPKMAnalysis;
    private int minReadCount;
    private int maxReadCount;
    
    /**
     * Data storage for all parameters associated with an RPKM and read count
     * calculation.
     * @param performRPKMAnalysis true, if the RPKM analysis shall be performed
     * @param minReadCount minimum read count of a feature to return it in the result
     * @param maxReadCount maximum read count of a feature to return it in the result
     * @param selFeatureTypes the set of selected feature types
     */
    public ParameterSetRPKM(boolean performRPKMAnalysis, int minReadCount, int maxReadCount, Set<FeatureType> selFeatureTypes) {
        
        super(selFeatureTypes);
        this.performRPKMAnalysis = performRPKMAnalysis;
        this.minReadCount = minReadCount;
        this.maxReadCount = maxReadCount;
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
}
