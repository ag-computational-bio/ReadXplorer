/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

/**
 *
 * @author jritter
 */
public class AnalysesContainer {

    private final TssDetection analysisTSS;
    private final OperonDetection analysisOperon;

    /**
     * Container class for all available transcription analyses.
     */
    public AnalysesContainer(TssDetection analysisTSS, OperonDetection analysisOperon) {
        this.analysisTSS = analysisTSS;
        this.analysisOperon = analysisOperon;
//        this.analysisRPKM = analysisRPKM;
    }

    /**
     * @return The transcription start site analysis stored in this container
     */
    public TssDetection getAnalysisTSS() {
        return analysisTSS;
    }

    /**
     * @return The operon detection stored in this container
     */
    public OperonDetection getAnalysisOperon() {
        return analysisOperon;
    }

//    public RPKMValuesCalculation getAnalysisRPKM() {
//        return analysisRPKM;
//    }
}
