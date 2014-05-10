package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.ParametersFeatureTypes;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.util.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with an operon detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetOperonDet extends ParametersFeatureTypes implements ParameterSetI<ParameterSetOperonDet> {
    
    private boolean performOperonAnalysis;
    private int minSpanningReads;
    private boolean autoOperonParamEstimation;
    private final ParametersReadClasses readClassParams;

    /**
     * Data storage for all parameters associated with an operon detection.
     * @param performOperonAnalysis true, if the operon analysis should be carried out
     * @param minSpanningReads minimum number of spanning reads between two neighboring
     * features
     * @param autoOperonParamEstimation true, if the automatic parameter estimation should
     * be switched on for the operon detection
     * @param selFeatureTypes the set of selected feature types
     * @param readClassParams
     */
    public ParameterSetOperonDet(boolean performOperonAnalysis, int minSpanningReads, 
            boolean autoOperonParamEstimation, Set<FeatureType> selFeatureTypes, ParametersReadClasses readClassParams) {
        super(selFeatureTypes);
        this.performOperonAnalysis = performOperonAnalysis;
        this.minSpanningReads = minSpanningReads;
        this.autoOperonParamEstimation = autoOperonParamEstimation;
        this.readClassParams = readClassParams;
    }

    /**
     * @return true, if the operon analysis should be carried out
     */
    public boolean isPerformOperonAnalysis() {
        return performOperonAnalysis;
    }

    /**
     * @return minimum number of spanning reads between two neighboring
     * features
     */
    public int getMinSpanningReads() {
        return minSpanningReads;
    }

    /**
     * @return true, if the automatic parameter estimation should
     * be switched on for the operon detection
     */
    public boolean isAutoOperonParamEstimation() {
        return autoOperonParamEstimation;
    }

    /**
     * @param performOperonAnalysis true, if the operon analysis should be carried out
     */
    public void setPerformOperonAnalysis(boolean performOperonAnalysis) {
        this.performOperonAnalysis = performOperonAnalysis;
    }

    /**
     * @param minSpanningReads minimum number of spanning reads between two
     * neighboring features
     */
    public void setMinSpanningReads(int minSpanningReads) {
        this.minSpanningReads = minSpanningReads;
    }

    /**
     * @param autoOperonParamEstimation true, if the automatic parameter
     * estimation should be switched on for the operon detection
     */
    public void setAutoOperonParamEstimation(boolean autoOperonParamEstimation) {
        this.autoOperonParamEstimation = autoOperonParamEstimation;
    }

    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }
    
}
