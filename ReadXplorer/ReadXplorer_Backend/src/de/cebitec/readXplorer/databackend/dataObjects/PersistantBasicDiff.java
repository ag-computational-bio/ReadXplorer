package de.cebitec.readXplorer.databackend.dataObjects;

/**
 * Depicts a difference in a mapping to the reference genome. Therefore, it contains
 * base, position, number of replicates and strand information.
 *
 * @author ddoppmeier, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
class PersistantBasicDiff {
    
    private static final long serialVersionUID = 1L;

    private final char base;
    private final int position;
    private final boolean isForwardStrand;
    private final int count;
    private final byte baseQuality;
    private final Byte mappingQuality; //is a Byte, because we want to share the single object with all other diffs of the read

    /**
     * Depicts a difference in a mapping to the reference genome. Therefore, it
     * contains base, position, number of replicates, strand information, base
     * quality and mapping quality value.
     *
     * @param position
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
    public PersistantBasicDiff(int position, char base, boolean isForwardStrand, int count, byte baseQuality, Byte mappingQuality) {
        this.position = position;
        this.base = base;
        this.isForwardStrand = isForwardStrand;
        this.count = count;
        this.baseQuality = baseQuality;
        this.mappingQuality = mappingQuality;
    }

    /**
     * @return The associated base of the diff, already converted to the correct
     * strand and upper case.
     */
    public char getBase() {
        return base;
    }

    /**
     * @return the absolute position of the diff in genome coordinates.
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return number of replicate mappings in which this diff occurs.
     */
    public int getCount() {
        return count;
    }

    /**
     * @return true, if this diff is on the forward strand
     */
    public boolean isForwardStrand() {
        return isForwardStrand;
    }

    /**
     * @return The phred base quality value for the diff base in the read.
     */
    public byte getBaseQuality() {
        return baseQuality;
    }

    /**
     * @return The phred mapping quality value of the read from which the diff
     * originated. Instead of 255 for unkown values, we use -1 here!
     */
    public Byte getMappingQuality() {
        return mappingQuality;
    }
}
