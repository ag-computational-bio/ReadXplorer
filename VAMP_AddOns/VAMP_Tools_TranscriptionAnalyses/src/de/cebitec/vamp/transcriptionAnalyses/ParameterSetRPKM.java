package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ParameterSetI;

/**
 *
 * @author Martin Tötsches
 */
public class ParameterSetRPKM implements ParameterSetI<ParameterSetRPKM> {
    
    private boolean performRPKMAnalysis;
    private int minRPRM;
    private int maxRPKM;
    
    public ParameterSetRPKM(boolean performRPKMAnalysis, int minRPKM, int maxRPKM) {
        this.performRPKMAnalysis = performRPKMAnalysis;
        this.minRPRM = minRPKM;
        this.maxRPKM = maxRPKM;
    }

    public boolean isPerformRPKMAnalysis() {
        return performRPKMAnalysis;
    }

    /**
     * @return the minRPKM
     */
    public int getMinRPKM() {
        return minRPRM;
    }

    /**
     * @return the maxRPKM
     */
    public int getMaxRPKM() {
        return maxRPKM;
    }

    /**
     * @param minRPKM the minRPKM to set
     */
    public void setMinRPKM(int minRPKM) {
        this.minRPRM = minRPKM;
    }

    /**
     * @param maxRPKM the maxRPKM to set
     */
    public void setMaxRPKM(int maxRPKM) {
        this.maxRPKM = maxRPKM;
    }
}
