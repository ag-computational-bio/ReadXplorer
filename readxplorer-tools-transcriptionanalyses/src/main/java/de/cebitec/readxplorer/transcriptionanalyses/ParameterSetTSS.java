/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.databackend.ParameterSetI;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;


/**
 * Data storage for all parameters associated with a transcription analysis.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetTSS implements ParameterSetI<ParameterSetTSS> {

    private boolean performTSSAnalysis;
    private boolean autoTssParamEstimation;
    private boolean performUnannotatedTranscriptDet;
    private boolean isAssociateTss;
    private int minNoReadStarts;
    private int minPercentIncrease;
    private int maxLowCovInitCount;
    private int minLowCovIncrease;
    private int minTranscriptExtensionCov;
    private int maxLeaderlessDistance;
    private int maxFeatureDistance;
    private int associateTssWindow;
    private ParametersReadClasses readClassParams;


    /**
     * Data storage for all parameters associated with a transcription analysis.
     * <p>
     * @param performTSSAnalysis
     * @param autoTssParamEstimation
     * @param performUnannotatedTranscriptDet
     * @param minNoReadStarts
     * @param minPercentIncrease
     * @param maxLowCovInitCount
     * @param minLowCovIncrease
     * @param maxLeaderlessDistance
     * @param minTranscriptExtensionCov
     * @param isAssociateTss
     * @param associateTssWindow
     * @param readClassParams
     */
    public ParameterSetTSS( boolean performTSSAnalysis, boolean autoTssParamEstimation, boolean performUnannotatedTranscriptDet,
                     int minNoReadStarts, int minPercentIncrease, int maxLowCovInitCount, int minLowCovIncrease,
                     int minTranscriptExtensionCov, int maxLeaderlessDistance, int maxFeatureDistance, boolean isAssociateTss,
                     int associateTssWindow, ParametersReadClasses readClassParams ) {
        this.performTSSAnalysis = performTSSAnalysis;
        this.autoTssParamEstimation = autoTssParamEstimation;
        this.performUnannotatedTranscriptDet = performUnannotatedTranscriptDet;
        this.minNoReadStarts = minNoReadStarts;
        this.minPercentIncrease = minPercentIncrease;
        this.maxLowCovInitCount = maxLowCovInitCount;
        this.minLowCovIncrease = minLowCovIncrease;
        this.minTranscriptExtensionCov = minTranscriptExtensionCov;
        this.maxLeaderlessDistance = maxLeaderlessDistance;
        this.maxFeatureDistance = maxFeatureDistance;
        this.isAssociateTss = isAssociateTss;
        this.associateTssWindow = associateTssWindow;
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


    public int getMaxFeatureDistance() {
        return this.maxFeatureDistance;
    }


    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }


    public void setPerformTSSAnalysis( boolean performTSSAnalysis ) {
        this.performTSSAnalysis = performTSSAnalysis;
    }


    public void setAutoTssParamEstimation( boolean autoTssParamEstimation ) {
        this.autoTssParamEstimation = autoTssParamEstimation;
    }


    public void setPerformUnannotatedTranscriptDet( boolean performUnannotatedTranscriptDet ) {
        this.performUnannotatedTranscriptDet = performUnannotatedTranscriptDet;
    }


    public void setMinNoReadStarts( int minNoReadStarts ) {
        this.minNoReadStarts = minNoReadStarts;
    }


    public void setMinPercentIncrease( int minPercentIncrease ) {
        this.minPercentIncrease = minPercentIncrease;
    }


    public void setMaxLowCovInitCount( int maxLowCovInitCount ) {
        this.maxLowCovInitCount = maxLowCovInitCount;
    }


    public void setMinLowCovIncrease( int minLowCovIncrease ) {
        this.minLowCovIncrease = minLowCovIncrease;
    }


    public void setMinTranscriptExtensionCov( int minTranscriptExtensionCov ) {
        this.minTranscriptExtensionCov = minTranscriptExtensionCov;
    }


    public void setMaxLeaderlessDistance( int maxLeaderlessDistance ) {
        this.maxLeaderlessDistance = maxLeaderlessDistance;
    }


    public void setMaxFeatureDistance( int maxFeatureDistance ) {
        this.maxFeatureDistance = maxFeatureDistance;
    }


    public void setReadClassParams( ParametersReadClasses readClassParams ) {
        this.readClassParams = readClassParams;
    }


    /**
     * @return <code>true</code>, if TSS within the given window shall be
     * associated, <code>false</code> otherwise.
     */
    public boolean isAssociateTss() {
        return isAssociateTss;
    }


    /**
     * @param isAssociateTss <code>true</code>, if TSS within the given window shall
     * be associated, <code>false</code> otherwise.
     */
    public void setIsAssociateTss(boolean isAssociateTss) {
        this.isAssociateTss = isAssociateTss;
    }


    /**
     * @return Bp window to use for associating TSS.
     */
    public int getAssociateTssWindow() {
        return associateTssWindow;
    }


    /**
     * @param associateTssWindow Bp window to use for associating TSS.
     */
    public void setAssociateTssWindow(int associateTssWindow) {
        this.associateTssWindow = associateTssWindow;
    }


}
