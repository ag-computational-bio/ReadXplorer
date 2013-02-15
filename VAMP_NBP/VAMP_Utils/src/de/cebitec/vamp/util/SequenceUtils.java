package de.cebitec.vamp.util;

/**
 * Contains all global accessible sequence util methods.
 * 
 * @author Rolf Hilker
 */
public final class SequenceUtils {

    /** Indicates that something is located on the forward strand (1). */
    public static final byte STRAND_FWD = 1;
    /** Indicates that something is located on the reverse strand (-1). */
    public static final byte STRAND_REV = -1;
    /** Indicates that something is located on the forward strand (Fwd). */
    public static final String STRAND_FWD_STRING = "Fwd";
    /** Indicates that something is located on the reverse strand (Rev). */
    public static final String STRAND_REV_STRING = "Rev";
    
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
    public static String reverseString(final String string) {
        StringBuilder revString = new StringBuilder(string);
        revString.reverse();
        return revString.toString();
    }

    /**
     * Complements a sequence String. Bases not present in the DNA or RNA
     * alphabet are not replaced and RNA sequences are translated in DNA
     * sequences. A = T / a = t U = A / u = a G = C / g = c all other characters
     * are returned as they were.
     * @param sequence the string to complement
     * @return the complemented string
     */
    public static String complementDNA(final String sequence){
        StringBuilder complement = new StringBuilder(sequence.length());
        char currChar;
        for (int i = 0; i < sequence.length(); i++) {
            currChar = sequence.charAt(i);

            switch (currChar){
                case 'c': complement.append('g'); break;
                case 'g': complement.append('c'); break;
                case 't': complement.append('a'); break;
                case 'a': complement.append('t'); break;
                case 'u': complement.append('a'); break;
                case 'C': complement.append('G'); break;
                case 'G': complement.append('C'); break;
                case 'T': complement.append('A'); break;
                case 'A': complement.append('T'); break;
                case 'U': complement.append('A'); break; 
                default : complement.append(currChar);
            }
        }
        return complement.toString();
    }


    /**
     * Produces the reverse complement of a dna sequence.
     * @param sequence the dna sequence to reverse and complement
     * @return the reversed and complemented dna sequence
     */
    public static String getReverseComplement(String sequence) {
        String revCompSeq = SequenceUtils.complementDNA(SequenceUtils.reverseString(sequence));
        return revCompSeq;
    }


    /**
     * Produces the complement of a single base. Bases not present in the DNA or
     * RNA alphabet are not replaced and RNA sequences are translated in DNA
     * sequences.
     * A = T / a = t
     * U = A / u = a
     * G = C / g = c
     * all other characters are returned as they were
     * @param base the base to complement
     * @return the complemented base
     */
    public static char getDnaComplement(char base) {
        char comp;
        switch (base) {
                case 'C': comp = 'G'; break;
                case 'G': comp = 'C'; break;
                case 'T': comp = 'A'; break;
                case 'A': comp = 'T'; break;
                case 'U': comp = 'A'; break;
                case 'c': comp = 'g'; break;
                case 'g': comp = 'c'; break;
                case 't': comp = 'a'; break;
                case 'a': comp = 't'; break;
                case 'u': comp = 'a'; break;
                default : comp = base;
            }

        return comp;
    }
    
    /**
     * Checks if the input sequence is a valid DNA string (!not RNA!).
     * @param sequence input sequence to check
     * @return <code>true</code> if it is a valid DNA string, <code>false</code> otherwise
     */
    public static boolean isValidDnaString(String sequence) {
        return sequence.matches("[acgtnACGTN]+");
    }
}
