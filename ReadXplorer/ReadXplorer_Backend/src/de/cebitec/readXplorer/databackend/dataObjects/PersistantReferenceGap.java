package de.cebitec.readXplorer.databackend.dataObjects;

import java.io.Serializable;

/**
 * Creates a new comparable gap in the reference sequence.
 *
 * @author ddoppmeier, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class PersistantReferenceGap extends PersistantBasicDiff implements Comparable<PersistantReferenceGap>, Serializable {
    
    private static final long serialVersionUID = 1L;

    private int order;
    
    /**
     * Creates a new comparable gap in the reference sequence.
     * @param position the absolute reference position at which the gap is added
     * @param base the base to add
     * @param order the index (beginning with 0) of the reference gap at the 
     * given position
     * @param isForwardStrand true, if it is on the fwd strand, false otherwise
     * @param count the number of occurrences of the gap
     * @param baseQuality The phred base quality value for the diff base in the
     * read.
     * @param mappingQuality The phred mapping quality value of the read from
     * which the diff originated
     */
    public PersistantReferenceGap(int position, Character base, int order, boolean isForwardStrand, int count, byte baseQuality, Byte mappingQuality){
        super(position, base, isForwardStrand, count, baseQuality, mappingQuality);
        this.order = order;
    }

    /**
     * @return the index (beginning with 0) of the reference gap at the 
     * given position
     */
    public int getOrder() {
        return order;
    }

    /**
     * Only use this compare method for comparing gaps of the same mappings.
     * @param o
     * @return 
     */
    @Override
    public int compareTo(PersistantReferenceGap o) {

        // order by position
        if (this.getPosition() < o.getPosition()) {
            return -1;
        } else if (this.getPosition() > o.getPosition()) {
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
