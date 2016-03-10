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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.ParameterSetI;
import de.cebitec.readxplorer.databackend.ParametersFeatureTypesAndReadClasses;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import java.util.Set;


/**
 * Data storage for all parameters associated with an read count and normalized
 * read count (RPKM and TPM) calculation.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParameterSetNormalization extends ParametersFeatureTypesAndReadClasses
        implements ParameterSetI<ParameterSetNormalization> {

    private final boolean performNormalization;
    private final boolean useEffectiveLength;
    private int minReadCount;
    private int maxReadCount;


    /**
     * Data storage for all parameters associated with an read count and
     * normalized read count (RPKM and TPM) calculation.
     * <p>
     * @param performNormalization true, if the normalization analysis shall be
     *                             performed
     * @param minReadCount         minimum read count of a feature to return it
     *                             in the result
     * @param maxReadCount         maximum read count of a feature to return it
     *                             in the result
     * @param useEffectiveLength   true, if the effective feature length should
     *                             be used, false if the total feature length
     *                             should be used
     * @param featureStartOffset   The start offset making genomic features
     *                             start further upstream
     * @param featureStopOffset    The stop offset making genomic features end
     *                             further downstream
     * @param selFeatureTypes      The set of selected feature types
     * @param readClassParams      The read classification parameters
     */
    public ParameterSetNormalization( boolean performNormalization, int minReadCount, int maxReadCount,
                                      boolean useEffectiveLength,
                                      int featureStartOffset,
                                      int featureStopOffset,
                                      Set<FeatureType> selFeatureTypes,
                                      ParametersReadClasses readClassParams ) {

        super( featureStartOffset, featureStopOffset, selFeatureTypes, readClassParams );
        this.performNormalization = performNormalization;
        this.minReadCount = minReadCount;
        this.useEffectiveLength = useEffectiveLength;
        this.maxReadCount = maxReadCount;
    }


    /**
     * @return true, if the effective feature length should be used, false if
     *         the total feature length should be used
     */
    public boolean isUseEffectiveLength() {
        return useEffectiveLength;
    }


    /**
     * @return true, if the normalization analysis shall be performed
     */
    public boolean isPerformNormAnalysis() {
        return performNormalization;
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
     * @param minReadCount the minimum read count of a feature to return it in
     *                     the result
     */
    public void setMinReadCount( int minReadCount ) {
        this.minReadCount = minReadCount;
    }


    /**
     * @param maxReadCount the maximum read count of a feature to return it in
     *                     the result
     */
    public void setMaxReadCount( int maxReadCount ) {
        this.maxReadCount = maxReadCount;
    }


}
