package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ParameterSetI;

/**
 *
 * @author jritter
 */
public class ParameterSetFiveEnrichedAnalyses implements ParameterSetI<ParameterSetFiveEnrichedAnalyses> {

    private boolean performTSSAnalysis;
    private boolean performAntisenseAnalysis;
    private Double fraction;
    private Integer ratio;
    private Integer upstreamRegion, downstreamRegion;
    private Integer leaderlessLimit, exclusionOfTSSDistance, keepingInternalTssDistance;
    private boolean exclusionOfInternalTSS, cdsShiftDetection, performPutativeUnAnnotated;

    /**
     * Data storage for all parameters associated with a transcription analysis.
     *
     * @param performTSSAnalysis
     * @param performLeaderlessAnalysis
     * @param performAntisenseAnalysis
     * @param fraction
     */
    public ParameterSetFiveEnrichedAnalyses(boolean performTSSAnalysis, boolean performAntisenseAnalysis, boolean performPutativeUnAnnotatedAnalysis, Double fraction, Integer ratio, Integer upstream, Integer downstream,
            boolean excludeInternalTSS, Integer distanceForExcludionOfTss, 
            Integer leaderlessLimit, boolean cdsShiftDetection, int keepInternalDistance) {
        this.performTSSAnalysis = performTSSAnalysis;
        this.performAntisenseAnalysis = performAntisenseAnalysis;
        this.fraction = fraction;
        this.upstreamRegion = upstream;
        this.downstreamRegion = downstream;
        this.ratio = ratio;
        this.leaderlessLimit = leaderlessLimit;
        this.cdsShiftDetection = cdsShiftDetection;
        this.exclusionOfInternalTSS = excludeInternalTSS;
        this.exclusionOfTSSDistance = distanceForExcludionOfTss;
        this.keepingInternalTssDistance = keepInternalDistance;
        this.performPutativeUnAnnotated = performPutativeUnAnnotatedAnalysis;
            

    }

    public boolean isPerformTSSAnalysis() {
        return performTSSAnalysis;
    }

    public void setPerformTSSAnalysis(boolean performTSSAnalysis) {
        this.performTSSAnalysis = performTSSAnalysis;
    }

    public boolean isPerformAntisenseAnalysis() {
        return performAntisenseAnalysis;
    }

    public void setPerformAntisenseAnalysis(boolean performAntisenseAnalysis) {
        this.performAntisenseAnalysis = performAntisenseAnalysis;
    }

    public double getFraction() {
        return fraction;
    }

    public void setFraction(double fraction) {
        this.fraction = fraction;
    }

    public int getUpstreamRegion() {
        return upstreamRegion;
    }

    public int getDownstreamRegion() {
        return downstreamRegion;
    }

    public Integer getRatio() {
        return ratio;
    }

    public Integer getLeaderlessLimit() {
        return leaderlessLimit;
    }

    public Integer getExclusionOfTSSDistance() {
        return exclusionOfTSSDistance;
    }

    public boolean isExclusionOfInternalTSS() {
        return exclusionOfInternalTSS;
    }

    public boolean isCdsShiftDetection() {
        return cdsShiftDetection;
    }

    public Integer getKeepingInternalTssDistance() {
        return keepingInternalTssDistance;
    }

    boolean isPerformUnAnnotatedRegionsAnalysis() {
        return this.performPutativeUnAnnotated;
    }

}
