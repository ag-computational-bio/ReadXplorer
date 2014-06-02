/* 
 * Copyright (C) 2014 Kai Bernd Stadermann
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
package de.cebitec.readXplorer.differentialExpression.expressTest;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author kstaderm
 */
public interface ExpressTestI {

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
    public void addObserver(ExpressTestObserver o);

    /**
     * Removes a observer listening for results.
     *
     * @param o the observer to be removed.
     */
    public void removeObserver(ExpressTestObserver o);

    /**
     * Returns the results of an Express Test differential expression analysis.
     * The Object array can be handed over directly to a JTable as the rowData
     * argument.
     *
     * @return results of the analysis.
     */
    public Vector<Vector> getResults();
    
    /**
     * Returns the normalized results of an Express Test differential expression analysis.
     * The Object array can be handed over directly to a JTable as the rowData
     * argument.
     *
     * @return normalized results of the analysis.
     */
    public Vector<Vector> getResultsNormalized();

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

    /**
     * Sets features that should be used for normalization, if the user selected
     * some.
     * @param normalizationFeatures features for normalization
     */
    public void setNormalizationFeatures(List<Integer> normalizationFeatures);
}
