package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ParameterSetI;

/**
 *
 * @author jritter
 */
public class ParameterSetFiveEnrichedAnalyses implements ParameterSetI<ParameterSetFiveEnrichedAnalyses> {

    private Double fraction;
    private Integer ratio;
    private Integer upstreamRegion, downstreamRegion;
    private Integer leaderlessLimit, exclusionOfTSSDistance, keepingInternalTssDistance;
    private boolean exclusionOfInternalTSS;

    /**
     * Data storage for all parameters associated with a transcription analysis.
     *
     * @param performTSSAnalysis
     * @param performLeaderlessAnalysis
     * @param performAntisenseAnalysis
     * @param fraction
     */
    public ParameterSetFiveEnrichedAnalyses(Double fraction, Integer ratio, Integer upstream, Integer downstream,
            boolean excludeInternalTSS, Integer distanceForExcludionOfTss, 
            Integer leaderlessLimit, int keepInternalDistance) {
        this.fraction = fraction;
        this.upstreamRegion = upstream;
        this.downstreamRegion = downstream;
        this.ratio = ratio;
        this.leaderlessLimit = leaderlessLimit;
        this.exclusionOfInternalTSS = excludeInternalTSS;
        this.exclusionOfTSSDistance = distanceForExcludionOfTss;
        this.keepingInternalTssDistance = keepInternalDistance;
            

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

    public Integer getKeepingInternalTssDistance() {
        return keepingInternalTssDistance;
    }


}
