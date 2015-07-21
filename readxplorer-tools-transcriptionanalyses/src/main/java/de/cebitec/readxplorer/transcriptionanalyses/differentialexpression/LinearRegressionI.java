/*
 * Copyright (C) 2015 Agne Matuseviciute
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.Map;

/**
 *
 * @author Kai Bernd Stadermann
 */
public interface LinearRegressionI {

    /**
     * This method is called to start the analysis.
     * As a parameter the continuous count data is handed over. The data
     * structure is build as follows:
     * Map<ConditionName, Map<GeneName, List<CountValueForEachSingleNucleotideOfTheGene>>>;
     * 
     * @param countData The count data for each gene.
     */
    void process(Map<Integer, Map<PersistentFeature, int[]>> countData);
    
    /**
     * Returns the result of the analysis run.
     * 
     * @return A Map containing gene names as key and the similarity score as value.
     * @throws IllegalStateException If the method is called before the calculation is finished.
     */
    Map<PersistentFeature, LinearRegressionResult> getResults() throws IllegalStateException;

}
