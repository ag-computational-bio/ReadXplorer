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

package de.cebitec.readxplorer.api.enums;


import de.cebitec.readxplorer.api.Classification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Enumeration for all different features types used in readxplorer. This does
 * not only include genetic features, but also features displayed in viewers.
 * Each type is created with an integer and a string representation and it can
 * return values for both. It also allows to return the feature type represented
 * by a given integer or string.
 * <p>
 * @author ddoppmeier, rhilker, Oliver Schwengers
 */
public enum FeatureType implements Classification {

    /**
     * getTypeInt() returns '-1' = To be used if feature type does not matter.
     * getType() returns '-1' = To be used if feature type does not matter.
     */
    ANY( -1, "any" ),
    /**
     * getType() returns '0' = To be used for a feature with unknown type.
     */
    UNDEFINED( 0, "unknown" ),
    /**
     * getType() returns '1' = To be used for mRNAs.
     */
    MRNA( 1, "mRNA" ),
    /**
     * getType() returns '2' = To be used for coding sequences.
     */
    CDS( 2, "CDS" ),
    /**
     * getType() returns '3' = To be used for misc rnas.
     */
    MISC_RNA( 3, "misc RNA" ),
    /**
     * getType() returns '4' = To be used for rRNAs.
     */
    RRNA( 4, "rRNA" ),
    /**
     * getType() returns '5' = To be used for repeat units.
     */
    REPEAT_UNIT( 5, "Repeat unit" ),
    /**
     * getType() returns '6' = To be used for sources.
     */
    SOURCE( 6, "Source" ),
    /**
     * getType() returns '7' = To be used for tRNAs.
     */
    TRNA( 7, "tRNA" ),
    /**
     * getType() returns '8' = To be used for genes.
     */
    GENE( 8, "Gene" ),
    /**
     * getType() returns '9' = To be used for micro RNAs.
     */
    MIRNA( 9, "miRNA" ),
    /**
     * getType() returns '10' = To be used for exons.
     */
    EXON( 10, "Exon" ),
    /**
     * getType() returns '11' = To be used for introns.
     */
    INTRON( 11, "Intron" ),
    /**
     * getType() returns '29' = To be used for five prime untranslated region.
     */
    FIVE_UTR( 29, "5'UTR" ),
    /**
     * getType() returns '30' = To be used for three prime untranslated region.
     */
    THREE_UTR( 30, "3'UTR" ),
    /**
     * getType() returns '31' = To be used for non coding RNAs.
     */
    NC_RNA( 31, "non-coding RNA" ),
    /**
     * getType() returns '32' = To be used for minus 35 region.
     */
    MINUS_THIRTYFIVE( 32, "-35 signal" ),
    /**
     * getType() returns '33' = To be used for minus 10 region.
     */
    MINUS_TEN( 33, "-10 signal" ),
    /**
     * getType() returns '34' = To be used for ribosome binding sides.
     */
    RBS( 34, "RBS" ),
    /**
     * getType() returns '35' = To be used for start codon location.
     */
    START_CODON( 35, "Start codon" ),
    /**
     * getType() returns '36' = To be used for stop codon location.
     */
    STOP_CODON( 36, "Stop codon" ),
    /**
     * getType() returns '37' = To be used for transcripts.
     */
    TRANSCRIPT( 37, "Transcript" ),
    /**
     * feature type for multiple mapped reads (non-unique)
     */
    MULTIPLE_MAPPED_READ( 14, "Include multiple mapped reads" ),
    // feature types for the histogram viewer
    BASE_A( 18, "A" ),
    BASE_C( 19, "C" ),
    BASE_G( 20, "G" ),
    BASE_T( 21, "T" ),
    BASE_N( 22, "N" ),
    MATCH( 23, "Match" ),
    GAP( 24, "Gap in read" ),
    // feature types for the alignment viewer
    DIFF( 25, "Diff." ),
    // feature types for the read pair viewer
    PERFECT_PAIR( 26, "Perfect read pair" ),
    DISTORTED_PAIR( 27, "Distorted read pair" ),
    SINGLE_MAPPING( 28, "Single mapping" );


    // Underscore Strings are needed to parse the official feature keys
    /**
     * ORF string is only used for input. It cannot be returned.
     */
    private static final String ORF_STRING = "ORF";
    /**
     * misc RNA string with underscore "misc_RNA".
     */
    private static final String MISC_RNA_STRING_UNDERSCORE = "misc_RNA";
    /**
     * Repeat unit string with underscore "repeat_unit".
     */
    private static final String REPEAT_UNIT_STRING_UNDERSCORE = "Repeat_unit";
    /**
     * UTR strings with underscore.
     */
    private static final String FIVE_UTR_STRING_UNDERSCORE = "five_prime_UTR";
    private static final String THREE_UTR_STRING_UNDERSCORE = "three_prime_UTR";
    /**
     * -35 signal string with underscore "-35_signal".
     */
    private static final String MINUS_THIRTYFIVE_STRING_UNDERSCORE = "-35_signal";
    /**
     * -10 signal string with underscore "-10_signal".
     */
    private static final String MINUS_TEN_STRING_UNDERSCORE = "-10_signal";

    /**
     * Start codon string with underscore "start_codon".
     */
    private static final String START_CODON_STRING_UNDERSCORE = "start_codon";
    /**
     * Stop codon string with underscore "stop_codon".
     */
    private static final String STOP_CODON_STRING_UNDERSCORE = "stop_codon";

    private final int type;
    private final String string;

    /**
     * FeatureTypes that are GUI selectable.
     */
    public static final FeatureType[] SELECTABLE_FEATURE_TYPES = { GENE, CDS, EXON, UNDEFINED, MRNA, MISC_RNA, RRNA, REPEAT_UNIT, SOURCE, TRNA, MIRNA, NC_RNA };


    private FeatureType( int type, String string ) {
        this.type = type;
        this.string = string;
    }


    /**
     * @return the integer value of the type of the current feature.
     */
    @Override
    public int getType() {
        return type;
    }


    @Override
    public String toString() {
        return string;
    }


    /**
     * @return the desired FeatureType for a given integer of a genomic feature
     *         type.
     * <p>
     * @param type the type of FeatureType to return. If the type does not match
     *             a genomic feature type, FeatureType.UNDEFINED is returned.
     */
    public static FeatureType getFeatureType( int type ) {

        for( FeatureType featType : values() ) {
            if( featType.getType() == type ) {
                return featType;
            }
        }

        return UNDEFINED;

    }


    /**
     * @return the desired FeatureType for a given genomic feature type string.
     * <p>
     * @param type the type of FeatureType to return. If the type is unknown
     *             FeatureType.UNDEFINED is returned.
     */
    public static FeatureType getFeatureType( String type ) {

        for( FeatureType featType : values() ) {
            if( featType.toString().equalsIgnoreCase( type ) ) {
                return featType;
            }
        }

        //treat possible underscore cases
        if( type.equalsIgnoreCase( ORF_STRING ) ) {
            return CDS;
        } else if( type.equalsIgnoreCase( MISC_RNA_STRING_UNDERSCORE ) ) {
            return MISC_RNA;
        } else if( type.equalsIgnoreCase( REPEAT_UNIT_STRING_UNDERSCORE ) ) {
            return REPEAT_UNIT;
        } else if( type.equalsIgnoreCase( FIVE_UTR_STRING_UNDERSCORE ) ) {
            return FIVE_UTR;
        } else if( type.equalsIgnoreCase( THREE_UTR_STRING_UNDERSCORE ) ) {
            return THREE_UTR;
        } else if( type.equalsIgnoreCase( MINUS_THIRTYFIVE_STRING_UNDERSCORE ) ) {
            return MINUS_THIRTYFIVE;
        } else if( type.equalsIgnoreCase( MINUS_TEN_STRING_UNDERSCORE ) ) {
            return MINUS_TEN;
        } else if( type.equalsIgnoreCase( START_CODON_STRING_UNDERSCORE ) ) {
            return START_CODON;
        } else if( type.equalsIgnoreCase( STOP_CODON_STRING_UNDERSCORE ) ) {
            return STOP_CODON;
        }

        return UNDEFINED;

    }


    /**
     * For retrieving a list of selected feature types from a previously stored
     * string.
     *
     * @param featuresString The comma separated string representation of valid
     *                       feature types (e.g. "Gene,tRNA,miRNA")
     *
     * @return The list of selected indices among all GUI selectable feature
     *         types (see {@link #SELECTABLE_FEATURE_TYPES})
     */
    public static int[] calcSelectedIndices( String featuresString ) {
        String[] featuresArray = featuresString.split( "," );

        List<FeatureType> selectedFeatTypes = new ArrayList<>();
        for( String featureString : featuresArray ) {
            selectedFeatTypes.add( FeatureType.getFeatureType( featureString ) );
        }

        List<FeatureType> featTypeList = Arrays.asList( FeatureType.SELECTABLE_FEATURE_TYPES );
        List<Integer> selectedInices = new ArrayList<>();
        for( FeatureType selFeatureType : selectedFeatTypes ) {
            selectedInices.add( featTypeList.indexOf( selFeatureType ) );
        }

        int[] selIndicesArray = new int[selectedInices.size()];
        for( int i = 0; i < selectedInices.size(); ++i ) {
            selIndicesArray[i] = selectedInices.get( i );
        }

        return selIndicesArray;
    }


    /**
     * Creates a comma separated string of the feature types in the list (e.g.
     * "Gene,tRNA,miRNA").
     *
     * @param featureTypes The list of feature types needed as comma separated
     *                     string
     *
     * @return The comma separated string of the given feature types
     */
    public static String createFeatureTypeString( List<FeatureType> featureTypes ) {
        StringBuilder featTypeString = new StringBuilder( 30 );
        for( FeatureType type : featureTypes ) {
            featTypeString.append( type ).append( ',' );
        }
        return featTypeString.toString();
    }


}
