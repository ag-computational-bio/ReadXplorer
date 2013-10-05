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
public class ParameterSetFiveEnrichedAnalyses implements ParameterSetI<ParameterSetFiveEnrichedAnalyses> {

    private boolean performTSSAnalysis;
    private boolean performLeaderlessAnalysis;
    private boolean performAntisenseAnalysis;
    private Double fraction;
    private Integer ratio;
    private Integer upstreamRegion, downstreamRegion;

    /**
     * Data storage for all parameters associated with a transcription analysis.
     *
     * @param performTSSAnalysis
     * @param performLeaderlessAnalysis
     * @param performAntisenseAnalysis
     * @param fraction
     */
    public ParameterSetFiveEnrichedAnalyses(boolean performTSSAnalysis, boolean performLeaderlessAnalysis, boolean performAntisenseAnalysis, Double fraction, Integer ratio, Integer upstream, Integer downstream) {
        this.performTSSAnalysis = performTSSAnalysis;
        this.performLeaderlessAnalysis = performLeaderlessAnalysis;
        this.performAntisenseAnalysis = performAntisenseAnalysis;
        this.fraction = fraction;
        this.upstreamRegion = upstream;
        this.downstreamRegion = downstream;
        this.ratio = ratio;

    }

    public boolean isPerformTSSAnalysis() {
        return performTSSAnalysis;
    }

    public void setPerformTSSAnalysis(boolean performTSSAnalysis) {
        this.performTSSAnalysis = performTSSAnalysis;
    }

    public boolean isPerformLeaderlessAnalysis() {
        return performLeaderlessAnalysis;
    }

    public void setPerformLeaderlessAnalysis(boolean performLeaderlessAnalysis) {
        this.performLeaderlessAnalysis = performLeaderlessAnalysis;
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
}
