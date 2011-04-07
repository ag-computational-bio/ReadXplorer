package de.cebitec.vamp.util;

/**
 * Contains different parsers.
 *
 * @author Rolf Hilker
 */
public class Parser {


    /**
     * TODO: put in parser module
     * Generates a string formatted in fasta format.
     * @param sequence the sequence to be stored
     * @param headerParameters the strings to be contained in the header line of the fasta
     * @return the sequence string formatted in fasta format
     */
    public static String generateFasta(String sequence, String... headerParameters) {

        String header = ">";
        for (int i=0; i<headerParameters.length; ++i){
            header = header.concat(headerParameters[i]).concat(" ");
        }

        final int lineLength = 80;
        final int seqLength = sequence.length();
        String formattedSeq = "";
        int i = 0;
        int end = 0;
        while (i < seqLength) {
            end = i + lineLength;

            if (end > seqLength) {
                end = i + (seqLength - i);
            }
            formattedSeq = formattedSeq.concat(sequence.substring(i, end).concat("\r\n"));
            i += lineLength;
        }
        return header.concat("\r\n").concat(formattedSeq);
    }

}
