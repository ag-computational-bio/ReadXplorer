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

package de.cebitec.readxplorer.utils;


/**
 * Contains all global accessible sequence util methods.
 * <p>
 * @author Rolf Hilker
 */
public final class SequenceUtils {

    /**
     * String for tagging positions or anything else as not having a gene with
     * "No gene".
     */
    public static final String NO_GENE = "No gene";

    /**
     * Maximum phred quality score as byte = 60.
     */
    public static final byte MAX_PHRED = 60;


    private SequenceUtils() {
        //do not instantiate
    }


    /**
     * Reverses a string.
     * <p>
     * @param string the string to reverse
     * <p>
     * @return the reversed string
     */
    public static String reverseString( final String string ) {
        StringBuilder revString = new StringBuilder( string );
        revString.reverse();
        return revString.toString();
    }


    /**
     * Produces the complement of a single base. Bases not present in the DNA or
     * RNA alphabet are not replaced and RNA sequences are translated in DNA
     * sequences.<br>
     * A = T / a = t<br>
     * U = A / u = a<br>
     * G = C / g = c<br>
     * all other characters are returned as they were
     * <p>
     * @param base the base to complement
     * <p>
     * @return the complemented base
     */
    public static char getDnaComplement( char base ) {
        char comp;
        switch( base ) {
            case 'C':
                comp = 'G';
                break;
            case 'G':
                comp = 'C';
                break;
            case 'T':
                comp = 'A';
                break;
            case 'A':
                comp = 'T';
                break;
            case 'U':
                comp = 'A';
                break;
            case 'c':
                comp = 'g';
                break;
            case 'g':
                comp = 'c';
                break;
            case 't':
                comp = 'a';
                break;
            case 'a':
                comp = 't';
                break;
            case 'u':
                comp = 'a';
                break;
            default:
                comp = base;
        }

        return comp;
    }


    /**
     * Complements a sequence String. Bases not present in the DNA or RNA
     * alphabet are not replaced and RNA sequences are translated in DNA
     * sequences. A = T / a = t U = A / u = a G = C / g = c all other characters
     * are returned as they were.
     * <p>
     * @param sequence the string to complement
     * <p>
     * @return the complemented string
     */
    public static String getDnaComplement( final String sequence ) {
        StringBuilder complement = new StringBuilder( sequence.length() );
        char currChar;
        for( int i = 0; i < sequence.length(); i++ ) {
            currChar = sequence.charAt( i );
            complement.append( SequenceUtils.getDnaComplement( currChar ) );
        }
        return complement.toString();
    }


    /**
     * Produces the reverse complement of a dna sequence.
     * <p>
     * @param sequence the dna sequence to reverse and complement
     * <p>
     * @return the reversed and complemented dna sequence
     */
    public static String getReverseComplement( String sequence ) {
        String revCompSeq = SequenceUtils.getDnaComplement( SequenceUtils.reverseString( sequence ) );
        return revCompSeq;
    }


    /**
     * Checks if the input sequence is a valid DNA string (!not RNA!).
     * <p>
     * @param sequence input sequence to check
     * <p>
     * @return <code>true</code> if it is a valid DNA string, <code>false</code>
     * otherwise
     */
    public static boolean isValidDnaString( String sequence ) {
        return sequence.matches( "[acgtnACGTN]+" );
    }


}
