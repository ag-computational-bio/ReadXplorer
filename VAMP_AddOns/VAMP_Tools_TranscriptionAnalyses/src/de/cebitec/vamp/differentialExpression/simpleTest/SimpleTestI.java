package de.cebitec.vamp.differentialExpression.simpleTest;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import java.util.Vector;

/**
 *
 * @author kstaderm
 */
public interface SimpleTestI {

    /**
     * Starts a new analysis on the given count data. Each Integer array in a
     *
     * @param groupA countData of group A.
     * @param groupB countData of group B.
     * @throws IllegalArgumentException if not all arrays have a proper
     * dimension
     */
    public void performAnalysis(PersistantFeature[] regionNames, int[] start, int[] stop, int[][] groupA, int[][] groupB, double cutOff) throws IllegalArgumentException;

    /**
     * Adds a new observer listening for results.
     *
     * @param o the observer to be added.
     */
    public void addObserver(SimpleTestObserver o);

    /**
     * Removes a observer listening for results.
     *
     * @param o the observer to be removed.
     */
    public void removeObserver(SimpleTestObserver o);

    /**
     * Returns the results of a Simple Test differential expression analysis.
     * The Object array can be handed over directly to a JTable as the rowData
     * argument.
     *
     * @return results of the analysis.
     */
    public Vector<Vector> getResults();

    /**
     * Returns the columnNames fitting the analysis run.
     *
     * @return columnNames for the current analysis run.
     */
    public Vector getColumnNames();

    /**
     * Returns the rowNames fitting the analysis run. 
     *
     * @return columnNames for the current analysis run.
     */
    public Vector getRowNames();
}
