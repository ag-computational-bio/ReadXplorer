package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;

/**
 * Data storage for all parameters associated with a transcription analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetTSS implements ParameterSetI<ParameterSetTSS> {
    
    private boolean performTSSAnalysis;
    private boolean autoTssParamEstimation;
    private boolean performUnannotatedTranscriptDet;
    private int minNoReadStarts;
    private int minPercentIncrease;
    private int maxLowCovInitCount;
    private int minLowCovIncrease;
    private int minTranscriptExtensionCov;
    private int maxLeaderlessDistance;
    private ParametersReadClasses readClassParams;

    
    /**
     * Data storage for all parameters associated with a transcription analysis.
     * @param performTSSAnalysis
     * @param autoTssParamEstimation
     * @param performUnannotatedTranscriptDet
     * @param minNoReadStarts
     * @param minPercentIncrease
     * @param maxLowCovInitCount
     * @param minLowCovIncrease
     * @param maxLeaderlessDistance
     * @param minTranscriptExtensionCov 
     */
    ParameterSetTSS(boolean performTSSAnalysis, boolean autoTssParamEstimation, boolean performUnannotatedTranscriptDet, 
            int minNoReadStarts, int minPercentIncrease, int maxLowCovInitCount, int minLowCovIncrease, 
            int minTranscriptExtensionCov, int maxLeaderlessDistance, ParametersReadClasses readClassParams) {
        this.performTSSAnalysis = performTSSAnalysis;
        this.autoTssParamEstimation = autoTssParamEstimation;
        this.performUnannotatedTranscriptDet = performUnannotatedTranscriptDet;
        this.minNoReadStarts = minNoReadStarts;
        this.minPercentIncrease = minPercentIncrease;
        this.maxLowCovInitCount = maxLowCovInitCount;
        this.minLowCovIncrease = minLowCovIncrease;
        this.minTranscriptExtensionCov = minTranscriptExtensionCov;
        this.maxLeaderlessDistance = maxLeaderlessDistance;
        this.readClassParams = readClassParams;
        
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

    public int getMinNoReadStarts() {
        return minNoReadStarts;
    }

    public int getMinPercentIncrease() {
        return minPercentIncrease;
    }

    public int getMaxLowCovReadStarts() {
        return maxLowCovInitCount;
    }

    public int getMinLowCovReadStarts() {
        return minLowCovIncrease;
    }

    public int getMinTranscriptExtensionCov() {
        return minTranscriptExtensionCov;
    }

    public int getMaxLeaderlessDistance() {
        return this.maxLeaderlessDistance;
    }

    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
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

    public void setMinNoReadStarts(int minNoReadStarts) {
        this.minNoReadStarts = minNoReadStarts;
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

    public void setMaxLeaderlessDistance(int maxLeaderlessDistance) {
        this.maxLeaderlessDistance = maxLeaderlessDistance;
    }

    public void setReadClassParams(ParametersReadClasses readClassParams) {
        this.readClassParams = readClassParams;
    }
    
}
