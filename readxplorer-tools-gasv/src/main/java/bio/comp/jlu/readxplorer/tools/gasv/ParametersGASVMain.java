/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.tools.gasv;

import de.cebitec.readxplorer.databackend.ParameterSetI;


/**
 * Parameter set for running GASVMain for detection of genome rearrangements
 * using read pair data.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersGASVMain implements ParameterSetI<ParametersGASVMain> {

    public static final String OUT_STANDARD = "standard";
    public static final String OUT_READS = "reads";
    public static final String OUT_REGIONS = "regions";

    private int maxCliqueSize;
    private int maxClusterSize;
    private int maxReadPairs;
    private int minClusterSize;
    private String outputType;
    private boolean isMaxSubClusters;
    private boolean isNonreciprocal;
    private boolean isHeaderless;
    private boolean isFast;
    private boolean isVerbose;
    private boolean isDebug;


    /**
     * Parameter set for running GASVMain for detection of genome rearrangements
     * using read pair data.
     * <p>
     * @param minClusterSize   Min. read pair cluster size for a rearrangement
     *                         or 0 if not used.
     * @param maxClusterSize   Max. read pair cluster size for a rearrangement
     *                         or 0 if not used.
     * @param maxCliqueSize    Max. clique size for Sub cluster calculation or 0
     *                         if not used.
     * @param maxReadPairs     Max. read pairs per window or 0 if not used.
     * @param outputType       Output type among {@link #OUT_STANDARD} and
     *                         {@link #OUT_READS} and {@link #OUT_REGIONS}
     * @param isMaxSubClusters Report maximal sub clusters for components
     *                         without common intersection.
     * @param isNonreciprocal  Use nonreciprocal classifcation. Then only read
     *                         pairs in same orientation are clustered (e.g. +/+
     *                         and -/- are listed separately).
     * @param isHeaderless     Create output file without header.
     * @param isFast           Run faster, but risk out of memory errors.
     * @param isVerbose        Verbose output.
     * @param isDebug          Run GASVMain in debug mode.
     * <p>
     */
    public ParametersGASVMain( int minClusterSize,
                               int maxClusterSize,
                               int maxCliqueSize,
                               int maxReadPairs,
                               String outputType,
                               boolean isMaxSubClusters,
                               boolean isNonreciprocal,
                               boolean isHeaderless,
                               boolean isFast,
                               boolean isVerbose,
                               boolean isDebug ) {

        this.minClusterSize = minClusterSize;
        this.maxClusterSize = maxClusterSize;
        this.maxCliqueSize = maxCliqueSize;
        this.maxReadPairs = maxReadPairs;
        this.outputType = outputType;
        this.isMaxSubClusters = isMaxSubClusters;
        this.isNonreciprocal = isNonreciprocal;
        this.isHeaderless = isHeaderless;
        this.isFast = isFast;
        this.isVerbose = isVerbose;
        this.isDebug = isDebug;
    }


    /**
     * Convenience constructor for creating a default parameter set.
     */
    public ParametersGASVMain() {
        this( 30, 0, 0, 0, ParametersGASVMain.OUT_STANDARD, false, false, false, false, false, false );
    }


    /**
     * @return Min. read pair cluster size for a rearrangement or 0 if not used.
     */
    public int getMinClusterSize() {
        return minClusterSize;
    }


    /**
     * @param minClusterSize Min. read pair cluster size for a rearrangement or
     *                       0 if not used.
     */
    public void setMinClusterSize( int minClusterSize ) {
        this.minClusterSize = minClusterSize;
    }


    /**
     * @return Max. read pair cluster size for a rearrangement or 0 if not used.
     */
    public int getMaxClusterSize() {
        return maxClusterSize;
    }


    /**
     * @param maxClusterSize Max. read pair cluster size for a rearrangement or
     *                       0 if not used.
     */
    public void setMaxClusterSize( int maxClusterSize ) {
        this.maxClusterSize = maxClusterSize;
    }


    /**
     * @return Max. clique size for Sub cluster calculation or 0 if not used.
     */
    public int getMaxCliqueSize() {
        return maxCliqueSize;
    }


    /**
     * @param maxCliqueSize Max. clique size for Sub cluster calculation or 0 if
     *                      not used.
     */
    public void setMaxCliqueSize( int maxCliqueSize ) {
        this.maxCliqueSize = maxCliqueSize;
    }


    /**
     * @return Max. read pairs per window or 0 if not used.
     */
    public int getMaxReadPairs() {
        return maxReadPairs;
    }


    /**
     * @param maxReadPairs Max. read pairs per window or 0 if not used.
     */
    public void setMaxReadPairs( int maxReadPairs ) {
        this.maxReadPairs = maxReadPairs;
    }


    /**
     * @return Output type among {@link #OUT_STANDARD} and {@link #OUT_READS}
     *         and {@link #OUT_REGIONS}
     */
    public String getOutputType() {
        return outputType;
    }


    /**
     * @param outputType Output type among {@link #OUT_STANDARD} and
     *                   {@link #OUT_READS} and {@link #OUT_REGIONS}
     */
    public void setOutputType( String outputType ) {
        this.outputType = outputType;
    }


    /**
     * @return Report maximal sub clusters for components without common
     *         intersection.
     */
    public boolean isMaxSubClusters() {
        return isMaxSubClusters;
    }


    /**
     * @param isMaxSubClusters Report maximal sub clusters for components
     *                         without common intersection.
     */
    public void setIsMaxSubClusters( boolean isMaxSubClusters ) {
        this.isMaxSubClusters = isMaxSubClusters;
    }


    /**
     * @return Use nonreciprocal classifcation. Then only read pairs in same
     *         orientation are clustered (e.g. +/+ and -/- are listed
     *         separately).
     */
    public boolean isNonreciprocal() {
        return isNonreciprocal;
    }


    /**
     * @param isNonreciprocal Use nonreciprocal classifcation. Then only read
     *                        pairs in same orientation are clustered (e.g. +/+
     *                        and -/- are listed separately).
     */
    public void setIsNonreciprocal( boolean isNonreciprocal ) {
        this.isNonreciprocal = isNonreciprocal;
    }


    /**
     * @return Create output file without header.
     */
    public boolean isHeaderless() {
        return isHeaderless;
    }


    /**
     * @param isHeaderless Create output file without header.
     */
    public void setIsHeaderless( boolean isHeaderless ) {
        this.isHeaderless = isHeaderless;
    }


    /**
     * @return Run faster, but risk out of memory errors.
     */
    public boolean isFast() {
        return isFast;
    }


    /**
     * @param isFast Run faster, but risk out of memory errors.
     */
    public void setIsFast( boolean isFast ) {
        this.isFast = isFast;
    }


    /**
     * @return Verbose output.
     */
    public boolean isVerbose() {
        return isVerbose;
    }


    /**
     * @param isVerbose Verbose output.
     */
    public void setIsVerbose( boolean isVerbose ) {
        this.isVerbose = isVerbose;
    }


    /**
     * @return Run GASVMain in debug mode.
     */
    public boolean isDebug() {
        return isDebug;
    }


    /**
     * @param isDebug Run GASVMain in debug mode.
     */
    public void setIsDebug( boolean isDebug ) {
        this.isDebug = isDebug;
    }


}
