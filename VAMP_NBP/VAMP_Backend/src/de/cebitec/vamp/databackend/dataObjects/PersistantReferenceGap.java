package de.cebitec.vamp.databackend.dataObjects;

import java.io.Serializable;

/**
 *
 * @author ddoppmeier
 */
public class PersistantReferenceGap implements Comparable<PersistantReferenceGap>, Serializable {

    private int position;
    private Character base;
    private int order;
    boolean isForwardStrand;
    private int count;

    public PersistantReferenceGap(int position, Character base, int order, boolean isForwardStrand, int count){
        this.position = position;
        this.base = base;
        this.order = order;
        this.isForwardStrand = isForwardStrand;
        this.count = count;
    }

    public int getPosition(){
        return position;
    }

    public Character getBase(){
        return base;
    }

    public int getOrder() {
        return order;
    }

    public boolean isForwardStrand() {
        return isForwardStrand;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(PersistantReferenceGap o) {
        // only use this compare method for comparing gaps of the same mappings

        // order by position
        if(position < o.getPosition()){
            return -1;
        } else if(position > o.getPosition()){
            return 1;
        } else {

            // order by position in mapping
            if(order < o.getOrder()){
                return -1;
            } else if(order > o.getOrder()){
                return 1;
            } else {
                return 0;
            }
        }

    }

}
