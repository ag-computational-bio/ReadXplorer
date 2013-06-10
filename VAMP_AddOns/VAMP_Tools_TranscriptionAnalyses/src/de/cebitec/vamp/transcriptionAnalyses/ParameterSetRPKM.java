/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptionAnalyses;

/**
 *
 * @author Martin Tötsches
 */
public class ParameterSetRPKM {
    private boolean performRPKMAnalysis;
    private int minReads;
    private int maxReads;
    
    public ParameterSetRPKM(boolean performRPKMAnalysis, int minReads, int maxReads) {
        this.performRPKMAnalysis = performRPKMAnalysis;
        this.minReads = minReads;
        this.maxReads = maxReads;
    }

    public boolean isPerformRPKMAnalysis() {
        return performRPKMAnalysis;
    }

    /**
     * @return the minReads
     */
    public int getMinReads() {
        return minReads;
    }

    /**
     * @return the maxReads
     */
    public int getMaxReads() {
        return maxReads;
    }

    /**
     * @param minReads the minReads to set
     */
    public void setMinReads(int minReads) {
        this.minReads = minReads;
    }

    /**
     * @param maxReads the maxReads to set
     */
    public void setMaxReads(int maxReads) {
        this.maxReads = maxReads;
    }
}
