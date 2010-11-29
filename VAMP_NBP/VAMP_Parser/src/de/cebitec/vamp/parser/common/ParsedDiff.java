package de.cebitec.vamp.parser.common;

/**
 *
 * @author ddoppmeier
 */
public class ParsedDiff {

    private long position;
    private char base;

    public ParsedDiff(long position, char c){
        this.position = position;
        this.base = c;
    }

    public char getBase() {
        return base;
    }

    public long getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParsedDiff other = (ParsedDiff) obj;
        if (this.position != other.position) {
            return false;
        }
        if (this.base != other.base) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (this.position ^ (this.position >>> 32));
        hash = 37 * hash + this.base;
        return hash;
    }

        
}
