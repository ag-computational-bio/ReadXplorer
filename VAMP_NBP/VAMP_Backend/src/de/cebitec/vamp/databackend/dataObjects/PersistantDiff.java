package de.cebitec.vamp.databackend.dataObjects;

/**
 * @author ddoppmei
 * 
 * Depicts a difference in a mapping to the reference genome. Therefore, it contains
 * base, position, number of replicates and strand information.
 */
public class PersistantDiff {

    private char base;
    private int position;
    private boolean isForwardStrand;
    private int count;

    /**
     * Depicts a difference in a mapping to the reference genome. Therefore, it
     * contains base, position, number of replicates and strand information.
     */
    public PersistantDiff(int position, char base, boolean isForwardStrand, int count) {
        this.position = position;
        this.base = base;
        this.isForwardStrand = isForwardStrand;
        this.count = count;
    }

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

    public boolean isIsForwardStrand() {
        return isForwardStrand;
    }
}
