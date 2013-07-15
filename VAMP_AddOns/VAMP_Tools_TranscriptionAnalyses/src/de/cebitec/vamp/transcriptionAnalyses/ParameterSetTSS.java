package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ParameterSetI;

/**
 * Data storage for all parameters associated with a transcription analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetTSS implements ParameterSetI<ParameterSetTSS> {
    
    private boolean performTSSAnalysis;
    private boolean autoTssParamEstimation;
    private boolean performUnannotatedTranscriptDet;
    private int minTotalIncrease;
    private int minPercentIncrease;
    private int maxLowCovInitCount;
    private int minLowCovIncrease;
    private int minTranscriptExtensionCov;

    
    /**
     * Data storage for all parameters associated with a transcription analysis.
     * @param performTSSAnalysis
     * @param autoTssParamEstimation
     * @param performUnannotatedTranscriptDet
     * @param minTotalIncrease
     * @param minPercentIncrease
     * @param maxLowCovInitCount
     * @param minLowCovIncrease
     * @param minTranscriptExtensionCov 
     */
    ParameterSetTSS(boolean performTSSAnalysis, boolean autoTssParamEstimation, boolean performUnannotatedTranscriptDet, 
            int minTotalIncrease, int minPercentIncrease, int maxLowCovInitCount, int minLowCovIncrease, 
            int minTranscriptExtensionCov) {
        this.performTSSAnalysis = performTSSAnalysis;
        this.autoTssParamEstimation = autoTssParamEstimation;
        this.performUnannotatedTranscriptDet = performUnannotatedTranscriptDet;
        this.minTotalIncrease = minTotalIncrease;
        this.minPercentIncrease = minPercentIncrease;
        this.maxLowCovInitCount = maxLowCovInitCount;
        this.minLowCovIncrease = minLowCovIncrease;
        this.minTranscriptExtensionCov = minTranscriptExtensionCov;
        
    }

    public boolean isPerformTSSAnalysis() {
        return performTSSAnalysis;
    }

    public boolean isAutoTssParamEstimation() {
        return autoTssParamEstimation;
    }
    
    public boolean isPerformUnannotatedTranscriptDet() {
        return performUnannotatedTranscriptDet;
    }

    public int getMinTotalIncrease() {
        return minTotalIncrease;
    }

    public int getMinPercentIncrease() {
        return minPercentIncrease;
    }

    public int getMaxLowCovInitCount() {
        return maxLowCovInitCount;
    }

    public int getMinLowCovIncrease() {
        return minLowCovIncrease;
    }

    public int getMinTranscriptExtensionCov() {
        return minTranscriptExtensionCov;
    }

    public void setPerformTSSAnalysis(boolean performTSSAnalysis) {
        this.performTSSAnalysis = performTSSAnalysis;
    }

    public void setAutoTssParamEstimation(boolean autoTssParamEstimation) {
        this.autoTssParamEstimation = autoTssParamEstimation;
    }

    public void setPerformUnannotatedTranscriptDet(boolean performUnannotatedTranscriptDet) {
        this.performUnannotatedTranscriptDet = performUnannotatedTranscriptDet;
    }

    public void setMinTotalIncrease(int minTotalIncrease) {
        this.minTotalIncrease = minTotalIncrease;
    }

    public void setMinPercentIncrease(int minPercentIncrease) {
        this.minPercentIncrease = minPercentIncrease;
    }

    public void setMaxLowCovInitCount(int maxLowCovInitCount) {
        this.maxLowCovInitCount = maxLowCovInitCount;
    }

    public void setMinLowCovIncrease(int minLowCovIncrease) {
        this.minLowCovIncrease = minLowCovIncrease;
    }

    public void setMinTranscriptExtensionCov(int minTranscriptExtensionCov) {
        this.minTranscriptExtensionCov = minTranscriptExtensionCov;
    }    
}
