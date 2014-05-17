/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.util;

/**
 * Enumeration of read pair types.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public enum ReadPairType {
    
    /** 0 = perfect sequence pair (distance and orientation correct). */
    PERFECT_PAIR(ReadPairType.PERFECT_PAIR_INT, ReadPairType.PERFECT_PAIR_STRING),
    /** 1 = distance too large sequence pair. */
    DIST_LARGE_PAIR(ReadPairType.DIST_LARGE_PAIR_INT, ReadPairType.DIST_LARGE_PAIR_STRING),
    /** 2 = distance too small sequence pair. */
    DIST_SMALL_PAIR(ReadPairType.DIST_SMALL_PAIR_INT, ReadPairType.DIST_SMALL_PAIR_STRING),
    /** 3 = orientation wrong sequence pair (distance is correct). */
    ORIENT_WRONG_PAIR(ReadPairType.ORIENT_WRONG_PAIR_INT, ReadPairType.ORIENT_WRONG_PAIR_STRING),
    /** 4 = distance too large and orientation wrong sequence pair. */
    OR_DIST_LARGE_PAIR(ReadPairType.OR_DIST_LARGE_PAIR_INT, ReadPairType.OR_DIST_LARGE_PAIR_STRING),
    /** 5 = distance too small and orientation wrong sequence pair. */
    OR_DIST_SMALL_PAIR(ReadPairType.OR_DIST_SMALL_PAIR_INT, ReadPairType.OR_DIST_SMALL_PAIR_STRING),
    /** 6 = a single mapping whose partner did not map on the reference. */
    UNPAIRED_PAIR(ReadPairType.UNPAIRED_PAIR_INT, ReadPairType.UNPAIRED_PAIR_STRING),
    /** 7 = unique perfect sequence pair (distance and orientation correct, both reads only mapped once). */
    PERFECT_UNQ_PAIR(ReadPairType.PERFECT_UNQ_PAIR_INT, ReadPairType.PERFECT_UNQ_PAIR_STRING),
    /** 8 = unique distance too large sequence pair. */
    DIST_LARGE_UNQ_PAIR(ReadPairType.DIST_LARGE_UNQ_PAIR_INT, ReadPairType.DIST_LARGE_UNQ_PAIR_STRING),
    /** 9 = unique distance too small sequence pair. */
    DIST_SMALL_UNQ_PAIR(ReadPairType.DIST_SMALL_UNQ_PAIR_INT, ReadPairType.DIST_SMALL_UNQ_PAIR_STRING),
    /** 10 = unique orientation wrong sequence pair (distance is correct). */
    ORIENT_WRONG_UNQ_PAIR(ReadPairType.ORIENT_WRONG_UNQ_PAIR_INT, ReadPairType.ORIENT_WRONG_UNQ_PAIR_STRING),
    /** 11 = unique distance too large and orientation wrong sequence pair. */
    OR_DIST_LARGE_UNQ_PAIR(ReadPairType.OR_DIST_LARGE_UNQ_PAIR_INT, ReadPairType.OR_DIST_LARGE_UNQ_PAIR_STRING),
    /** 12 = unique distance too small and orientation wrong sequence pair. */
    OR_DIST_SMALL_UNQ_PAIR(ReadPairType.OR_DIST_SMALL_UNQ_PAIR_INT, ReadPairType.OR_DIST_SMALL_UNQ_PAIR_STRING);
    
    
    
    private static final byte PERFECT_PAIR_INT = 0;
    private static final byte DIST_LARGE_PAIR_INT = 1;
    private static final byte DIST_SMALL_PAIR_INT = 2;
    private static final byte ORIENT_WRONG_PAIR_INT = 3;
    private static final byte OR_DIST_LARGE_PAIR_INT = 4;
    private static final byte OR_DIST_SMALL_PAIR_INT = 5;
    private static final byte UNPAIRED_PAIR_INT = 6;
    private static final byte PERFECT_UNQ_PAIR_INT = 7;
    private static final byte DIST_LARGE_UNQ_PAIR_INT = 8;
    private static final byte DIST_SMALL_UNQ_PAIR_INT = 9;
    private static final byte ORIENT_WRONG_UNQ_PAIR_INT = 10;
    private static final byte OR_DIST_LARGE_UNQ_PAIR_INT = 11;
    private static final byte OR_DIST_SMALL_UNQ_PAIR_INT = 12;
    
    private static final String PERFECT_PAIR_STRING = "Perfect Pair";
    private static final String DIST_LARGE_PAIR_STRING = "Enlarged Pair";
    private static final String DIST_SMALL_PAIR_STRING = "Smaller Pair";
    private static final String ORIENT_WRONG_PAIR_STRING = "Wrong Orientation Pair";
    private static final String OR_DIST_LARGE_PAIR_STRING = "Larger Wrong Orientation Pair";
    private static final String OR_DIST_SMALL_PAIR_STRING = "Smaller Wrong Orientation Pair";
    private static final String UNPAIRED_PAIR_STRING = "Single Mapping";
    private static final String PERFECT_UNQ_PAIR_STRING = "Unique Perfect Pair";
    private static final String DIST_LARGE_UNQ_PAIR_STRING = "Unique Enlarged Pair";
    private static final String DIST_SMALL_UNQ_PAIR_STRING = "Unique Smaller Pair";
    private static final String ORIENT_WRONG_UNQ_PAIR_STRING = "Unique Wrong Orientation Pair";
    private static final String OR_DIST_LARGE_UNQ_PAIR_STRING = "Unique Larger Wrong Orientation Pair";
    private static final String OR_DIST_SMALL_UNQ_PAIR_STRING = "Unique Smaller Wrong Orientation Pair";
    
    private int typeInt;
    private String typeString;
    
    private ReadPairType(int typeInt, String typeString) {
        this.typeInt = typeInt;
        this.typeString = typeString;
    }

    /**
     * @return the string representation of the current feature type.
     */
    public String getTypeString() {
        return this.typeString;
    }
    
    /**
     * @return the integer value of the type of the current feature.
     */
    public int getTypeInt(){
        return this.typeInt;
    }
    
    /**
     * @return the desired ReadPairType for a given integer between 0 and 12.
     * @param type the type of ReadPairType to return. If the type is larger than 12
     * UNPAIRED_PAIR is returned.
     */
    public static ReadPairType getReadPairType(int type){
        switch (type) { 
            case PERFECT_PAIR_INT:              return PERFECT_PAIR;
            case DIST_LARGE_PAIR_INT:           return DIST_LARGE_PAIR;
            case DIST_SMALL_PAIR_INT:           return DIST_SMALL_PAIR;
            case ORIENT_WRONG_PAIR_INT:         return ORIENT_WRONG_PAIR;
            case OR_DIST_LARGE_PAIR_INT:        return OR_DIST_LARGE_PAIR;
            case OR_DIST_SMALL_PAIR_INT:        return OR_DIST_SMALL_PAIR;
            case UNPAIRED_PAIR_INT:             return UNPAIRED_PAIR;
            case PERFECT_UNQ_PAIR_INT:          return PERFECT_UNQ_PAIR;
            case DIST_LARGE_UNQ_PAIR_INT:       return DIST_LARGE_UNQ_PAIR;
            case DIST_SMALL_UNQ_PAIR_INT:       return DIST_SMALL_UNQ_PAIR;
            case ORIENT_WRONG_UNQ_PAIR_INT:     return ORIENT_WRONG_UNQ_PAIR;
            case OR_DIST_LARGE_UNQ_PAIR_INT:    return OR_DIST_LARGE_UNQ_PAIR;
            case OR_DIST_SMALL_UNQ_PAIR_INT:    return OR_DIST_SMALL_UNQ_PAIR;
            default:                            return UNPAIRED_PAIR;
        }
    }
    
    /**
     * @return the desired ReadPairType for a given type string.
     * @param type the type of ReadPairType to return. If the type is unknown
     * UNPAIRED_PAIR is returned.
     */
    public static ReadPairType getReadPairType(String type) {
        ReadPairType readPairType;
        if (type.equalsIgnoreCase(PERFECT_PAIR_STRING)) {
            readPairType = PERFECT_PAIR;
        } else if (type.equalsIgnoreCase(DIST_LARGE_PAIR_STRING)) {
            readPairType = DIST_LARGE_PAIR;
        } else if (type.equalsIgnoreCase(DIST_SMALL_PAIR_STRING)) {
            readPairType = DIST_SMALL_PAIR;
        } else if (type.equalsIgnoreCase(ORIENT_WRONG_PAIR_STRING)) {
            readPairType = ORIENT_WRONG_PAIR;
        } else if (type.equalsIgnoreCase(OR_DIST_LARGE_PAIR_STRING)) {
            readPairType = OR_DIST_LARGE_PAIR;
        } else if (type.equalsIgnoreCase(OR_DIST_SMALL_PAIR_STRING)) {
            readPairType = OR_DIST_SMALL_PAIR;
        }  else if (type.equalsIgnoreCase(UNPAIRED_PAIR_STRING)) {
            readPairType = UNPAIRED_PAIR;
        } else if (type.equalsIgnoreCase(PERFECT_UNQ_PAIR_STRING)) {
            readPairType = PERFECT_UNQ_PAIR;
        } else if (type.equalsIgnoreCase(DIST_LARGE_UNQ_PAIR_STRING)) {
            readPairType = DIST_LARGE_UNQ_PAIR;
        } else if (type.equalsIgnoreCase(DIST_SMALL_UNQ_PAIR_STRING)) {
            readPairType = DIST_SMALL_UNQ_PAIR;
        } else if (type.equalsIgnoreCase(ORIENT_WRONG_UNQ_PAIR_STRING)) {
            readPairType = ORIENT_WRONG_UNQ_PAIR;
        } else if (type.equalsIgnoreCase(OR_DIST_LARGE_UNQ_PAIR_STRING)) {
            readPairType = OR_DIST_LARGE_UNQ_PAIR;
        } else if (type.equalsIgnoreCase(OR_DIST_SMALL_UNQ_PAIR_STRING)) {
            readPairType = OR_DIST_SMALL_UNQ_PAIR;
        } else {
            readPairType = UNPAIRED_PAIR;
        }
        
        return readPairType;
    }
    
    @Override
    public String toString(){
        return this.getTypeString();
    }
}
