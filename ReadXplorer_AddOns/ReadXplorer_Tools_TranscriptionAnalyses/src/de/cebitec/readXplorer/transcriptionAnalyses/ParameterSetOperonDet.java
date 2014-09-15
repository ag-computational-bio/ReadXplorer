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
package de.cebitec.readXplorer.transcriptionAnalyses;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.ParametersFeatureTypesAndReadClasses;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with an operon detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetOperonDet extends ParametersFeatureTypesAndReadClasses implements ParameterSetI<ParameterSetOperonDet> {
    
    private boolean performOperonAnalysis;
    private int minSpanningReads;
    private boolean autoOperonParamEstimation;

    /**
     * Data storage for all parameters associated with an operon detection.
     * @param performOperonAnalysis true, if the operon analysis should be carried out
     * @param minSpanningReads minimum number of spanning reads between two neighboring
     * features
     * @param autoOperonParamEstimation true, if the automatic parameter estimation should
     * be switched on for the operon detection
     * @param selFeatureTypes the set of selected feature types
     * @param readClassParams
     */
    public ParameterSetOperonDet(boolean performOperonAnalysis, int minSpanningReads, 
            boolean autoOperonParamEstimation, Set<FeatureType> selFeatureTypes, ParametersReadClasses readClassParams) {
        super(selFeatureTypes, readClassParams);
        this.performOperonAnalysis = performOperonAnalysis;
        this.minSpanningReads = minSpanningReads;
        this.autoOperonParamEstimation = autoOperonParamEstimation;
    }

    /**
     * @return true, if the operon analysis should be carried out
     */
    public boolean isPerformOperonAnalysis() {
        return performOperonAnalysis;
    }

    /**
     * @return minimum number of spanning reads between two neighboring
     * features
     */
    public int getMinSpanningReads() {
        return minSpanningReads;
    }

    /**
     * @return true, if the automatic parameter estimation should
     * be switched on for the operon detection
     */
    public boolean isAutoOperonParamEstimation() {
        return autoOperonParamEstimation;
    }

    /**
     * @param performOperonAnalysis true, if the operon analysis should be carried out
     */
    public void setPerformOperonAnalysis(boolean performOperonAnalysis) {
        this.performOperonAnalysis = performOperonAnalysis;
    }

    /**
     * @param minSpanningReads minimum number of spanning reads between two
     * neighboring features
     */
    public void setMinSpanningReads(int minSpanningReads) {
        this.minSpanningReads = minSpanningReads;
    }

    /**
     * @param autoOperonParamEstimation true, if the automatic parameter
     * estimation should be switched on for the operon detection
     */
    public void setAutoOperonParamEstimation(boolean autoOperonParamEstimation) {
        this.autoOperonParamEstimation = autoOperonParamEstimation;
    }
    
}
