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
 * Data storage for all parameters associated with an RPKM and read count 
 * calculation.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetRPKM extends ParametersFeatureTypesAndReadClasses implements ParameterSetI<ParameterSetRPKM> {
    
    private boolean performRPKMAnalysis;
    private int minReadCount;
    private int maxReadCount;
    
    /**
     * Data storage for all parameters associated with an RPKM and read count
     * calculation.
     * @param performRPKMAnalysis true, if the RPKM analysis shall be performed
     * @param minReadCount minimum read count of a feature to return it in the result
     * @param maxReadCount maximum read count of a feature to return it in the result
     * @param selFeatureTypes the set of selected feature types
     * @param readClassParams
     */
    public ParameterSetRPKM(boolean performRPKMAnalysis, int minReadCount, int maxReadCount, Set<FeatureType> selFeatureTypes, ParametersReadClasses readClassParams) {
        
        super(selFeatureTypes, readClassParams);
        this.performRPKMAnalysis = performRPKMAnalysis;
        this.minReadCount = minReadCount;
        this.maxReadCount = maxReadCount;
    }

    /**
     * @return true, if the RPKM analysis shall be performed
     */
    public boolean isPerformRPKMAnalysis() {
        return performRPKMAnalysis;
    }

    /**
     * @return the minimum read count of a feature to return it in the result
     */
    public int getMinReadCount() {
        return minReadCount;
    }

    /**
     * @return the maximum read count of a feature to return it in the result
     */
    public int getMaxReadCount() {
        return maxReadCount;
    }

    /**
     * @param minRPKM the minimum read count of a feature to return it in the result
     */
    public void setMinReadCount(int minRPKM) {
        this.minReadCount = minRPKM;
    }

    /**
     * @param maxRPKM the maximum read count of a feature to return it in the result
     */
    public void setMaxReadCount(int maxRPKM) {
        this.maxReadCount = maxRPKM;
    }    

}
