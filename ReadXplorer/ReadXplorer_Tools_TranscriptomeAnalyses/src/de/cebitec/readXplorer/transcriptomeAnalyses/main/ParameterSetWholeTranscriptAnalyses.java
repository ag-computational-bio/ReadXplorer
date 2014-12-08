package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import de.cebitec.readXplorer.databackend.ParameterSetI;
import java.io.File;


/**
 *
 * @author jritter
 */
public class ParameterSetWholeTranscriptAnalyses implements
        ParameterSetI<ParameterSetWholeTranscriptAnalyses> {

    private boolean performWholeTrascriptomeAnalyses, performOperonDetection, includeBestMatchedReadsOP, includeBestMatchedReadsRpkm, includeBestMatchedReadsNr, performNovelRegionDetection, performRPKMs, ratioInclusion;
    private double fraction, fractionForNewRegionDetection;
    private int minLengthBoundary, increaseRatioValue;
    private File referenceFile;
    private Integer manuallySetThreshold;
    private boolean thresholdManuallySet;


    /**
     *
     * @param performWholeTrascriptomeAnalyses
     * @param performOperonDetection
     * @param performNovelRegionDetection
     * @param rPKMs
     * @param referenceFile
     * @param fraction
     * @param minBoundary
     * @param ratioInclusion
     * @param increaseRatioValue
     * @param includeBestMatchedReads
     */
    public ParameterSetWholeTranscriptAnalyses( boolean performWholeTrascriptomeAnalyses, boolean performOperonDetection, boolean performNovelRegionDetection, boolean rPKMs, File referenceFile, double fraction, int minBoundary, boolean ratioInclusion, int increaseRatioValue, boolean includeBestMatchedReadsOP, boolean includeBestMatchedReadsRpkm, boolean includeBestMatchedReadsNr ) {
        this.performWholeTrascriptomeAnalyses = performWholeTrascriptomeAnalyses;
        this.performOperonDetection = performOperonDetection;
        this.performNovelRegionDetection = performNovelRegionDetection;
        this.performRPKMs = rPKMs;
        this.fraction = fraction;
        this.minLengthBoundary = minBoundary;
        this.increaseRatioValue = increaseRatioValue;
        this.ratioInclusion = ratioInclusion;
        this.includeBestMatchedReadsOP = includeBestMatchedReadsOP;
        this.includeBestMatchedReadsRpkm = includeBestMatchedReadsRpkm;
        this.includeBestMatchedReadsNr = includeBestMatchedReadsNr;
        this.referenceFile = referenceFile;
    }


    public boolean isRatioInclusion() {
        return ratioInclusion;
    }


    public File getReferenceFile() {
        return referenceFile;
    }


    public void setReferenceFile( File referenceFile ) {
        this.referenceFile = referenceFile;
    }


    public void setRatioInclusion( boolean ratioInclusion ) {
        this.ratioInclusion = ratioInclusion;
    }


    public boolean isPerformRPKMs() {
        return performRPKMs;
    }


    public void setPerformRPKMs( boolean performRPKMs ) {
        this.performRPKMs = performRPKMs;
    }


    public int getIncreaseRatioValue() {
        return increaseRatioValue;
    }


    public void setIncreaseRatioValue( int increaseRatioValue ) {
        this.increaseRatioValue = increaseRatioValue;
    }


    public boolean isPerformWholeTrascriptomeAnalyses() {
        return performWholeTrascriptomeAnalyses;
    }


    public void setPerformWholeTrascriptomeAnalyses( boolean performWholeTrascriptomeAnalyses ) {
        this.performWholeTrascriptomeAnalyses = performWholeTrascriptomeAnalyses;
    }


    public boolean isPerformOperonDetection() {
        return performOperonDetection;
    }


    public void setPerformOperonDetection( boolean performOperonDetection ) {
        this.performOperonDetection = performOperonDetection;
    }


    public boolean isPerformNovelRegionDetection() {
        return performNovelRegionDetection;
    }


    public void setPerformNovelRegionDetection( boolean performNovelRegionDetection ) {
        this.performNovelRegionDetection = performNovelRegionDetection;
    }


    public boolean isPerformingRPKMs() {
        return performRPKMs;
    }


    public void setPerformingRPKMs( boolean rPKMs ) {
        this.performRPKMs = rPKMs;
    }


    public double getFraction() {
        return fraction;
    }


    public void setFraction( double fraction ) {
        this.fraction = fraction;
    }


    public int getMinLengthBoundary() {
        return minLengthBoundary;
    }


    public void setMinLengthBoundary( int minLengthBoundary ) {
        this.minLengthBoundary = minLengthBoundary;
    }


    public double getFractionForNewRegionDetection() {
        return fractionForNewRegionDetection;
    }


    public boolean isIncludeBestMatchedReadsOP() {
        return includeBestMatchedReadsOP;
    }


    public void setIncludeBestMatchedReadsOP( boolean includeBestMatchedReadsOP ) {
        this.includeBestMatchedReadsOP = includeBestMatchedReadsOP;
    }


    public boolean isIncludeBestMatchedReadsRpkm() {
        return includeBestMatchedReadsRpkm;
    }


    public void setIncludeBestMatchedReadsRpkm( boolean includeBestMatchedReadsRpkm ) {
        this.includeBestMatchedReadsRpkm = includeBestMatchedReadsRpkm;
    }


    public boolean isIncludeBestMatchedReadsNr() {
        return includeBestMatchedReadsNr;
    }


    public void setIncludeBestMatchedReadsNr( boolean includeBestMatchedReadsNr ) {
        this.includeBestMatchedReadsNr = includeBestMatchedReadsNr;
    }


    public Integer getManuallySetThreshold() {
        return manuallySetThreshold;
    }


    public void setManuallySetThreshold( Integer manuallySetThreshold ) {
        this.manuallySetThreshold = manuallySetThreshold;
    }


    public boolean isThresholdManuallySet() {
        return thresholdManuallySet;
    }


    public void setThresholdManuallySet( boolean thresholdManuallySet ) {
        this.thresholdManuallySet = thresholdManuallySet;
    }


}
