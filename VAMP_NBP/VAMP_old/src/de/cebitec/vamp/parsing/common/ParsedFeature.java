package de.cebitec.vamp.parsing.common;

/**
 *
 * @author ddoppmeier
 */
public class ParsedFeature {

    private Integer type;
    private Integer start;
    private Integer stop;
    private Integer strand;
    private String locusTag;
    private String product;
    private String ecNumber;

    public ParsedFeature(int type, int start, int stop, int strand, String locusTag, String product, String ecNumber){
        this.type = type; // if type is null, 0 is assumed, which is equal to FeatureType.UNDEFINED
        this.start = start;
        this.stop = stop;
        this.strand = strand;
        this.locusTag = locusTag;
        this.product = product;
        this.ecNumber = ecNumber;
    }

    public boolean hasEcNumber(){
        if(ecNumber != null){
            return true;
        } else {
            return false;
        }
    }

    public String getEcNumber() {
        return ecNumber;
    }

    public boolean hasLocusTag(){
        if(locusTag != null){
            return true;
        } else {
            return false;
        }
    }

    public String getLocusTag() {
        return locusTag;
    }

    public boolean hasProduct(){
        if(product != null){
            return true;
        } else {
            return false;
        }
    }

    public String getProduct() {
        return product;
    }

    public boolean hasStart(){
        if(start != null){
            return true;
        } else {
            return false;
        }
    }

    public int getStart() {
        return start;
    }

   public boolean hasStop(){
        if(stop != null){
            return true;
        } else {
            return false;
        }
    }

    public int getStop() {
        return stop;
    }

   public boolean hasStrand(){
        if(strand != null && strand.intValue() != 0){
            return true;
        } else {
            return false;
        }
    } 

    public int getStrand() {
        return strand;
    }

    public boolean hasType(){
        if(type != null){
            return true;
        } else {
            return false;
        }
    }

    public int getType() {
        return type;
    }

}
