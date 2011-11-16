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

    /** Indicates that something is located on the forward strand (1). */
    public static final int STRAND_FWD = 1;
    /** Indicates that something is located on the reverse strand (-1). */
    public static final int STRAND_REV = -1;
    
    /** String for tagging positions or anything else as not having a gene with "No gene".*/
    public static final String NO_GENE = "No gene";
    
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
     * Produces the reverse complement of a sequence.
     * @param sequence the sequence to reverse and complement
     * @return the reversed and complemented sequence
     */
    public static String getReverseComplement(String sequence) {
        StringBuilder revCompSeq = new StringBuilder();
        for (int i=sequence.length()-1; i>=0; --i) {
            char base = sequence.charAt(i);
            base = SequenceUtils.getDnaComplement(base, sequence);
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
     * @param sequence the sequence the base originates from or an empty string if the sequence is not accessible
     * @return the complemented base
     */
    public static char getDnaComplement(char base, String sequence) {
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
