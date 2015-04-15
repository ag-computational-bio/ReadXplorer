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



/**
 * Enumeration of read mapping classes in ReadXplorer.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public enum MappingClass implements Classification {

    /**
     * Single Perfect Match classification = MappingClass for read mappings
     * whose read only has a single perfect mapping (without differences to the
     * reference). Other Common Match mappings are allowed to exist for the
     * read.
     */
    SINGLE_PERFECT_MATCH( 4, "Single Perfect Match" ),

    /**
     * Perfect Match classification = Classification for all mappings of a read
     * without differences to the reference.
     */
    PERFECT_MATCH( 1, "Perfect Match" ),

    /**
     * Single Best Match classification = MappingClass for read mappings whose
     * read only has a single best mapping (with the least number of differences
     * to the reference). Other Common Match mappings are allowed to exist for
     * the read.
     */
    SINGLE_BEST_MATCH( 5, "Single Best Match" ),

    /**
     * Best Match classification = MappingClass for all best mappings (least
     * differences to the reference) of a read.
     */
    BEST_MATCH( 2, "Best Match" ),

    /**
     * Common Match classification = MappingClass for read mappings having at
     * least one other, better mapping (with less differences to the reference).
     */
    COMMON_MATCH( 3, "Common Match" );

    private final int type;
    private final String string;


    private MappingClass( int type, String string ) {
        this.type = type;
        this.string = string;
    }


    /**
     * @return the string representation of the current read mapping
     * classification.
     */
    @Override
    public String getString() {
        return this.string;
    }


    /**
     * @return the byte value of the type of the current read mapping
     * classification.
     */
    @Override
    public int getType() {
        return this.type;
    }


    /**
     * @return the string representation of the current read mapping
     * classification.
     */
    @Override
    public String toString() {
        return this.string;
    }


    /**
     * @return the desired MappingClass for a given byte of a valid
     * classification type.
     * <p>
     * @param type Type of MappingClass to return. If the type does not match a
     * classification type or is <code>null</code>, MappingClass.COMMON_MATCH is
     * returned.
     */
    public static MappingClass getFeatureType( int type ) {

        if( type == 0 ) {
            return COMMON_MATCH;
        }

        for( MappingClass mappingClass : values() ) {
            if( mappingClass.getType() == type ) {
                return mappingClass;
            }
        }

        return COMMON_MATCH;

    }


}
