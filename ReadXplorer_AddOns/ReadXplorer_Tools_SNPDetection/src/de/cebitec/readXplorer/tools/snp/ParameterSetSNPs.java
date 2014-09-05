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
package de.cebitec.readXplorer.tools.snp;

import de.cebitec.readXplorer.databackend.ParameterSetI;
import de.cebitec.readXplorer.databackend.ParametersFeatureTypes;
import de.cebitec.readXplorer.databackend.ParametersReadClasses;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.util.Set;

/**
 * Data storage for all parameters associated with a SNP and DIP detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class ParameterSetSNPs extends ParametersFeatureTypes implements ParameterSetI<ParameterSetSNPs> {
    private int minMismatchBases;
    private int minPercentage;
    private final boolean useMainBase;
    private ParametersReadClasses readClassParams;
    private final byte minBaseQuality;
    private final byte minAverageBaseQual;
    private final int minAverageMappingQual;

    /**
     * Data storage for all parameters associated with a SNP and DIP detection.
     * @param minMismatchBases the minimum number of mismatches at a SNP position 
     * @param minPercentage the minimum percentage of mismatches at a SNP position
     * @param useMainBase <cc>true</cc>, if the minVaryingBases count corresponds to the count of
     * the most frequent base at the current position. <cc>false</cc>, if the 
     * minVaryingBases count corresponds to the overall mismatch count at the
     * current position.
     * @param selFeatureTypes list of seletect feature types to use for the 
     * snp translation.
     * @param readClassParams only include mappings in the analysis, which 
     * belong to the selected mapping classes.
     * @param minBaseQuality Minimum phred scaled base quality or -1 if unknown.
     * @param minAverageBaseQual Minimum average phred scaled base quality or -1 if unknown.
     * @param minAverageMappingQual Minimum average phred scaled mapping quality or -1 if unknown.
     */
    public ParameterSetSNPs(int minMismatchBases, int minPercentage, boolean useMainBase, Set<FeatureType> selFeatureTypes, ParametersReadClasses readClassParams,
            byte minBaseQuality, byte minAverageBaseQual, int minAverageMappingQual) {
        super(selFeatureTypes);
        this.minMismatchBases = minMismatchBases;
        this.minPercentage = minPercentage;
        this.useMainBase = useMainBase;
        this.readClassParams = readClassParams;
        this.minBaseQuality = minBaseQuality;
        this.minAverageBaseQual = minAverageBaseQual;
        this.minAverageMappingQual = minAverageMappingQual;
    }

    /**
     * @return the minimum number of mismatches at a SNP position 
     */
    public int getMinMismatchingBases() {
        return minMismatchBases;
    }

    public void setMinVaryingBases(int minVaryingBases) {
        this.minMismatchBases = minVaryingBases;
    }

    /**
     * @return the minimum percentage of mismatches at a SNP position
     */
    public int getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(int minPercentage) {
        this.minPercentage = minPercentage;
    }

    /**
     * @return only include mappings in the analysis, which 
     * belong to the selected mapping classes.
     */
    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }

    public void setReadClassParams(ParametersReadClasses readClassParams) {
        this.readClassParams = readClassParams;
    }

    /**
     * @return <cc>true</cc>, if the minVaryingBases count corresponds to the count of
     * the most frequent base at the current position. <cc>false</cc>, if the 
     * minVaryingBases count corresponds to the overall mismatch count at the
     * current position.
     */
    public boolean isUseMainBase() {
        return this.useMainBase;
    }

    /**
     * @return Minimum phred scaled base quality or -1 if unknown.
     */
    public byte getMinBaseQuality() {
       return this.minBaseQuality; 
    }

    /**
     * @return Minimum average phred scaled base quality or -1 if unknown.
     */
    public int getMinAverageBaseQual() {
        return this.minAverageBaseQual;
    }

    /**
     * @return Minimum average phred scaled mapping quality or 255 if unknown.
     */
    public int getMinAverageMappingQual() {
        return this.minAverageMappingQual;
    }
    
}
