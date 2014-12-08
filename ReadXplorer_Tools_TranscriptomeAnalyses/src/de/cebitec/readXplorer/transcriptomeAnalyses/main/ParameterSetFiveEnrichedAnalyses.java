
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.StartCodon;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Data storage for all parameters associated with a 5'-ends enriched RNA-seq
 * data analyses.
 *
 * @author jritter
 */
public class ParameterSetFiveEnrichedAnalyses implements
        ParameterSetI<ParameterSetFiveEnrichedAnalyses> {

    private Double fraction;
    private Integer cdsShiftPercentage;
    private Integer manuallySetThreshold;
    private final Integer ratio;
    private Integer threeUtrLimitAntisenseDetection;
    private final Integer leaderlessLimit, exclusionOfTSSDistance, keepingIntragenicTssDistanceLimit;
    private final boolean exclusionOfAllIntragenicTSS, keepAllIntragenicTss, includeBestMatchedReads;
    private final Set<FeatureType> excludeFeatureTypes;
    private Map<String, StartCodon> validStartCodons;
    private boolean keepOnlyAssignedIntragenicTss;
    private boolean thresholdManuallySet;


    /**
     * Constructor for tasting cases.
     */
    public ParameterSetFiveEnrichedAnalyses() {
        this.ratio = 0;
        this.leaderlessLimit = 0;
        this.exclusionOfTSSDistance = 0;
        this.keepingIntragenicTssDistanceLimit = 0;
        this.exclusionOfAllIntragenicTSS = false;
        this.keepAllIntragenicTss = false;
        this.includeBestMatchedReads = false;
        this.excludeFeatureTypes = new HashSet<>();
    }


    /**
     * Data storage for all parameters associated with a 5'-ends enriched
     * RNA-seq data analyses.
     *
     * @param fraction
     * @param ratio                                  minimal increase value for
     *                                               readstarts on position i-1
     *                                               to
     *                                               readstarts on position i.
     * @param excludeAllInternalTSS
     * @param distanceForExcludionOfTss
     * @param leaderlessLimit
     * @param keepInternalDistance
     * @param keepInragenicTss
     * @param cdsShiftPercentage
     * @param includeBestMatchedReads
     * @param maxDistantaseFor3UtrAntisenseDetection
     * @param validStartCodons                       params
     *                                               keepOnlyAssignedIntragenicTss
     */
    public ParameterSetFiveEnrichedAnalyses( Double fraction, Integer ratio,
                                             boolean excludeAllInternalTSS, Integer distanceForExcludionOfTss,
                                             Integer leaderlessLimit, int keepInternalDistance, boolean keepInragenicTss, boolean keepOnlyAssignedIntragenicTss,
                                             int cdsShiftPercentage, boolean includeBestMatchedReads, int maxDistantaseFor3UtrAntisenseDetection,
                                             Map<String, StartCodon> validStartCodons, Set<FeatureType> fadeOutFeatureTypes ) {
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


    /**
     * Get percentage for CDS-shift check.
     *
     * @return cdsShiftPercentage
     */
    public Integer getCdsShiftPercentage() {
        return cdsShiftPercentage;
    }


    /**
     * Get Map of valid start codons for analysis.
     * <p>
     * @return Map<String, StartCodon>
     */
    public Map<String, StartCodon> getValidStartCodons() {
        return validStartCodons;
    }


    /**
     * Get valid start codons as a string representation.
     * <p>
     * @return string representation of valid start codons
     */
    public String getValidStartCodonsAsString() {
        String val = "";
        for( String startCodon : validStartCodons.keySet() ) {
            val = val.concat( validStartCodons.get( startCodon ).toString() );
        }
        return val;
    }


    /**
     * Get the Hash of feature types, that schould be excluded from analysis.
     * <p>
     * @return HashSet<FeatureType>
     */
    public Set<FeatureType> getExcludeFeatureTypes() {
        return excludeFeatureTypes;
    }


    /**
     * Set Hash of valid start codons.
     * <p>
     * @param validStartCodons
     */
    public void setValidStartCodons( HashMap<String, StartCodon> validStartCodons ) {
        this.validStartCodons = validStartCodons;
    }


    /**
     * Set Percentage for CDS-shift check.
     *
     * @param cdsShiftPercentage
     */
    public void setCdsShiftPercentage( int cdsShiftPercentage ) {
        this.cdsShiftPercentage = cdsShiftPercentage;
    }


    /**
     * Get fraction.
     *
     * @return fraction
     */
    public double getFraction() {
        return fraction;
    }


    /**
     * Set fraction.
     *
     * @param fraction
     */
    public void setFraction( double fraction ) {
        this.fraction = fraction;
    }


    /**
     * Get ratio value.
     *
     * @return ration value
     */
    public Integer getRatio() {
        return ratio;
    }


    /**
     * Get range for leaderless classification.
     *
     * @return leaderless limit
     */
    public Integer getLeaderlessLimit() {
        return leaderlessLimit;
    }


    /**
     *
     * @return
     */
    public Integer getExclusionOfTSSDistance() {
        return exclusionOfTSSDistance;
    }


    /**
     *
     * @return <true> if checkbox for exclusion of all intragenic TSS is
     *         selected else <false>
     */
    public boolean isExclusionOfAllIntragenicTSS() {
        return exclusionOfAllIntragenicTSS;
    }


    /**
     *
     * @return the distance limit between an intragenic TSS and the next
     *         downstream TLS
     */
    public Integer getKeepIntragenicTssDistanceLimit() {
        return keepingIntragenicTssDistanceLimit;
    }


    /**
     *
     * @return
     */
    public boolean isKeepAllIntragenicTss() {
        return keepAllIntragenicTss;
    }


    /**
     *
     * @return
     */
    public Integer getThreeUtrLimitAntisenseDetection() {
        return threeUtrLimitAntisenseDetection;
    }


    /**
     *
     * @return
     */
    public boolean isIncludeBestMatchedReads() {
        return includeBestMatchedReads;
    }


    /**
     *
     * @return
     */
    public boolean isKeepOnlyAssignedIntragenicTss() {
        return keepOnlyAssignedIntragenicTss;
    }


    /**
     * Get manually setted background threshold.
     *
     * @return manuallySetThreshold
     */
    public Integer getManuallySetThreshold() {
        return manuallySetThreshold;
    }


    /**
     *
     * @param manuallySetThreshold
     */
    public void setManuallySetThreshold( Integer manuallySetThreshold ) {
        this.manuallySetThreshold = manuallySetThreshold;
    }


    /**
     *
     * @return <true> if threshold is setted manually else <false>
     */
    public boolean isThresholdManuallySet() {
        return thresholdManuallySet;
    }


    /**
     *
     * @param thresholdManuallySet
     */
    public void setThresholdManuallySet( boolean thresholdManuallySet ) {
        this.thresholdManuallySet = thresholdManuallySet;
    }


}
