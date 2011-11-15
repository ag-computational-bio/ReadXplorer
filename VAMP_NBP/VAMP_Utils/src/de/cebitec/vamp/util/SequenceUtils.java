/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolf Hilker
 *
 * Contains all global accessible util methods.
 */
public final class SequenceUtils {

    private SequenceUtils(){
        //do not instantiate
    }

    /**
     * Reverses a string.
     * @param string the string to reverse
     * @return the reversed string
     */
    public static String reverseString(final String string){
        StringBuilder revString = new StringBuilder();
        for (int i=string.length()-1; i>=0; --i){
            revString = revString.append(string.charAt(i));
        }
        return revString.toString();
    }

    /**
     * Complements a sequence String. Requires only lower case characters!
     * @param sequence the string to complement
     * @return the complemented string
     */
    public static String complementDNA(final String sequence){
        String complement = "";
        char currChar;
        for (int i = 0; i < sequence.length(); i++) {
            currChar = sequence.charAt(i);

            switch (currChar){
                case 'c': complement = complement.concat("g"); break;
                case 'g': complement = complement.concat("c"); break;
                case 't': complement = complement.concat("a"); break;
                case 'a': complement = complement.concat("t"); break;
                default : complement = complement.concat(String.valueOf(currChar));
            }
        }
        return complement;
    }

    /**
     * Complements a single DNA base. Needs upper case values.
     * @param base base to complement
     * @return the complemented base or a whitespace, if it encounters a value other than A,C,G,T,N,_.
     */
    public static Character complementDNA(final char base){

        switch (base){
            case 'A': return 'T';
            case 'C': return 'G';
            case 'T': return 'A';
            case 'G': return 'C';
            case 'N': return base;
            case '_': return base;
            default : return ' ';
        }
    }


    /**
     * Produces the reverse complement of a sequence.
     * @param sequence the sequence to reverse and complement
     * @return the reversed and complemented sequence
     */
    public static String getReverseComplement(String sequence) {
        StringBuilder revCompSeq = new StringBuilder();
        for (int i=sequence.length()-1; i>=0; --i) {
            char base = sequence.charAt(i);
            base = SequenceUtils.getComplement(base, sequence);
            revCompSeq.append(base);
        }
        return revCompSeq.toString();
    }




    /**
     * Produces the complement of a single base. For error handling
     * also the whole sequence has to be passed. Returns only upper
     * case values.
     * A = T
     * G = C
     * N = N
     * _ = _
     * @param base the base to complement
     * @param sequence the sequence the base originates from
     * @return the complemented base
     */
    public static char getComplement(char base, String sequence) {
        base = Character.toUpperCase(base);
        char comp = ' ';
        switch (base){
                case 'C': comp = 'G'; break;
                case 'G': comp = 'C'; break;
                case 'T': comp = 'A'; break;
                case 'A': comp = 'T'; break;
                case 'N': comp = 'N'; break;
                case '_': comp = '_'; break;
                default : Logger.getLogger(SequenceUtils.class.getName()).log(Level.SEVERE, 
                        "Found unknown char {0}!Sequence: {1}", new Object[]{base, sequence});
            }

        return comp;
    }
}
