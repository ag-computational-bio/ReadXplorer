package de.cebitec.common.sequencetools.geneticcode;

/**
 * @author ljelonek
 *
 * A codon represents a dna triplet. Thus it holds the codon itself, the amino acid
 * it codes for in one letter code and if codon is a start or a stop codon.
 */
public class Codon {

    private final String codon;
    private final char aminoAcid;
    private final Boolean isStartCodon;

    /**
     * A codon represents a dna triplet. Thus it holds the codon itself, the amino acid
     * it codes for in one letter code and if codon is a start codon.
     * @param codon the dna triplet
     * @param aminoAcid the amino acid it codes for
     * @param isStart <code>true</code> if it is a start codon, <code>false</code> otherwise
     */
    public Codon(String codon, char aminoAcid, Boolean isStart) {
        this.codon = codon;
        this.aminoAcid = aminoAcid;
        this.isStartCodon = isStart;
    }

    public char getAminoAcid() {
        return aminoAcid;
    }

    /**
     * @return the dna triplet (codon)
     */
    public String getCodon() {
        return codon;
    }

    public Boolean isStartCodon() {
        return isStartCodon;
    }

    public Boolean isStopCodon() {
        return aminoAcid == '*';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Codon other = (Codon) obj;
        if ((this.codon == null) ? (other.codon != null) : !this.codon.equals(other.codon)) {
            return false;
        }
        if (this.aminoAcid != other.aminoAcid) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.codon != null ? this.codon.hashCode() : 0);
        hash = 53 * hash + this.aminoAcid;
        return hash;
    }
}
