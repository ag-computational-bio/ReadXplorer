package de.cebitec.vamp.databackend.dataObjects;

import java.io.Serializable;

/**
 * Creates a new gap in the reference sequence.
 *
 * @author ddoppmeier
 */
public class PersistantReferenceGap implements Comparable<PersistantReferenceGap>, Serializable {
    
    private static final long serialVersionUID = 1L;

    private int position;
    private Character base;
    private int order;
    boolean isForwardStrand;
    private int count;

    /**
     * Creates a new gap in the reference sequence.
     * @param position the absolute reference position at which the gap is added
     * @param base the base to add
     * @param order the index (beginning with 0) of the reference gap at the 
     * given position
     * @param isForwardStrand true, if it is on the fwd strand, false otherwise
     * @param count the number of occurrences of the gap
     */
    public PersistantReferenceGap(int position, Character base, int order, boolean isForwardStrand, int count){
        this.position = position;
        this.base = base;
        this.order = order;
        this.isForwardStrand = isForwardStrand;
        this.count = count;
    }

    /**
     * @return the absolute reference position at which the gap is added
     */
    public int getPosition(){
        return position;
    }

    /**
     * @return The corresponding base in the reads
     */
    public Character getBase(){
        return base;
    }

    /**
     * @return the index (beginning with 0) of the reference gap at the 
     * given position
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return true, if it is on the fwd strand, false otherwise
     */
    public boolean isForwardStrand() {
        return isForwardStrand;
    }

    /**
     * @return the number of occurrences of the gap
     */
    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(PersistantReferenceGap o) {
        // only use this compare method for comparing gaps of the same mappings

        // order by position
        if (position < o.getPosition()) {
            return -1;
        } else if (position > o.getPosition()) {
            return 1;
        } else {

            // order by position in mapping
            if (order < o.getOrder()) {
                return -1;
            } else if (order > o.getOrder()) {
                return 1;
            } else {
                return 0;
            }
        }

    }

}
