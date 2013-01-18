package de.cebitec.vamp.transcriptionAnalyses;

/**
 * Data storage for all parameters associated with filtering features.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetFilteredFeatures {
    
    private boolean performFilterAnalysis;
    private int minNumberReads;
    private int maxNumberReads;

    /**
     * Data storage for all parameters associated with filtering features.
     * @param performFilterAnalysis true, if the filtering should be carries out
     * @param minNumberReads the minimum number of reads an feature has to contain
     * @param maxNumberReads  the maximum number of reads an feature has to contain
     * 
     */
    public ParameterSetFilteredFeatures(boolean performFilterAnalysis, int minNumberReads, int maxNumberReads) {
        this.performFilterAnalysis = performFilterAnalysis;
        this.minNumberReads = minNumberReads;
        this.maxNumberReads = maxNumberReads;
    }

    public boolean isPerformFilterAnalysis() {
        return performFilterAnalysis;
    }

    public int getMinNumberReads() {
        return minNumberReads;
    }

    public int getMaxNumberReads() {
        return maxNumberReads;
    }

    public void setPerformFilterAnalysis(boolean performFilterAnalysis) {
        this.performFilterAnalysis = performFilterAnalysis;
    }

    public void setMinNumberReads(int minNumberReads) {
        this.minNumberReads = minNumberReads;
    }

    public void setMaxNumberReads(int maxNumberReads) {
        this.maxNumberReads = maxNumberReads;
    }
    
}
