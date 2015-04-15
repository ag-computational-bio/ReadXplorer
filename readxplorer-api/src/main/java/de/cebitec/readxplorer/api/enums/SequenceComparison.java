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
 * Enumeration for sequence comparisons.
 * The different types can represent for example if something matches,
 * was substituted, inserted, or deleted.
 *
 * @author rhilker
 */
public enum SequenceComparison {

    /**
     * getType() returns 'S' = To be used for substitutions.
     */
    SUBSTITUTION( 'S' ),

    /**
     * getType() returns 'N' = To be used for neutral substitutions.
     */
    NEUTRAL( 'N' ),

    /**
     * getType() returns 'E' = To be used for missense substitutions.
     */
    MISSENSE( 'E' ),

    /**
     * getType() returns 'M' = To be used for matches.
     */
    MATCH( 'M' ),

    /**
     * getType() returns 'D' = To be used for deletions.
     */
    DELETION( 'D' ),

    /**
     * getType() returns 'I' = To be used for insertions.
     */
    INSERTION( 'I' ),

    /**
     * getType() returns ' ' = To be used for unknown type.
     */
    UNKNOWN( ' ' );


    private final char type;


    /**
     * Enumeration for sequence comparisons. The different types can represent
     * for example if something matches, was substituted, inserted, or deleted.
     */
    private SequenceComparison( char type ) {
        this.type = type;
    }


    /**
     * @return the effect type char of the current effect.
     */
    public char getType() {
        return type;
    }


    /**
     * @param type the type of SequenceComparison to return.
     * <p>
     * @return The SequenceComparison for a given char. If the type is unknown
     * SequenceComparison.UNKNOWN is returned.
     */
    public static SequenceComparison getSequenceComparison( char type ) {

        for( SequenceComparison seqComp : values() ) {
            if( seqComp.getType() == type ) {
                return seqComp;
            }
        }

        return UNKNOWN;

    }


    /**
     * @return the String value of the type char of the current effect
     */
    @Override
    public String toString() {
        return String.valueOf( type );
    }


}
