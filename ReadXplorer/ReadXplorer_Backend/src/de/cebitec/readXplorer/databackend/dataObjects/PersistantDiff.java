package de.cebitec.readXplorer.databackend.dataObjects;

import java.io.Serializable;

/**
 * Depicts a difference in a mapping to the reference genome. Therefore, it contains
 * base, position, number of replicates and strand information.
 * 
 * @author ddoppmei
 */
public class PersistantDiff implements Comparable<PersistantDiff>, Serializable {
    
    private static final long serialVersionUID = 1L;

    private char base;
    private int position;
    private boolean isForwardStrand;
    private int count;

    /**
     * Depicts a difference in a mapping to the reference genome. Therefore, it
     * contains base, position, number of replicates and strand information.
     * @param position
     * @param base the base already converted to the correct strand and upper case
     * @param isForwardStrand
     * @param count  
     */
    public PersistantDiff(int position, char base, boolean isForwardStrand, int count) {
        this.position = position;
        this.base = base;
        this.isForwardStrand = isForwardStrand;
        this.count = count;
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
