package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.util.SequenceComparison;

/**
 * Contains the two amino acid triplets for the reference and the mapped genome
 * plus the identifier for the gene to which the triplet belongs to.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CodonSnp {
    
    private final String tripletRef;
    private final String tripletSnp;
    private final char aminoRef;
    private final char aminoSnp;
    private final SequenceComparison effect;
    private final PersistantFeature feature;
    
    
    /** 
     * Contains the two DNA triplets for the reference and the mapped genome
     * plus the identifier for the gene to which the triplet belongs.
     * @param tripletRef DNA triplet of the reference genome for a certain position
     * @param tripletSnp DNA triplet of the mapped genome for the same position
     * @param aminoRef amino acid of the reference genome for a certain position
     * @param aminoSnp amino acid of the mapped genome for the same position
     * @param effect the effect type of a snp on the amino acid sequence among SequenceUtils.SUBSTITUTION, 
     *             SequenceUtils.MATCH, SequenceUtils.DELETION, SequenceUtils.INSERTION
     * @param feature identifier of the gene to which the triplet belongs
     */
    public CodonSnp(String tripletRef, String tripletSnp, char aminoRef, char aminoSnp, SequenceComparison effect, PersistantFeature feature){
        this.tripletRef = tripletRef;
        this.tripletSnp = tripletSnp;
        this.feature = feature;    
        this.aminoRef = aminoRef;
        this.aminoSnp = aminoSnp;
        this.effect = effect;
    }

    /**
     * @return The reference genome triplet stored in this object.
     */
    public String getTripletRef() {
        return tripletRef;
    }

    /**
     * @return The mapped genome triplet stored in this object, which should/might differ from
     * the reference triplet.
     */
    public String getTripletSnp() {
        return tripletSnp;
    }

    /**
     * @return The identifier of the gene or feature, this codon belongs to.
     */
    public PersistantFeature getFeature() {
        return feature;
    }

    /**
     * @return The reference genome amino acid stored in this object.
     */
    public char getAminoRef() {
        return aminoRef;
    }

    /**
     * @return The mapped genome amino acid stored in this object, which might differ from
     * the reference amino acid.
     */
    public char getAminoSnp() {
        return aminoSnp;
    }

    /**
     * @return the effect type of a snp on the amino acid sequence among SequenceComparison values.
     */
    public SequenceComparison getEffect() {
        return effect;
    }
    
    
}
