package de.cebitec.vamp.parsing.common;

/**
 *
 * @author ddoppmeier
 */
public class ParsedReferenceGap {

    private long absPos;
    private char base;
    private int order;

    public ParsedReferenceGap(int absPos, char base, int order) {
        this.absPos = absPos;
        this.base = base;
        this.order = order;
    }

    public long getAbsPos() {
        return absPos;
    }

    public char getBase() {
        return base;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParsedReferenceGap other = (ParsedReferenceGap) obj;
        if (this.absPos != other.absPos) {
            return false;
        }
        if (this.base != other.base) {
            return false;
        }
        if (this.order != other.order) {
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
