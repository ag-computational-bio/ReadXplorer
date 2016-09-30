package de.cebitec.common.sequencetools.geneticcode;

/**
 * @author rhilker
 *
 * Knows which amino acid (1 character code) has which main property and
 * returns it when getPropertyForAA is called.
 * The properties of the amino acids are set according to the classifications
 * in "Molekulare Genetik" by Rolf Knippers (first few pages) in combination with information from
 * http://en.wikipedia.org/wiki/Amino_acid .
 */
public class AminoAcidProperties {

    public static String BASIC = "basic";
    public static String IMIDAZOLE = "imidazole";
    public static String AROMATIC = "aromatic";
    public static String ACIDIC = "acidic";
    public static String AMIDE = "amide group";
    public static String HYDROXY = "hydroxy";
    public static String IMINO = "imino group";
    public static String THIOL = "thiol group";
    public static String SMALL = "small";
    public static String ALIPHATIC = "aliphatic";
    public static String SELENO = "seleno group";
    public static String PYRRO = "pyrro group";
    public static String STOP = "stop";


    /**
     * @param aminoAcid the amino acid whose main property is to be returned
     * @return Knows which amino acid (1 character code) has which main property and
     * returns it. The properties of the amino acids are set according to the classifications
     * in "Molekulare Genetik" by Rolf Knippers (first few pages) in combination with information from
     * http://en.wikipedia.org/wiki/Amino_acid .
     */
    public static String getPropertyForAA(char aminoAcid) {

        switch (aminoAcid) {
            case 'R':
                return BASIC;
            case 'K':
                return BASIC;
            case 'H':
                return IMIDAZOLE;
            case 'D':
                return ACIDIC;
            case 'E':
                return ACIDIC;
            case 'F':
                return AROMATIC;
            case 'W':
                return AROMATIC;
            case 'Y':
                return AROMATIC;
            case 'N':
                return AMIDE;
            case 'Q':
                return AMIDE;
            case 'S':
                return HYDROXY;
            case 'T':
                return HYDROXY;
            case 'P':
                return IMINO;
            case 'C':
                return THIOL;
            case 'G':
                return SMALL;
            case 'A':
                return ALIPHATIC;
            case 'I':
                return ALIPHATIC;
            case 'L':
                return ALIPHATIC;
            case 'M':
                return ALIPHATIC;
            case 'V':
                return ALIPHATIC;
            case 'U':
                return SELENO;
            case 'O':
                return PYRRO;
            case '*':
                return STOP;
            default:
                throw new AssertionError(aminoAcid + ": This is not a valid essential amino acid!");
        }
    }
}
