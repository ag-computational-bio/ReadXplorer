package de.cebitec.readXplorer.util;

import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;


/**
 * Class for utility methods working on dna codons.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CodonUtilities {
    
    /**
     * Parses custom codons of a line defined by the "wantedIndex" into an array of Strings. 
     * Each codon is one entry in the resulting array.
     * @param wantedIndex index of the line containing the wanted codons
     * @param customCodonString string containing the codons inbetween '(' & ')' and
     * seperated by a ',', e.g. (AGT, AGG)
     * @return array containing the wanted codons
     * TODO: change format...
     */
    public static String[] parseCustomCodons(int wantedIndex, String customCodonString) {
        GeneticCodeFactory genCodeFactory = GeneticCodeFactory.getDefault();
        int index = genCodeFactory.getGeneticCodes().size() + 1;
        while (index++ <= wantedIndex){
            customCodonString = customCodonString.substring(customCodonString.indexOf('\n')+1, customCodonString.length());
        }        
        int startIndex = customCodonString.startsWith("\n") ? 2 : 1;
        String codons = customCodonString.substring(startIndex, customCodonString.indexOf(')'));
        String[] splittedCodons = codons.split(",");
        for (int i = 0; i < splittedCodons.length; ++i) {
            splittedCodons[i] = splittedCodons[i].toUpperCase().trim(); //to assure correct format
        }
        return splittedCodons;
    }
    
}
