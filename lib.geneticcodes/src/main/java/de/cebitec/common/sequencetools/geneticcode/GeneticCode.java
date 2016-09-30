package de.cebitec.common.sequencetools.geneticcode;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author ljelonek, rhilker
 *
 * A genetic code is a mapping from nucleotides to amino acids.
 */
public class GeneticCode {

    private final Map<String, Character> translationMap;
    private final List<String> startCodons;
    private final String description;
    private final String altDescription;
    private final int id;

    /**
     * A genetic code is a mapping from nucleotides to amino acids.
     *
     * @param id id of the genetic code according to NCBI standard genetic codes
     * @param description description of the code
     * @param altDescription alternative description of the code
     * @param codons list of codons, containing the mapping from dna triplets to amino acids
     */
    GeneticCode(int id, String description, String altDescription, List<Codon> codons) throws MalformedGeneticCodeException {
        this(id, description, altDescription, getTranslationMap(codons), getStartCodons(codons));
    }

    /**
     * A genetic code is a mapping from nucleotides to amino acids.
     *
     * @param id id of the genetic code according to NCBI standard genetic codes
     * @param description description of the code
     * @param codons list of codons, containing the mapping from dna triplets to amino acids
     */
    GeneticCode(int id, String description, List<Codon> codons) throws MalformedGeneticCodeException {
        this(id, description, null, getTranslationMap(codons), getStartCodons(codons));
    }

    /**
     * A genetic code is a mapping from nucleotides to amino acids.
     *
     * @param id id of the genetic code according to NCBI standard genetic codes
     * @param description description of the code
     * @param altDescription alternative description of the code (not always used)
     * @param translationMap mapping from dna triplets to amino acids
     * @param startCodons list of start codons
     */
    GeneticCode(int id, String description, String altDescription, Map<String, Character> translationMap,
            List<String> startCodons) throws MalformedGeneticCodeException {

        if (id < 1 || description == null || description.isEmpty() || translationMap.isEmpty() || startCodons.isEmpty()) {
            throw new MalformedGeneticCodeException(
                    ResourceBundle.getBundle("de/cebitec/common/sequencetools/Bundle").getString("GeneticCode.Error"));
        }

        this.id = id;
        this.description = description;
        this.altDescription = altDescription;
        this.translationMap = Collections.unmodifiableMap(translationMap);
        this.startCodons = Collections.unmodifiableList(startCodons);
    }

    /**
     * @return The NCBI genetic code id.
     */
    public int getId() {
        return id;
    }

    /**
     * @return A human readable form of the NCBI genetic code id.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return
     * <code>true</code> if an alternative description exists,
     * <code>false</code> otherwise.
     */
    public boolean hasAlternativeDescription() {
        return altDescription != null;
    }

    /**
     * @return A secondary description of the genetic code.
     */
    public String getAlternativeDescription() {
        return altDescription;
    }

    /**
     * @return An unmodifiable list of start codons for the genetic code.
     */
    public List<String> getStartCodons() {
        return startCodons;
    }

    /**
     * @return An unmodifiable list of stop codons for the genetic code.
     */
    public List<String> getStopCodons() {
        List<String> out = new LinkedList<String>();
        for (Map.Entry<String, Character> en : translationMap.entrySet()) {
            String codon = en.getKey();
            Character aa = en.getValue();
            if (aa.charValue() == '*') {
                out.add(codon);
            }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * Returns the amino acid encoded by the given codon.
     *
     * @param codon
     * @return The amino acid or null if the codon is invalid or nonexistent.
     * @throws InvalidCodonException ex
     */
    public Character getTranslation(String codon) {
        try {
            codon = normalizeCodon(codon);
        } catch (InvalidCodonException ex) {
            return null;
        }

        return getTranslation2(codon);
    }

    /**
     * Same as getTranslation but expects a normalized codon.
     *
     * @param codon
     * @return
     */
    private Character getTranslation2(String codon) {
        if (translationMap.containsKey(codon)) {
            return translationMap.get(codon);
        } else {
            return 'x';
        }
    }

    /**
     * Returns the amino acid sequence encoded by the given dna string. This method also
     * handles sequences that have a length not dividable by 3 by simply ommiting overhanging bases.
     *
     * @param nucleicAcidToTranslate dna or rna string to translate in amino acids
     * @return The amino acid sequence represented by the nuclecic acid. If an unknown amino acid is
     * found it is written as an 'x' in the sequence, according to the IUPAC amino acid code.
     */
    public String getTranslationForString(String nucleicAcidToTranslate) {
        nucleicAcidToTranslate = normalizeSequence(nucleicAcidToTranslate);
        StringBuilder sb = new StringBuilder(nucleicAcidToTranslate.length() / 3);

        for (int i = 0; i + 3 <= nucleicAcidToTranslate.length(); i += 3) {
            String codon = nucleicAcidToTranslate.substring(i, i + 3);
            sb.append(getTranslation2(codon));
        }

        return sb.toString();
    }

    /**
     * Returns the codons that encode the given amino acid.
     *
     * @param aminoAcid amino acid in one character code
     * @return An empty list if aminoAcid is null or not existent, a list with codons otherwise.
     */
    public List<String> getInverseTranslation(Character aminoAcid) {
        if (aminoAcid == null) {
            return Collections.EMPTY_LIST;
        }

        List<String> out = new LinkedList<String>();

        for (Map.Entry<String, Character> en : translationMap.entrySet()) {
            String codon = en.getKey();
            Character aa = en.getValue();

            if (aminoAcid.equals(aa)) {
                out.add(codon);
            }
        }

        return out;
    }

    /**
     * @return Returns an unmodifiable map of codon to amino-acid mappings.
     */
    public Map<String, Character> getTranslationMap() {
        return translationMap;
    }


    /**
     * Normalizes a codon string to uppercase and the ATGC alphabet, i.e. replaces Uracil with
     * Thymine.
     *
     * @param codon
     * @return
     * @throws InvalidCodonException
     */
    static String normalizeCodon(String codon) throws InvalidCodonException {
        if (codon == null || codon.length() != 3) {
            throw new InvalidCodonException();
        } else {
            return normalizeSequence(codon);
        }
    }

    static String normalizeSequence(String sequence) {
        if (sequence != null) {
            return sequence.toUpperCase().replace('U', 'T');
        } else {
            return null;
        }
    }

    /**
     * Generates a translation map from the codons handed in.
     *
     * @param codons the codons to generate a translation map for
     * @return the translation map for the given codons
     */
    private static Map<String, Character> getTranslationMap(List<Codon> codons) {
        Map<String, Character> out = new HashMap<String, Character>();
        for (Codon codon : codons) {
            out.put(normalizeSequence(codon.getCodon()), codon.getAminoAcid());
        }
        return out;
    }

    /**
     * @param codons codons whose start codons should be returned as strings
     * @return list of start codons from the list as strings
     */
    private static List<String> getStartCodons(List<Codon> codons) {
        List<String> out = new LinkedList<String>();
        for (Codon codon : codons) {
            if (codon.isStartCodon()) {
                out.add(codon.getCodon());
            }
        }
        return out;
    }
}
