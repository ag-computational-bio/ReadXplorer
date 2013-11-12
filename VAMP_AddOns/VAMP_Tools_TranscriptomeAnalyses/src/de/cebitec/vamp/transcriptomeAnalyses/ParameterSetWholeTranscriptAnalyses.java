/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.ParameterSetI;

/**
 *
 * @author jritter
 */
class ParameterSetWholeTranscriptAnalyses implements ParameterSetI<ParameterSetWholeTranscriptAnalyses> {
    
    private boolean performWholeTrascriptomeAnalyses, performOperonDetection, performNovelRegionDetection, performRPKMs;
    private double fraction;

    public ParameterSetWholeTranscriptAnalyses(boolean performWholeTrascriptomeAnalyses, boolean performOperonDetection, boolean performNovelRegionDetection, boolean rPKMs, double fraction) {
        this.performWholeTrascriptomeAnalyses = performWholeTrascriptomeAnalyses;
        this.performOperonDetection = performOperonDetection;
        this.performNovelRegionDetection = performNovelRegionDetection;
        this.performRPKMs = rPKMs;
        this.fraction = fraction;
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
}
