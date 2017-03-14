/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.expresstest;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.List;


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
     * <p>
     * @throws IllegalArgumentException if not all arrays have a proper
     *                                  dimension
     */
    void performAnalysis( PersistentFeature[] regionNames, int[] start, int[] stop, int[][] groupA, int[][] groupB, double cutOff ) throws IllegalArgumentException;


    /**
     * Adds a new observer listening for results.
     *
     * @param o the observer to be added.
     */
    void addObserver( ExpressTestObserver o );


    /**
     * Removes a observer listening for results.
     *
     * @param o the observer to be removed.
     */
    void removeObserver( ExpressTestObserver o );


    /**
     * Returns the results of an Express Test differential expression analysis.
     * The Object array can be handed over directly to a JTable as the rowData
     * argument.
     *
     * @return results of the analysis.
     */
    List<List<Object>> getResults();


    /**
     * Returns the normalized results of an Express Test differential expression
     * analysis.
     * The Object array can be handed over directly to a JTable as the rowData
     * argument.
     *
     * @return normalized results of the analysis.
     */
    List<List<Object>> getResultsNormalized();


    /**
     * Returns the columnNames fitting the analysis run.
     *
     * @return columnNames for the current analysis run.
     */
    List<Object> getColumnNames();


    /**
     * Returns the rowNames fitting the analysis run.
     *
     * @return columnNames for the current analysis run.
     */
    List<Object> getRowNames();


    /**
     * Sets features that should be used for normalization, if the user selected
     * some.
     * <p>
     * @param normalizationFeatures features for normalization
     */
    void setNormalizationFeatures( int[] normalizationFeatures );


}
