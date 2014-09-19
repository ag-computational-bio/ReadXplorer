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
package de.cebitec.readXplorer.util.classification;

/**
 * Enumeration of read mapping classes in ReadXplorer.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public enum MappingClass implements Classification {
    
    /**
     * Single Perfect Match classification = MappingClass for read mappings
     * whose read only has a single perfect mapping (without differences to the
     * reference). Other Common Match mappings are allowed to exist for the
     * read.
     */
    SINGLE_PERFECT_MATCH(MappingClass.SINGLE_PERFECT_MATCH_BYTE, MappingClass.SINGLE_PERFECT_MATCH_STRING),
    /**
     * Perfect Match classification = Classification for all mappings of a read
     * without differences to the reference.
     */
    PERFECT_MATCH(MappingClass.PERFECT_MATCH_BYTE, MappingClass.PERFECT_MATCH_STRING),
    /**
     * Single Best Match classification = MappingClass for read mappings whose
     * read only has a single best mapping (with the least number of differences
     * to the reference). Other Common Match mappings are allowed to exist for
     * the read.
     */
    SINGLE_BEST_MATCH(MappingClass.SINGLE_BEST_MATCH_BYTE, MappingClass.SINGLE_BEST_MATCH_STRING),
    /**
     * Best Match classification = MappingClass for all best mappings (least
     * differences to the reference) of a read.
     */
    BEST_MATCH(MappingClass.BEST_MATCH_BYTE, MappingClass.BEST_MATCH_STRING),
    /**
     * Common Match classification = MappingClass for read mappings having at
     * least one other, better mapping (with less differences to the reference).
     */
    COMMON_MATCH(MappingClass.COMMON_MATCH_BYTE, MappingClass.COMMON_MATCH_STRING);
    
    private static final byte PERFECT_MATCH_BYTE = 1;
    private static final byte BEST_MATCH_BYTE = 2;
    private static final byte COMMON_MATCH_BYTE = 3;
    private static final byte SINGLE_PERFECT_MATCH_BYTE = 4;
    private static final byte SINGLE_BEST_MATCH_BYTE = 5;
    
    private static final String PERFECT_MATCH_STRING = "Perfect Match";
    private static final String BEST_MATCH_STRING = "Best Match";
    private static final String COMMON_MATCH_STRING = "Common Match";
    private static final String SINGLE_PERFECT_MATCH_STRING = "Single Perfect Match";
    private static final String SINGLE_BEST_MATCH_STRING = "Single Best Match";
    
    private byte typeByte;
    private String typeString;
    
    private MappingClass(byte typeByte, String typeString) {
        this.typeByte = typeByte;
        this.typeString = typeString;
    }
    
    /**
     * @return the string representation of the current read mapping 
     * classification.
     */
    @Override
    public String getTypeString() {
        return this.typeString;
    }
    
    /**
     * @return the byte value of the type of the current read mapping 
     * classification.
     */
    @Override
    public int getTypeByte() {
        return this.typeByte;
    }
    
    /**
     * @return the string representation of the current read mapping 
     * classification.
     */
    @Override
    public String toString() {
        return this.getTypeString();
    }
    
    /**
     * @return the desired MappingClass for a given byte of a valid
     * classification type.
     * @param type Type of MappingClass to return. If the type does not match a
     * classification type or is <code>null</code>, MappingClass.COMMON_MATCH is 
     * returned.
     */
    public static MappingClass getFeatureType(Byte type) {
        if (type == null) { return COMMON_MATCH; }
        switch (type) { 
            case SINGLE_PERFECT_MATCH_BYTE:  return SINGLE_PERFECT_MATCH;
            case SINGLE_BEST_MATCH_BYTE:     return SINGLE_BEST_MATCH;
            case PERFECT_MATCH_BYTE:         return PERFECT_MATCH;
            case BEST_MATCH_BYTE:            return BEST_MATCH;
            case COMMON_MATCH_BYTE:          return COMMON_MATCH;
            default:                         return COMMON_MATCH;
        }
    }
}
