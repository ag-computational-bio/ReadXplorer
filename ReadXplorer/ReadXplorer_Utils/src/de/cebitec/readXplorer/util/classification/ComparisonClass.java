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
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public enum ComparisonClass implements Classification {

    /** Classification for the coverage difference between two tracks. */
    DIFF_COVERAGE(ComparisonClass.DIFF_COVERAGE_BYTE, ComparisonClass.DIFF_COVERAGE_STRING),
    /** Classification for the coverage of track 1 of a track comparison. */
    TRACK1_COVERAGE(ComparisonClass.TRACK1_COVERAGE_BYTE, ComparisonClass.TRACK1_COVERAGE_STRING),
    /** Classification for the coverage of track 2 of a track comparison. */
    TRACK2_COVERAGE(ComparisonClass.TRACK2_COVERAGE_BYTE, ComparisonClass.TRACK2_COVERAGE_STRING);

    private static final byte DIFF_COVERAGE_BYTE = 1;
    private static final byte TRACK1_COVERAGE_BYTE = 2;
    private static final byte TRACK2_COVERAGE_BYTE = 3;
    
    private static final String DIFF_COVERAGE_STRING = "Coverage Difference";
    private static final String TRACK1_COVERAGE_STRING = "Coverage Track 1";
    private static final String TRACK2_COVERAGE_STRING = "Coverage Track 2";
    
    private byte typeByte;
    private String typeString;
    
    private ComparisonClass(byte typeByte, String typeString) {
        this.typeByte = typeByte;
        this.typeString = typeString;
    }
    
    /**
     * @return the string representation of the current comparison 
     * classification.
     */
    @Override
    public String getTypeString() {
        return this.typeString;
    }
    
    /**
     * @return the byte value of the type of the current comparison
     * classification.
     */
    @Override
    public int getTypeByte() {
        return this.typeByte;
    }
    
    /**
     * @return the string representation of the current comparison
     * classification.
     */
    @Override
    public String toString() {
        return this.getTypeString();
    }
    
    /**
     * @return the desired ComparisonClass for a given integer of a valid
     * classification type.
     * @param type Type of ComparisonClass to return. If the type does not
     * match a classification type, null is returned.
     */
    public static ComparisonClass getFeatureType(byte type){
        switch (type) { 
            case DIFF_COVERAGE_BYTE:    return DIFF_COVERAGE;
            case TRACK1_COVERAGE_BYTE:  return TRACK1_COVERAGE;
            case TRACK2_COVERAGE_BYTE:  return TRACK2_COVERAGE;
            default:                    return null;
        }
    }
    
}
