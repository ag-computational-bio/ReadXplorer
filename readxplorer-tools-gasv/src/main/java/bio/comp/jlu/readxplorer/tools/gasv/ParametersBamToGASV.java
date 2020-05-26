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
 * Parameter set for running BamToGASV for detection of genome rearrangements
 * using read pair data.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersBamToGASV implements ParameterSetI<ParametersBamToGASV> {

    /** Fragment bounds calculation method PCT. */
    public static final String FB_METHOD_PCT = "PCT";
    /** Fragment bounds calculation method SD. */
    public static final String FB_METHOD_SD = "SD";
    /** Fragment bounds calculation method EXACT. */
    public static final String FB_METHOD_EXACT = "EXACT";
    /** Fragment bounds calculation method FILE. */
    public static final String FB_METHOD_FILE = "FILE";

    /** SAM validation stringency silent. */
    public static final String STRINGENCY_SILENT = "silent";
    /** SAM validation stringency lenient. */
    public static final String STRINGENCY_LENIENT = "lenient";
    /** SAM validation stringency strict. */
    public static final String STRINGENCY_STRICT = "strict";

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
    private String samValidationStringency;
    private String fragmentBoundsMethod;


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
     */
    public ParametersBamToGASV( boolean isSolid,
                                boolean isSeparateOutput,
                                boolean isWriteConcordantPairs,
                                boolean isWriteLowQualityPairs,
                                byte minMappingQuality,
                                int maxPairLength,
                                String fragmentBoundsMethod,
                                int distPCTValue,
                                int distSDValue,
                                String distExactValue,
                                String distFile,
                                String samValidationStringency ) {

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
        this( false, false, false, false, Byte.valueOf( "20" ), 10000, FB_METHOD_PCT,
              99, -1, "", "", STRINGENCY_SILENT );
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
    public String getFragmentBoundsMethod() {
        return fragmentBoundsMethod;
    }


    /**
     * @param fragmentBoundsMethod The method for specifying fragment
     *                             distribution bounds.
     */
    public void setFragmentBoundsMethod( String fragmentBoundsMethod ) {
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
    public String getSamValidationStringency() {
        return samValidationStringency;
    }


    /**
     * @param samValidationStringency Validation stringency to use for reading
     *                                data from the bam file.
     */
    public void setSamValidationStringency( String samValidationStringency ) {
        this.samValidationStringency = samValidationStringency;
    }


}
