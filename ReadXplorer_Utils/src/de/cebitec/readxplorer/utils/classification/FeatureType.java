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

package de.cebitec.readxplorer.utils.classification;


/**
 * Enumeration for all different features types used in readXplorer. This does
 * not only
 * include genetic features, but also features displayed in viewers.
 * Each type is created with an integer and a string representation and
 * it can return values for both.
 * It also allows to return the feature type represented by a given integer
 * or string.
 * <p>
 * @author ddoppmeier, rhilker
 */
public enum FeatureType implements Classification {

    /**
     * getType() returns '-1' = To be used if feature type does not matter.
     */
    ANY( FeatureType.ANY_INT, FeatureType.ANY_STRING ),
    /**
     * getType() returns '0' = To be used for a feature with unknown type.
     */
    UNDEFINED( FeatureType.UNDEFINED_INT, FeatureType.UNDEFINED_STRING ),
    /**
     * getType() returns '1' = To be used for mRNAs.
     */
    MRNA( FeatureType.MRNA_INT, FeatureType.MRNA_STRING ),
    /**
     * getType() returns '2' = To be used for coding sequences.
     */
    CDS( FeatureType.CDS_INT, FeatureType.CDS_STRING ),
    /**
     * getType() returns '3' = To be used for misc rnas.
     */
    MISC_RNA( FeatureType.MISC_RNA_INT, FeatureType.MISC_RNA_STRING ),
    /**
     * getType() returns '4' = To be used for rRNAs.
     */
    RRNA( FeatureType.RRNA_INT, FeatureType.RRNA_STRING ),
    /**
     * getType() returns '5' = To be used for repeat units.
     */
    REPEAT_UNIT( FeatureType.REPEAT_UNIT_INT, FeatureType.REPEAT_UNIT_STRING ),
    /**
     * getType() returns '6' = To be used for sources.
     */
    SOURCE( FeatureType.SOURCE_INT, FeatureType.SOURCE_STRING ),
    /**
     * getType() returns '7' = To be used for tRNAs.
     */
    TRNA( FeatureType.TRNA_INT, FeatureType.TRNA_STRING ),
    /**
     * getType() returns '8' = To be used for genes.
     */
    GENE( FeatureType.GENE_INT, FeatureType.GENE_STRING ),
    /**
     * getType() returns '9' = To be used for micro RNAs.
     */
    MIRNA( FeatureType.MIRNA_INT, FeatureType.MIRNA_STRING ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    EXON( FeatureType.EXON_INT, FeatureType.EXON_STRING ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    FIVE_UTR( FeatureType.FIVE_UTR_INT, FeatureType.FIVE_UTR_STRING ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    THREE_UTR( FeatureType.THREE_UTR_INT, FeatureType.THREE_UTR_STRING ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    NC_RNA( FeatureType.NC_RNA_INT, FeatureType.NC_RNA_STRING ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    MINUS_THIRTYFIVE( FeatureType.MINUS_THIRTYFIVE_INT, FeatureType.MINUS_THIRTYFIVE_STRING ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    MINUS_TEN( FeatureType.MINUS_TEN_INT, FeatureType.MINUS_TEN_STRING ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    RBS( FeatureType.RBS_INT, FeatureType.RBS_STRING ),
    /**
     * feature type for multiple mapped reads (non-unique)
     */
    MULTIPLE_MAPPED_READ( FeatureType.MULTIPLE_MAPPED_READ_INT, FeatureType.MULTIPLE_MAPPED_READ_STRING ),
    //feature types for the histogram viewer
    BASE_A( FeatureType.BASE_A_INT, FeatureType.BASE_A_STRING ),
    BASE_C( FeatureType.BASE_C_INT, FeatureType.BASE_C_STRING ),
    BASE_G( FeatureType.BASE_G_INT, FeatureType.BASE_G_STRING ),
    BASE_T( FeatureType.BASE_T_INT, FeatureType.BASE_T_STRING ),
    BASE_N( FeatureType.BASE_N_INT, FeatureType.BASE_N_STRING ),
    MATCH( FeatureType.MATCH_INT, FeatureType.MATCH_STRING ),
    GAP( FeatureType.GAP_INT, FeatureType.GAP_STRING ),
    //feature types for the alignment viewer
    DIFF( FeatureType.DIFF_INT, FeatureType.DIFF_STRING ),
    //feature types for the read pair viewer
    PERFECT_PAIR( FeatureType.PERFECT_PAIR_INT, FeatureType.PERFECT_PAIR_STRING ),
    DISTORTED_PAIR( FeatureType.DISTORTED_PAIR_INT, FeatureType.DISTORTED_PAIR_STRING ),
    SINGLE_MAPPING( FeatureType.SINGLE_MAPPING_INT, FeatureType.SINGLE_MAPPING_STRING );

    //feature types supported by the reference viewer
    private static final int ANY_INT = -1;
    private static final int UNDEFINED_INT = 0;
    private static final int MRNA_INT = 1;
    private static final int CDS_INT = 2;
    private static final int MISC_RNA_INT = 3;
    private static final int RRNA_INT = 4;
    private static final int REPEAT_UNIT_INT = 5;
    private static final int SOURCE_INT = 6;
    private static final int TRNA_INT = 7;
    private static final int GENE_INT = 8;
    private static final int MIRNA_INT = 9;
    private static final int EXON_INT = 10;
    private static final int FIVE_UTR_INT = 29;
    private static final int THREE_UTR_INT = 30;
    private static final int NC_RNA_INT = 31;
    private static final int MINUS_THIRTYFIVE_INT = 32;
    private static final int MINUS_TEN_INT = 33;
    private static final int RBS_INT = 34;

    /**
     * feature type int for multiple mapped reads (non-unique)
     */
    private static final int MULTIPLE_MAPPED_READ_INT = 14;

    //feature types for the double track viewer
    private static final int COMPLETE_COVERAGE_INT = 15;
    private static final int TRACK1_COVERAGE_INT = 16;
    private static final int TRACK2_COVERAGE_INT = 17;

    //feature types for the histogram viewer
    private static final int BASE_A_INT = 18;
    private static final int BASE_C_INT = 19;
    private static final int BASE_G_INT = 20;
    private static final int BASE_T_INT = 21;
    private static final int BASE_N_INT = 22;
    private static final int MATCH_INT = 23;
    private static final int GAP_INT = 24;

    //feature types for the alignment viewer
    private static final int DIFF_INT = 25;

    //feature types for the read pair viewer
    private static final int PERFECT_PAIR_INT = 26;
    private static final int DISTORTED_PAIR_INT = 27;
    private static final int SINGLE_MAPPING_INT = 28;


    //feature types supported by ReadXplorer
    //Underscore Strings are needed to parse the official feature keys
    private static final String ANY_STRING = "any";
    private static final String UNDEFINED_STRING = "unknown";
    private static final String MRNA_STRING = "mRNA";
    private static final String CDS_STRING = "CDS";
    /**
     * ORF string is only used for input. It cannot be returned.
     */
    private static final String ORF_STRING = "ORF";
    private static final String MISC_RNA_STRING = "misc RNA";
    /**
     * misc RNA string with underscore "misc_RNA".
     */
    private static final String MISC_RNA_STRING_USCORE = "misc_RNA";
    private static final String RRNA_STRING = "rRNA";
    private static final String REPEAT_UNIT_STRING = "Repeat unit";
    /**
     * Repeat unit string with underscore "repeat_unit".
     */
    private static final String REPEAT_UNIT_STRING_USCORE = "Repeat_unit";
    private static final String SOURCE_STRING = "Source";
    private static final String TRNA_STRING = "tRNA";
    private static final String GENE_STRING = "Gene";
    private static final String MIRNA_STRING = "miRNA";
    private static final String EXON_STRING = "Exon";
    private static final String FIVE_UTR_STRING = "5'UTR";
    private static final String THREE_UTR_STRING = "3'UTR";
    private static final String NC_RNA_STRING = "non-coding RNA";
    private static final String MINUS_THIRTYFIVE_STRING = "-35 signal";
    /**
     * -35 signal string with underscore "-35_signal".
     */
    private static final String MINUS_THIRTYFIVE_STRING_UNDERSCORE = "-35_signal";
    private static final String MINUS_TEN_STRING = "-10 signal";
    /**
     * -10 signal string with underscore "-10_signal".
     */
    private static final String MINUS_TEN_STRING_UNDERSCORE = "-10_signal";
    private static final String RBS_STRING = "RBS";

    /**
     * feature type String for multiple mapped reads (non-unique)
     */
    private static final String MULTIPLE_MAPPED_READ_STRING = "Include multiple mapped reads";

    //feature types for the double track viewer
    private static final String COMPLETE_COVERAGE_STRING = "Difference between both";
    private static final String TRACK1_COVERAGE_STRING = "Track 1 coverage";
    private static final String TRACK2_COVERAGE_STRING = "Track 2 coverage";

    //feature types for the histogram viewer
    private static final String BASE_A_STRING = "A";
    private static final String BASE_C_STRING = "C";
    private static final String BASE_G_STRING = "G";
    private static final String BASE_T_STRING = "T";
    private static final String BASE_N_STRING = "N";
    private static final String MATCH_STRING = "Match";
    private static final String GAP_STRING = "Gap in read";

    //feature types for the alignment viewer
    private static final String DIFF_STRING = "Diff.";

    //feature types for the read pair viewer
    private static final String PERFECT_PAIR_STRING = "Perfect read pair";
    private static final String DISTORTED_PAIR_STRING = "Distorted read pair";
    private static final String SINGLE_MAPPING_STRING = "Single mapping";

    private final int typeInt;
    private final String typeString;

    /**
     * FeatureTypes that are GUI selectable.
     */
    public static final FeatureType[] SELECTABLE_FEATURE_TYPES = { GENE, CDS, EXON, UNDEFINED, MRNA, MISC_RNA, RRNA, REPEAT_UNIT, SOURCE, TRNA, MIRNA, NC_RNA };


    private FeatureType( int typeInt, String typeString ) {
        this.typeInt = typeInt;
        this.typeString = typeString;
    }


    /**
     * @return the string representation of the current feature type.
     */
    @Override
    public String getTypeString() {
        return this.typeString;
    }


    /**
     * @return the integer value of the type of the current feature.
     */
    @Override
    public int getTypeByte() {
        return this.typeInt;
    }


    /**
     * @return the desired FeatureType for a given integer of a genomic feature
     *         type.
     * <p>
     * @param type the type of FeatureType to return. If the type does not match
     *             a genomic feature type, FeatureType.UNDEFINED is returned.
     */
    public static FeatureType getFeatureType( int type ) {
        switch( type ) {
            case ANY_INT:
                return ANY;
            case MRNA_INT:
                return MRNA;
            case CDS_INT:
                return CDS;
            case MISC_RNA_INT:
                return MISC_RNA;
            case RRNA_INT:
                return RRNA;
            case REPEAT_UNIT_INT:
                return REPEAT_UNIT;
            case SOURCE_INT:
                return SOURCE;
            case TRNA_INT:
                return TRNA;
            case GENE_INT:
                return GENE;
            case MIRNA_INT:
                return MIRNA;
            case EXON_INT:
                return EXON;
            case FIVE_UTR_INT:
                return FIVE_UTR;
            case THREE_UTR_INT:
                return THREE_UTR;
            case NC_RNA_INT:
                return NC_RNA;
            case MINUS_THIRTYFIVE_INT:
                return MINUS_THIRTYFIVE;
            case MINUS_TEN_INT:
                return MINUS_TEN;
            case RBS_INT:
                return RBS;
            default:
                return UNDEFINED;
        }
    }


    /**
     * @return the desired FeatureType for a given genomic feature type string.
     * <p>
     * @param type the type of FeatureType to return. If the type is unknown
     *             FeatureType.UNDEFINED is returned.
     */
    public static FeatureType getFeatureType( String type ) {
        FeatureType featType;
        if( type.equalsIgnoreCase( UNDEFINED_STRING ) ) {
            featType = UNDEFINED;
        }
        else if( type.equalsIgnoreCase( MRNA_STRING ) ) {
            featType = MRNA;
        }
        else if( type.equalsIgnoreCase( CDS_STRING ) ) {
            featType = CDS;
        }
        else if( type.equalsIgnoreCase( ORF_STRING ) ) {
            featType = CDS;
        }
        else if( type.equalsIgnoreCase( MISC_RNA_STRING ) || type.equalsIgnoreCase( MISC_RNA_STRING_USCORE ) ) {
            featType = MISC_RNA;
        }
        else if( type.equalsIgnoreCase( RRNA_STRING ) ) {
            featType = RRNA;
        }
        else if( type.equalsIgnoreCase( REPEAT_UNIT_STRING ) || type.equalsIgnoreCase( REPEAT_UNIT_STRING_USCORE ) ) {
            featType = REPEAT_UNIT;
        }
        else if( type.equalsIgnoreCase( TRNA_STRING ) ) {
            featType = TRNA;
        }
        else if( type.equalsIgnoreCase( GENE_STRING ) ) {
            featType = GENE;
        }
        else if( type.equalsIgnoreCase( MIRNA_STRING ) ) {
            featType = MIRNA;
        }
        else if( type.equalsIgnoreCase( EXON_STRING ) ) {
            featType = EXON;
        }
        else if( type.equalsIgnoreCase( SOURCE_STRING ) ) {
            featType = SOURCE;
        }
        else if( type.equalsIgnoreCase( ANY_STRING ) ) {
            featType = ANY;
        }
        else if( type.equalsIgnoreCase( FIVE_UTR_STRING ) ) {
            featType = FIVE_UTR;
        }
        else if( type.equalsIgnoreCase( THREE_UTR_STRING ) ) {
            featType = THREE_UTR;
        }
        else if( type.equalsIgnoreCase( NC_RNA_STRING ) ) {
            featType = NC_RNA;
        }
        else if( type.equalsIgnoreCase( MINUS_THIRTYFIVE_STRING ) || type.equalsIgnoreCase( MINUS_THIRTYFIVE_STRING_UNDERSCORE ) ) {
            featType = MINUS_THIRTYFIVE;
        }
        else if( type.equalsIgnoreCase( MINUS_TEN_STRING ) || type.equalsIgnoreCase( MINUS_TEN_STRING_UNDERSCORE ) ) {
            featType = MINUS_TEN;
        }
        else if( type.equalsIgnoreCase( RBS_STRING ) ) {
            featType = RBS;
        }
        else {
            featType = UNDEFINED;
        }

        return featType;
    }


    @Override
    public String toString() {
        return this.getTypeString();
    }


}
