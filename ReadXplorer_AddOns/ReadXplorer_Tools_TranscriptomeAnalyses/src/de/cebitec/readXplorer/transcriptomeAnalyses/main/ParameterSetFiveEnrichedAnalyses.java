package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ParameterSetI;

/**
 *
 * @author jritter
 */
public class ParameterSetFiveEnrichedAnalyses implements ParameterSetI<ParameterSetFiveEnrichedAnalyses> {

    private Double fraction;
    private Integer cdsShiftPercentage;
    private Integer ratio;
    private Integer leaderlessLimit, exclusionOfTSSDistance, keepingInternalTssDistance;
    private boolean exclusionOfInternalTSS;

    /**
     * Data storage for all parameters associated with a transcription analysis.
     * 
     * @param fraction
     * @param ratio
     * @param excludeInternalTSS
     * @param distanceForExcludionOfTss
     * @param leaderlessLimit
     * @param keepInternalDistance
     * @param cdsShiftPercentage 
     */
    public ParameterSetFiveEnrichedAnalyses(Double fraction, Integer ratio,
            boolean excludeInternalTSS, Integer distanceForExcludionOfTss,
            Integer leaderlessLimit, int keepInternalDistance, int cdsShiftPercentage) {
        this.fraction = fraction;
        this.ratio = ratio;
        this.leaderlessLimit = leaderlessLimit;
        this.exclusionOfInternalTSS = excludeInternalTSS;
        this.exclusionOfTSSDistance = distanceForExcludionOfTss;
        this.keepingInternalTssDistance = keepInternalDistance;
        this.cdsShiftPercentage = cdsShiftPercentage;
    }

    public Integer getCdsShiftPercentage() {
        return cdsShiftPercentage;
    }

    public void setCdsShiftPercentage(int cdsShiftPercentage) {
        this.cdsShiftPercentage = cdsShiftPercentage;
    }

    public double getFraction() {
        return fraction;
    }

    public void setFraction(double fraction) {
        this.fraction = fraction;
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
