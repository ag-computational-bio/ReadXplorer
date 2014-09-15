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
 * Enumeration for the total coverage in ReadXplorer.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public enum TotalCoverage implements Classification {

    /**
     * Total coverage constant.
     */
    TOTAL_COVERAGE(TotalCoverage.TOTAL_COVERAGE_BYTE, TotalCoverage.TOTAL_COVERAGE_STRING);
        
    private static final byte TOTAL_COVERAGE_BYTE = 0;
    private static final String TOTAL_COVERAGE_STRING = "Total Coverage";

    private byte typeByte;
    private String typeString;
    
    private TotalCoverage(byte typeByte, String typeString) {
        this.typeByte = typeByte;
        this.typeString = typeString;
    }
    
    /**
     * @return the string representation of the total coverage.
     */
    @Override
    public String getTypeString() {
        return this.typeString;
    }

    /**
     * @return the byte value of the type of the total coverage.
     */
    @Override
    public int getTypeByte() {
        return this.typeByte;
    }

    /**
     * @return the string representation of the total coverage.
     */
    @Override
    public String toString() {
        return this.getTypeString();
    }
    
}
