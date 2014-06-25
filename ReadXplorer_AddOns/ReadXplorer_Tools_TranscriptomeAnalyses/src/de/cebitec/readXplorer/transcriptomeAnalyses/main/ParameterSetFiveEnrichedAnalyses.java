package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.StartCodon;
import de.cebitec.readXplorer.util.FeatureType;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Data storage for all parameters associated with a 5'-ends enriched RNA-seq
 * data analyses.
 *
 * @author jritter
 */
public class ParameterSetFiveEnrichedAnalyses implements ParameterSetI<ParameterSetFiveEnrichedAnalyses> {

    private Double fraction;
    private Integer cdsShiftPercentage;
    private Integer manuallySetThreshold;
    private final Integer ratio;
    private Integer threeUtrLimitAntisenseDetection;
    private final Integer leaderlessLimit, exclusionOfTSSDistance, keepingIntragenicTssDistanceLimit;
    private final boolean exclusionOfAllIntragenicTSS, keepAllIntragenicTss, includeBestMatchedReads;
    private final HashSet<FeatureType> excludeFeatureTypes;
    private HashMap<String, StartCodon> validStartCodons;
    private boolean keepOnlyAssignedIntragenicTss;
    private boolean thresholdManuallySet;

    /**
     * Data storage for all parameters associated with a 5'-ends enriched
     * RNA-seq data analyses.
     *
     * @param fraction
     * @param ratio
     * @param excludeAllInternalTSS
     * @param distanceForExcludionOfTss
     * @param leaderlessLimit
     * @param keepInternalDistance
     * @param keepInragenicTss
     * @param cdsShiftPercentage
     * @param includeBestMatchedReads
     * @param maxDistantaseFor3UtrAntisenseDetection
     * @param validStartCodons params keepOnlyAssignedIntragenicTss
     */
    public ParameterSetFiveEnrichedAnalyses(Double fraction, Integer ratio,
            boolean excludeAllInternalTSS, Integer distanceForExcludionOfTss,
            Integer leaderlessLimit, int keepInternalDistance, boolean keepInragenicTss, boolean keepOnlyAssignedIntragenicTss, int cdsShiftPercentage, boolean includeBestMatchedReads, int maxDistantaseFor3UtrAntisenseDetection, HashMap<String, StartCodon> validStartCodons, HashSet<FeatureType> fadeOutFeatureTypes) {
        this.fraction = fraction;
        this.ratio = ratio;
        this.leaderlessLimit = leaderlessLimit;
        this.exclusionOfAllIntragenicTSS = excludeAllInternalTSS;
        this.exclusionOfTSSDistance = distanceForExcludionOfTss;
        this.keepingIntragenicTssDistanceLimit = keepInternalDistance;
        this.cdsShiftPercentage = cdsShiftPercentage;
        this.keepAllIntragenicTss = keepInragenicTss;
        this.includeBestMatchedReads = includeBestMatchedReads;
        this.threeUtrLimitAntisenseDetection = maxDistantaseFor3UtrAntisenseDetection;
        this.validStartCodons = validStartCodons;
        this.excludeFeatureTypes = fadeOutFeatureTypes;
        this.keepOnlyAssignedIntragenicTss = keepOnlyAssignedIntragenicTss;
    }

    public Integer getCdsShiftPercentage() {
        return cdsShiftPercentage;
    }

    public HashMap<String, StartCodon> getValidStartCodons() {
        return validStartCodons;
    }

    public String getValidStartCodonsAsString() {
        String val = "";
        for (String startCodon : validStartCodons.keySet()) {
            val = val.concat(validStartCodons.get(startCodon).toString());
        }
        return val;
    }

    public HashSet<FeatureType> getExcludeFeatureTypes() {
        return excludeFeatureTypes;
    }

    public void setValidStartCodons(HashMap<String, StartCodon> validStartCodons) {
        this.validStartCodons = validStartCodons;
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

    /**
     *
     * @return <true> if checkbox for exclusion of all intragenic TSS is
     * selected else <false>
     */
    public boolean isExclusionOfAllIntragenicTSS() {
        return exclusionOfAllIntragenicTSS;
    }

    /**
     *
     * @return the distance limit between an intragenic TSS and the next
     * downstream TLS
     */
    public Integer getKeepIntragenicTssDistanceLimit() {
        return keepingIntragenicTssDistanceLimit;
    }

    public boolean isKeepAllIntragenicTss() {
        return keepAllIntragenicTss;
    }

    public Integer getThreeUtrLimitAntisenseDetection() {
        return threeUtrLimitAntisenseDetection;
    }

    public void setThreeUtrLimitAntisenseDetection(Integer threeUtrLimitAntisenseDetection) {
        this.threeUtrLimitAntisenseDetection = threeUtrLimitAntisenseDetection;
    }

    public boolean isIncludeBestMatchedReads() {
        return includeBestMatchedReads;
    }

    public boolean isKeepOnlyAssignedIntragenicTss() {
        return keepOnlyAssignedIntragenicTss;
    }

    public void setKeepOnlyAssignedIntragenicTss(boolean keepOnlyAssignedIntragenicTss) {
        this.keepOnlyAssignedIntragenicTss = keepOnlyAssignedIntragenicTss;
    }

    public Integer getManuallySetThreshold() {
        return manuallySetThreshold;
    }

    public void setManuallySetThreshold(Integer manuallySetThreshold) {
        this.manuallySetThreshold = manuallySetThreshold;
    }

    public boolean isThresholdManuallySet() {
        return thresholdManuallySet;
    }

    public void setThresholdManuallySet(boolean thresholdManuallySet) {
        this.thresholdManuallySet = thresholdManuallySet;
    }

}
