package de.cebitec.vamp.parser.common;

/**
 * @author ddoppmeier
 * 
 * Data structure representing a gap in the reference genome.
 */
public class ParsedReferenceGap {

    private long absPos;
    private char base;
    private int order;

    /**
     * Data structure representing a gap in the reference genome.
     * @param absPos absolute position of the gap in the reference genome
     * @param base base of the gap in the mapping
     * @param order the order at which position in a larger gap it occurs (1, 2, 3...)
     */
    public ParsedReferenceGap(int absPos, char base, int order) {
        this.absPos = absPos;
        this.base = base;
        this.order = order;
    }

    /**
     * @return the absolute position of the gap in the reference genome
     */
    public long getAbsPos() {
        return absPos;
    }

    /**
     * @return the base of the gap in the mapping
     */
    public char getBase() {
        return base;
    }

    /**
     * @return at which position in a larger gap it occurs (1, 2, 3...)
     */
    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ParsedReferenceGap other = (ParsedReferenceGap) obj;
        if (    this.absPos != other.getAbsPos() ||
                this.base   != other.getBase() ||
                this.order  != other.getOrder()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (int) (this.absPos ^ (this.absPos >>> 32));
        hash = 71 * hash + this.base;
        hash = 71 * hash + this.order;
        return hash;
    }

}
