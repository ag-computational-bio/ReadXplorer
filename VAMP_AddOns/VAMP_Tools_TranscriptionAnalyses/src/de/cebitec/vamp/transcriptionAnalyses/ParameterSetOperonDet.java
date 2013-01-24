package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ParameterSetI;

/**
 * Data storage for all parameters associated with an operon detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetOperonDet implements ParameterSetI<ParameterSetOperonDet> {
    
    private boolean performOperonAnalysis;
    private int minSpanningReads;
    private boolean autoOperonParamEstimation;

    /**
     * Data storage for all parameters associated with an operon detection.
     * @param performOperonAnalysis true, if the operon analysis should be carries out
     * @param minSpanningReads minimum number of spanning reads between two neighboring
     * features
     * @param autoOperonParamEstimation true, if the automatic parameter estimation should
     * be switched on for the operon detection
     * 
     */
    public ParameterSetOperonDet(boolean performOperonAnalysis, int minSpanningReads, boolean autoOperonParamEstimation) {
        this.performOperonAnalysis = performOperonAnalysis;
        this.minSpanningReads = minSpanningReads;
        this.autoOperonParamEstimation = autoOperonParamEstimation;
    }

    public boolean isPerformOperonAnalysis() {
        return performOperonAnalysis;
    }

    public int getMinSpanningReads() {
        return minSpanningReads;
    }

    public boolean isAutoOperonParamEstimation() {
        return autoOperonParamEstimation;
    }

    public void setPerformOperonAnalysis(boolean performOperonAnalysis) {
        this.performOperonAnalysis = performOperonAnalysis;
    }

    public void setMinSpanningReads(int minSpanningReads) {
        this.minSpanningReads = minSpanningReads;
    }

    public void setAutoOperonParamEstimation(boolean autoOperonParamEstimation) {
        this.autoOperonParamEstimation = autoOperonParamEstimation;
    }
}
