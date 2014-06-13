/* 
 * Copyright (C) 2014 Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
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
 * Enumeration for sequence comparisons. The different types can represent for 
 * example if something matches, was substituted, inserted, or deleted. 
 *
 * @author rhilker
 */
public enum SequenceComparison {
    
    /** getType() returns 'S' = To be used for substitutions. */
    SUBSTITUTION (SequenceComparison.S), 
    /** getType() returns 'N' = To be used for neutral substitutions. */
    NEUTRAL(SequenceComparison.N), 
    /** getType() returns 'E' = To be used for missense substitutions. */
    MISSENSE(SequenceComparison.E),
    /** getType() returns 'M' = To be used for matches. */
    MATCH (SequenceComparison.M),
    /** getType() returns 'D' = To be used for deletions. */
    DELETION (SequenceComparison.D),
    /** getType() returns 'I' = To be used for insertions. */
    INSERTION (SequenceComparison.I),
    /** getType() returns ' ' = To be used for unknown type. */
    UNKNOWN (' ');

    
    /** 'S' = To be used for substitutions. */
    private static final char S = 'S';
    /** 'N' = To be used for neutral substituions. */
    private static final char N = 'N';
    /** 'E' = To be used for missense substitutions. */
    private static final char E = 'E';
    /** 'M' = To be used for matches. */
    private static final char M = 'M';
    /** 'D' = To be used for deletions. */
    private static final char D = 'D';
    /** 'I' = To be used for insertions. */
    private static final char I = 'I';
    
    private char type;
    
    /**
     * Enumeration for sequence comparisons. The different types can represent
     * for example if something matches, was substituted, inserted, or deleted.
     */
    private SequenceComparison(char type) {
        this.type = type;
    }
    
    /**
     * @return the effect type char of the current effect.
     */
    public char getType(){
        return type;
    }
    
    /**
     * @param type the type of SequenceComparison to return. 
     * @return The SequenceComparison for a given char. If the type is unknown 
     * SequenceComparison.UNKNOWN is returned.
     */
    public static SequenceComparison getSequenceComparison(char type){
        switch (type){
            case SequenceComparison.S :
                return SUBSTITUTION;
            case SequenceComparison.N :
                return NEUTRAL;
            case SequenceComparison.E :
                return MISSENSE;
            case SequenceComparison.M :
                return MATCH;
            case SequenceComparison.I :
                return INSERTION;
            case SequenceComparison.D :
                return DELETION;
            default :
                return UNKNOWN;
        }
    }
    
    /**
     * @return the String value of the type char of the current effect
     */
    @Override
    public String toString(){
        return String.valueOf(type);
    }
    
}
