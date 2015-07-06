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


/**
 * Enumeration of read pair types.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public enum ReadPairType {

    /**
     * 0 = perfect read pair (distance and orientation correct).
     */
    PERFECT_PAIR( 0, "Perfect Pair" ),

    /**
     * 1 = distance too large read pair.
     */
    DIST_LARGE_PAIR( 1, "Enlarged Pair" ),

    /**
     * 2 = distance too small read pair.
     */
    DIST_SMALL_PAIR( 2, "Smaller Pair" ),

    /**
     * 3 = orientation wrong read pair (distance is correct).
     */
    ORIENT_WRONG_PAIR( 3, "Wrong Orientation Pair" ),

    /**
     * 4 = distance too large and orientation wrong read pair.
     */
    OR_DIST_LARGE_PAIR( 4, "Larger Wrong Orientation Pair" ),

    /**
     * 5 = distance too small and orientation wrong read pair.
     */
    OR_DIST_SMALL_PAIR( 5, "Smaller Wrong Orientation Pair" ),

    /**
     * 6 = a single mapping whose partner did not map on the reference or where
     * a pair assignment fails due to too many unambiguous mappings of both
     * reads.
     */
    UNPAIRED_PAIR( 6, "Single Mapping" ),

    /**
     * 7 = unique perfect read pair (distance and orientation correct, both
     * reads only mapped once).
     */
    PERFECT_UNQ_PAIR( 7, "Unique Perfect Pair" ),

    /**
     * 8 = unique distance too large read pair.
     */
    DIST_LARGE_UNQ_PAIR( 8, "Unique Enlarged Pair" ),

    /**
     * 9 = unique distance too small read pair.
     */
    DIST_SMALL_UNQ_PAIR( 9, "Unique Smaller Pair" ),

    /**
     * 10 = unique orientation wrong read pair (distance is correct).
     */
    ORIENT_WRONG_UNQ_PAIR( 10, "Unique Wrong Orientation Pair" ),

    /**
     * 11 = unique distance too large and orientation wrong read pair.
     */
    OR_DIST_LARGE_UNQ_PAIR( 11, "Unique Larger Wrong Orientation Pair" ),

    /**
     * 12 = unique distance too small and orientation wrong read pair.
     */
    OR_DIST_SMALL_UNQ_PAIR( 12, "Unique Smaller Wrong Orientation Pair" );
    

    private final int type;
    private final String string;


    private ReadPairType( int type, String string ) {
        this.type = type;
        this.string = string;
    }


    /**
     * @return the string representation of the current feature type.
     */
    public String getString() {
        return this.string;
    }


    /**
     * @return the integer value of the type of the current feature.
     */
    public int getType() {
        return this.type;
    }


    /**
     * @return the desired ReadPairType for a given integer between 0 and 12.
     * <p>
     * @param type the type of ReadPairType to return. If the type is larger
     * than 12 UNPAIRED_PAIR is returned.
     */
    public static ReadPairType getReadPairType( int type ) {

        for( ReadPairType rpType : values() ) {
            if( rpType.getType() == type ) {
                return rpType;
            }
        }

        return UNPAIRED_PAIR;

    }


    /**
     * @return the desired ReadPairType for a given type string.
     * <p>
     * @param type the type of ReadPairType to return. If the type is unknown
     * UNPAIRED_PAIR is returned.
     */
    public static ReadPairType getReadPairType( String type ) {

        for( ReadPairType rpType : values() ) {
            if( rpType.getString().equalsIgnoreCase( type ) ) {
                return rpType;
            }
        }

        return UNPAIRED_PAIR;
    }


    @Override
    public String toString() {
        return this.getString();
    }


}
