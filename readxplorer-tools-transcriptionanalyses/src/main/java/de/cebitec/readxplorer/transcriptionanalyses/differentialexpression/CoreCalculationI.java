package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

/**
 *
 * @author Kai Bernd Stadermann
 */
public interface CoreCalculationI {
    
    /**
     * Calculates the regression for one gene.
     * 
     * @param conditionA Per position count data under condition A.
     * @param conditionB Per position count data under condition B.
     * @return The regression coefficient between A and B.
     * @throws IllegalArgumentException if the input arrays have different length.
     */
    public double[] calculate(int[] conditionA, int[] conditionB) throws IllegalArgumentException;
    
}
