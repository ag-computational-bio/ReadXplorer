
package de.cebitec.readxplorer.transcriptomeanalyses.main;


import de.cebitec.readxplorer.databackend.ParameterSetI;
import de.cebitec.readxplorer.databackend.ParametersFeatureTypes;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.Set;


/**
 * Data storage for all parameters associated with an RPKM and read count
 * calculation.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetRPKM extends ParametersFeatureTypes implements
        ParameterSetI<ParameterSetRPKM> {

    private final boolean performRPKMAnalysis;
    private int minReadCount;
    private int maxReadCount;


    /**
     * Data storage for all parameters associated with an RPKM and read count
     * calculation.
     * <p>
     * @param performRPKMAnalysis true, if the RPKM analysis shall be performed
     * @param minReadCount        minimum read count of a feature to return it
     *                            in the result
     * @param maxReadCount        maximum read count of a feature to return it
     *                            in the result
     * @param selFeatureTypes     the set of selected feature types
     */
    public ParameterSetRPKM( boolean performRPKMAnalysis, int minReadCount, int maxReadCount, Set<FeatureType> selFeatureTypes ) {

        super( selFeatureTypes );
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
     * @param minRPKM the minimum read count of a feature to return it in the
     *                result
     */
    public void setMinReadCount( int minRPKM ) {
        this.minReadCount = minRPKM;
    }


    /**
     * @param maxRPKM the maximum read count of a feature to return it in the
     *                result
     */
    public void setMaxReadCount( int maxRPKM ) {
        this.maxReadCount = maxRPKM;
    }


}
