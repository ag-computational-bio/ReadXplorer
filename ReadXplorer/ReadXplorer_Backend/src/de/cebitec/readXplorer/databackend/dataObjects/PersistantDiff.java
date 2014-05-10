package de.cebitec.readXplorer.databackend.dataObjects;

import java.io.Serializable;

/**
 * Depicts a comparable difference in a mapping to the reference genome.
 * Therefore, it contains base, position, number of replicates and strand
 * information.
 *
 * @author ddoppmeier, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class PersistantDiff extends PersistantBasicDiff implements Comparable<PersistantDiff>, Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Depicts a comparable difference in a mapping to the reference genome.
     * Therefore, it contains base, position, number of replicates, strand
     * information, base quality and mapping quality value.
     * @param position position of the diff in the reference
     * @param base the base already converted to the correct strand and upper
     * case
     * @param isForwardStrand <cc>true</cc> if the diff was on the fwd strand,
     * <cc>false</cc> otherwise
     * @param count number of replicates of the diff
     * @param baseQuality The phred base quality value for the diff base in the
     * read.
     * @param mappingQuality The phred mapping quality value of the read from
     * which the diff originated
     */
    public PersistantDiff(int position, char base, boolean isForwardStrand, int count, byte baseQuality, Byte mappingQuality) {
        super(position, base, isForwardStrand, count, baseQuality, mappingQuality);
    }

    /**
     * Compares the other diff to this diff by position. 
     * @param other the diff to compare with this diff
     * @return -1 if this diff has a smaller position than the other, 1 if this 
     * diff has a larger position than the other, and 0 if the position is equal
     */
    @Override
    public int compareTo(PersistantDiff other) {
        if (this.getPosition() < other.getPosition()) {
            return -1;
        } else if (this.getPosition() > other.getPosition()) {
            return 1;
        } else {
            return 0;
        }
    }
}
