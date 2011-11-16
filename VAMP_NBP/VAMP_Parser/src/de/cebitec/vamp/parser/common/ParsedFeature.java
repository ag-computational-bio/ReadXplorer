package de.cebitec.vamp.parser.common;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedFeature {
    
    private Integer type;
    private Integer start;
    private Integer stop;
    private Integer strand;
    private String locusTag;
    private String product;
    private String ecNumber;
    private String geneName;

    /**
     * Contains all available information about a persistant feature
     * @param type FeatureType.CDS, FeatureType.REPEAT_UNIT, FeatureType.R_RNA, FeatureType.SOURCE,
                   FeatureType.T_RNA, FeatureType.MISC_RNA, FeatureType.MI_RNA, FeatureType.GENE,
                   FeatureType.M_RNA
     * @param start start position
     * @param stop stop position
     * @param strand SequenceUtils.STRAND_FWD for featues on forward and SequenceUtils.STRAND_REV on reverse strand
     * @param locusTag locus information
     * @param product description of the protein product
     * @param ecNumber ec number
     * @param geneName name of the gene, if it exists (e.g. "dnaA")
     */
    public ParsedFeature(int type, int start, int stop, int strand, String locusTag, String product, String ecNumber, String geneName){
        this.type = type; // if type is null, 0 is assumed, which is equal to FeatureType.UNDEFINED
        this.start = start;
        this.stop = stop;
        this.strand = strand;
        this.locusTag = locusTag;
        this.product = product;
        this.ecNumber = ecNumber;
        this.geneName = geneName;
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
    
    public boolean hasGeneName() {
        return this.geneName != null ? true : false;
    }

    public String getGeneName() {
        return this.geneName;
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

    /**
     * @return start of the feature. Always the smaller value among start and stop.
     */
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

    /**
     * @return stop of the feature. Always the larger value among start and stop.
     */
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
