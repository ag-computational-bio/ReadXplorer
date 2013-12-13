package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ParameterSetI;

/**
 *
 * @author jritter
 */
public class ParameterSetWholeTranscriptAnalyses implements ParameterSetI<ParameterSetWholeTranscriptAnalyses> {

    private boolean performWholeTrascriptomeAnalyses, performOperonDetection, performNovelRegionDetection, performRPKMs;
    private double fraction, fractionForNewRegionDetection;
    private int minLengthBoundary;

    public ParameterSetWholeTranscriptAnalyses(boolean performWholeTrascriptomeAnalyses, boolean performOperonDetection, boolean performNovelRegionDetection, boolean rPKMs, double fraction, int minBoundary) {
        this.performWholeTrascriptomeAnalyses = performWholeTrascriptomeAnalyses;
        this.performOperonDetection = performOperonDetection;
        this.performNovelRegionDetection = performNovelRegionDetection;
        this.performRPKMs = rPKMs;
        this.fraction = fraction;
        this.minLengthBoundary = minBoundary;
    }

    public boolean isPerformWholeTrascriptomeAnalyses() {
        return performWholeTrascriptomeAnalyses;
    }

    public void setPerformWholeTrascriptomeAnalyses(boolean performWholeTrascriptomeAnalyses) {
        this.performWholeTrascriptomeAnalyses = performWholeTrascriptomeAnalyses;
    }

    public boolean isPerformOperonDetection() {
        return performOperonDetection;
    }

    public void setPerformOperonDetection(boolean performOperonDetection) {
        this.performOperonDetection = performOperonDetection;
    }

    public boolean isPerformNovelRegionDetection() {
        return performNovelRegionDetection;
    }

    public void setPerformNovelRegionDetection(boolean performNovelRegionDetection) {
        this.performNovelRegionDetection = performNovelRegionDetection;
    }

    public boolean isPerformingRPKMs() {
        return performRPKMs;
    }

    public void setPerformingRPKMs(boolean rPKMs) {
        this.performRPKMs = rPKMs;
    }

    public double getFraction() {
        return fraction;
    }

    public void setFraction(double fraction) {
        this.fraction = fraction;
    }

    public int getMinLengthBoundary() {
        return minLengthBoundary;
    }

    public void setMinLengthBoundary(int minLengthBoundary) {
        this.minLengthBoundary = minLengthBoundary;
    }

    public double getFractionForNewRegionDetection() {
        return fractionForNewRegionDetection;
    }
}
