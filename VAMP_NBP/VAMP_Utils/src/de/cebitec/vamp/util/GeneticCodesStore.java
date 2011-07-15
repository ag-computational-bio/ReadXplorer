/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.util;

import java.util.HashMap;

/**
 * Storage for all start and stop codons of the different genetic codes.
 * 
 * @author Rolf Hilker
 */
public final class GeneticCodesStore {

    private static HashMap<String, String[][]> geneticCodes;

    private static final String[] IDENTIFIERS = {
/*1*/       "Standard",
/*2*/       "Vertebrate Mitochondrial",
/*3*/       "Yeast Mitochondrial",
/*4*/       "Mold Mitochondrial; Protozoan Mitochondrial; Coelenterate Mitochondrial; Mycoplasma; Spiroplasma",
/*5*/       "Invertebrate Mitochondrial",
/*6*/       "Ciliate Nuclear; Dasycladacean Nuclear; Hexamita Nuclear",
/*9,21*/    "Echinoderm Mitochondrial; Flatworm Mitochondrial; Trematode Mitochondrial",
/*10,13*/   "Euplotid Nuclear; Ascidian Mitochondrial",
/*11*/      "Bacterial and Plant Plastid",
/*12*/      "Alternative Yeast Nuclear",
/*14*/      "Alternative Flatworm Mitochondrial",
/*15,16*/   "Blepharisma Macronuclear; Chlorophycean Mitochondrial",
/*22*/      "Scenedesmus obliquus mitochondrial",
/*23*/      "Thraustochytrium mitochondrial code"
    };

    private static final String[][] START_CODONS = {
/*1*/       { "ATG", "GTG", "TTG" },
/*2*/       { "ATT", "ATC", "ATA", "ATG", "GTG" },
/*3*/       { "ATA", "ATG" },
/*4*/       { "TTA", "TTG", "CTG", "ATT", "ATC", "ATA", "ATG", "GTG" },
/*5*/       { "TTG", "ATT", "ATC", "ATA", "ATG", "GTG" },
/*6*/       { "ATG" },
/*9,21*/    { "ATG", "GTG" },
/*10,13*/   { "ATG" },
/*11*/      { "TTG", "CTG", "ATT", "ATC", "ATA", "ATG", "GTG" },
/*12*/      { "CTG", "ATG"},
/*14*/      { "ATG" },
/*15, 16*/  { "ATG" },
/*22*/      { "ATG" },
/*23*/      { "ATT", "ATG", "GTG" }
    };

    private static final String[][] STOP_CODONS = {
/*1*/       { "TAA", "TAG", "TGA" },
/*2*/       { "TAA", "TAG", "AGA", "AGG" },
/*3*/       { "TAA", "TAG" },
/*4*/       { "TAA", "TAG" },
/*5*/       { "TAA", "TAG" },
/*6*/       { "TGA" },
/*9,21*/    { "TAA", "TAG" },
/*10,13*/   { "TAA", "TAG" },
/*11*/      { "TAA", "TAG", "TGA" },
/*12*/      { "TAA", "TAG", "TGA" },
/*14*/      { "TAG" },
/*15, 16*/  { "TAA", "TGA" },
/*22*/      { "TCA", "TAA", "TGA" },
/*23*/      { "TTA", "TAA", "TAG", "TGA" }
    };

    /**
     * Returns the desired genetic code defined by the given index.
     * @param name identifier of the genetic code to return
     * @return the desired genetic code. The first array contains the start codons 
     *         and the second array the stop codons.
     */
    public static String[][] getGeneticCode(final String name){

        if (GeneticCodesStore.geneticCodes == null){
            GeneticCodesStore.geneticCodes = new HashMap<String, String[][]>();
            for (int j=0; j<IDENTIFIERS.length; ++j){
                String name2 = GeneticCodesStore.IDENTIFIERS[j];
                String[] startCodons = GeneticCodesStore.START_CODONS[j];
                String[] stopCodons = GeneticCodesStore.STOP_CODONS[j];
                String[][] currentCode = {startCodons, stopCodons};
                GeneticCodesStore.geneticCodes.put(name2, currentCode);
            }
        }

        return GeneticCodesStore.geneticCodes.get(name);
    }

    /**
     * Returns the array of identifiers of the different genetic codes, stored in this class.
     * @return the array of identifiers of the different genetic codes, stored in this class.
     */
    public static String[] getGeneticCodeIdentifiers(){
        return GeneticCodesStore.IDENTIFIERS.clone();
    }

    /**
     * Returns the array of start codons. The order is the same to the IDENTIFIERS
     * array.
     * @return the array of start codons. The order is the same to the IDENTIFIERS
     * array.
     */
    public static String[][] getStartCodons(){
        return GeneticCodesStore.START_CODONS.clone();
    }
}
