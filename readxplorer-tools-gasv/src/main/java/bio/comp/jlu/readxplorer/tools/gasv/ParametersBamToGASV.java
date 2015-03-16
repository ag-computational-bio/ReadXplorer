/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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
 * Parameter set for running BamToGASV for detection of genome rearrangements
 * using read pair data.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersBamToGASV implements ParameterSetI<ParametersBamToGASV> {

    private boolean isSeparateOutput;
    private boolean isSolid;
    private boolean isWriteConcordantPairs;
    private boolean isWriteLowQualityPairs;
    private byte minMappingQuality;
    private int maxPairLength;
    private int distPCTValue;
    private int distSDValue;
    private String distExactValue;
    private String distFile;
    private ParametersBamToGASV.SamValidationStringency samValidationStringency;
    private FragmentBoundsMethod fragmentBoundsMethod;


    /**
     * Parameter set for running BamToGASV for detection of genome
     * rearrangements using read pair data.
     * <p>
     * @param isSolid                 true, if input is from SOLiD like
     *                                sequencing platform, false if it is from
     *                                an Illumina like sequencing plattform
     *                                (default).
     * @param isSeparateOutput        true, if a separate output file shall be
     *                                created for each library contained in the
     *                                analyzed bam file, false if only one
     *                                output file is created per bam file.
     * @param isWriteConcordantPairs  true, if concordant read pairs shall be
     *                                written to a file, false if not.
     * @param isWriteLowQualityPairs  true, if low quality read pairs shall be
     *                                written to a file, false if not.
     * @param minMappingQuality       Minimum mapping quality of reads to be
     *                                considered during the analysis.
     * @param fragmentBoundsMethod    The method for specifying fragment
     *                                distribution bounds.
     * @param maxPairLength           Max length of pairs to consider for
     *                                fragment distribution bounds calculation.
     * @param distPCTValue            Percentage quantile (called PCT in GASV)
     *                                to use for clustering read pairs (A value
     *                                between 1-99).
     * @param distSDValue             Standard deviation above mean (called SD
     *                                in GASV) to use for clustering read pairs.
     * @param distExactValue          Exact LMin and LMax distance String to use
     *                                for clustering read pairs of the form
     *                                "LMin,LMax", e.g. 100,400.
     * @param distFile                A file specifying different cutoffs to use
     *                                for clustering read pairs for multiple
     *                                sequencing libraries.
     * @param samValidationStringency Validation stringency to use for reading
     *                                data from the bam file.
     * <p>
     */
    public ParametersBamToGASV( boolean isSolid,
                                boolean isSeparateOutput,
                                boolean isWriteConcordantPairs,
                                boolean isWriteLowQualityPairs,
                                byte minMappingQuality,
                                int maxPairLength,
                                FragmentBoundsMethod fragmentBoundsMethod,
                                int distPCTValue,
                                int distSDValue,
                                String distExactValue,
                                String distFile,
                                ParametersBamToGASV.SamValidationStringency samValidationStringency ) {

        this.isSolid = isSolid;
        this.isSeparateOutput = isSeparateOutput;
        this.isWriteConcordantPairs = isWriteConcordantPairs;
        this.isWriteLowQualityPairs = isWriteLowQualityPairs;
        this.minMappingQuality = minMappingQuality;
        this.maxPairLength = maxPairLength;
        this.fragmentBoundsMethod = fragmentBoundsMethod;
        this.distPCTValue = distPCTValue;
        this.distSDValue = distSDValue;
        this.distExactValue = distExactValue;
        this.distFile = distFile;
        this.samValidationStringency = samValidationStringency;

    }


    /**
     * Constructor for creating a default value parameter set.
     */
    public ParametersBamToGASV() {
        this( false, false, false, false, new Byte( "20" ), 10000, FragmentBoundsMethod.PCT,
              99, -1, "", "", SamValidationStringency.SILENT );
    }


    /**
     * @param isSolid true, if input is from SOLiD like sequencing platform,
     *                false if it is from an Illumina like sequencing plattform
     *                (default).
     */
    public void setPlatform( boolean isSolid ) {
        this.isSolid = isSolid;
    }


    /**
     * @return true, if input is from SOLiD like sequencing platform, false if
     *         it is from an Illumina like sequencing plattform (default).
     */
    public boolean getPlatform() {
        return isSolid;
    }


    /**
     * @param isSeparateOutput true, if a separate output file shall be created
     *                         for each library contained in the analyzed bam
     *                         file, false if only one output file is created
     *                         per bam file.
     */
    public void setLibrarySeparated( boolean isSeparateOutput ) {
        this.isSeparateOutput = isSeparateOutput;
    }


    /**
     * @return true, if a separate output file shall be created for each library
     *         contained in the analyzed bam file, false if only one output file
     *         is created per bam file.
     */
    public boolean isLibrarySeparated() {
        return isSeparateOutput;
    }


    /**
     * @param isWriteConcordantPairs true, if concordant read pairs shall be
     *                               written to a file, false if not.
     */
    public void setWriteConcordantPairs( boolean isWriteConcordantPairs ) {
        this.isWriteConcordantPairs = isWriteConcordantPairs;
    }


    /**
     * @return true, if concordant read pairs shall be written to a file, false
     *         if not.
     */
    public boolean isWriteConcordantPairs() {
        return isWriteConcordantPairs;
    }


    /**
     * @param isWriteLowQualityPairs true, if low quality read pairs shall be
     *                               written to a file, false if not.
     */
    public void setWriteLowQualityPairs( boolean isWriteLowQualityPairs ) {
        this.isWriteLowQualityPairs = isWriteLowQualityPairs;
    }


    /**
     * @return true, if low quality read pairs shall be written to a file, false
     *         if not.
     */
    public boolean isWriteLowQualityPairs() {
        return isWriteLowQualityPairs;
    }


    /**
     * @return Minimum mapping quality of reads to be considered during the
     *         analysis.
     */
    public byte getMinMappingQuality() {
        return minMappingQuality;
    }


    /**
     * @param minMappingQuality Minimum mapping quality of reads to be
     *                          considered during the analysis.
     */
    public void setMinMappingQuality( byte minMappingQuality ) {
        this.minMappingQuality = minMappingQuality;
    }


    /**
     * @return Max length of pairs to consider for fragment distribution bounds
     *         calculation.
     */
    public int getMaxPairLength() {
        return maxPairLength;
    }


    /**
     * @param maxPairLength Max length of pairs to consider for fragment
     *                      distribution bounds calculation.
     */
    public void setMaxPairLength( int maxPairLength ) {
        this.maxPairLength = maxPairLength;
    }


    /**
     * @return The method for specifying fragment distribution bounds.
     */
    public FragmentBoundsMethod getFragmentBoundsMethod() {
        return fragmentBoundsMethod;
    }


    /**
     * @param fragmentBoundsMethod The method for specifying fragment
     *                             distribution bounds.
     */
    public void setFragmentBoundsMethod( FragmentBoundsMethod fragmentBoundsMethod ) {
        this.fragmentBoundsMethod = fragmentBoundsMethod;
    }


    /**
     * @return Percentage quantile (called PCT in GASV) to use for clustering
     *         read pairs (A value between 1-99).
     */
    public int getDistPCTValue() {
        return distPCTValue;
    }


    /**
     * @param distPCTValue Percentage quantile (called PCT in GASV) to use for
     *                     clustering read pairs (A value between 1-99).
     */
    public void setDistPCTValue( int distPCTValue ) {
        this.distPCTValue = distPCTValue;
    }


    /**
     * @return Standard deviation above mean (called SD in GASV) to use for
     *         clustering read pairs.
     */
    public int getDistSDValue() {
        return distSDValue;
    }


    /**
     * @param distSDValue Standard deviation above mean (called SD in GASV) to
     *                    use for clustering read pairs.
     */
    public void setDistSDValue( int distSDValue ) {
        this.distSDValue = distSDValue;
    }


    /**
     * @return Exact LMin and LMax distance String to use for clustering read
     *         pairs of the form "LMin,LMax", e.g. 100,400.
     */
    public String getDistExactValue() {
        return distExactValue;
    }


    /**
     * @param distExactValue Exact LMin and LMax distance String to use for
     *                       clustering read pairs of the form "LMin,LMax", e.g.
     *                       100,400.
     */
    public void setDistExactValue( String distExactValue ) {
        this.distExactValue = distExactValue;
    }


    /**
     * @return A file specifying different cutoffs to use for clustering read
     *         pairs for multiple sequencing libraries.
     */
    public String getDistFile() {
        return distFile;
    }


    /**
     * @param distFile A file specifying different cutoffs to use for clustering
     *                 read pairs for multiple sequencing libraries.
     */
    public void setDistFile( String distFile ) {
        this.distFile = distFile;
    }


    /**
     * @return Validation stringency to use for reading data from the bam file.
     */
    public ParametersBamToGASV.SamValidationStringency getSamValidationStringency() {
        return samValidationStringency;
    }


    /**
     * @param samValidationStringency Validation stringency to use for reading
     *                                data from the bam file.
     */
    public void setSamValidationStringency( ParametersBamToGASV.SamValidationStringency samValidationStringency ) {
        this.samValidationStringency = samValidationStringency;
    }


    /**
     * Enumeration of fragment the methods for specifying fragment distribution
     * bounds.
     */
    public enum FragmentBoundsMethod {

        /** Percentage quantile method. */
        PCT( FragmentBoundsMethod.PCT_STRING ),
        /** Standard deviation above
         * mean method. */
        SD( FragmentBoundsMethod.SD_STRING ),
        /** EXACT LMin and LMax value
         * method. */
        EXACT( FragmentBoundsMethod.EXACT_STRING ),
        /** Use file with
         * different cutoffs for each library. */
        FILE( FragmentBoundsMethod.FILE_STRING );

        private static final String PCT_STRING = "PCT";
        private static final String SD_STRING = "SD";
        private static final String EXACT_STRING = "EXACT";
        private static final String FILE_STRING = "FILE";

        private String typeString;


        private FragmentBoundsMethod( String typeString ) {
            this.typeString = typeString;
        }


        /**
         * @return The String value of the type of the current fragment bounds
         *         method.
         */
        public String getTypeString() {
            return typeString;
        }


        /**
         * @param type Type of FragmentBoundsMethod to return. If the type does
         *             not match a method or is <code>null</code>, {@link #PCT}
         *             is returned.
         */
        public static FragmentBoundsMethod getMethodType( String type ) {
            if( type == null ) {
                return PCT;
            }
            switch( type ) {
                case SD_STRING:
                    return SD;
                case EXACT_STRING:
                    return EXACT;
                case FILE_STRING:
                    return FILE;
                case PCT_STRING: //fallthrough to default
                default:
                    return PCT;
            }
        }


    }


    /**
     * Enumeration of SAM validation stringencies.
     */
    public enum SamValidationStringency {

        /** Percentage quantile method. */
        SILENT( SamValidationStringency.SILENT_STRING ),
        /** Standard deviation above mean method. */
        LENIENT( SamValidationStringency.LENIENT_STRING ),
        /** EXACT LMin and LMax value method. */
        STRICT( SamValidationStringency.STRICT_STRING );

        private static final String SILENT_STRING = "silent";
        private static final String LENIENT_STRING = "lenient";
        private static final String STRICT_STRING = "strict";

        private String typeString;


        private SamValidationStringency( String typeString ) {
            this.typeString = typeString;
        }


        /**
         * @return The String representation of the type of the current Sam
         *         validation stringency.
         */
        public String getTypeString() {
            return typeString;
        }


        /**
         * @param type Type of SamValidationStringency to return. If the type
         *             does not match a stringency or is <code>null</code>,
         *             {@link #SILENT} is returned.
         */
        public static SamValidationStringency getMethodType( String type ) {
            if( type == null ) {
                return SILENT;
            }
            switch( type ) {
                case LENIENT_STRING:
                    return LENIENT;
                case STRICT_STRING:
                    return STRICT;
                case SILENT_STRING:  //fallthrough to default
                default:
                    return SILENT;
            }
        }


    }

}
