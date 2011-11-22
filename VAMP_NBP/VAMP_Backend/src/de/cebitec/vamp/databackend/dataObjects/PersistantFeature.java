package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 * A persistant feature. Containing background information about a feature, such as id,
 * ec number, locus, product, start and stop positions, strand and type.
 *
 * @author ddoppmeier
 */
public class PersistantFeature {
    
    private int id;
    private String ecNumber;
    private String locus;
    private String product;
    private int start;
    private int stop;
    private int strand;
    private FeatureType type;
    private String geneName;

    /**
     * @param id id of the feature in db 
     * @param type FeatureType.CDS, FeatureType.REPEAT_UNIT, FeatureType.R_RNA, FeatureType.SOURCE,
              FeatureType.T_RNA, FeatureType.MISC_RNA, FeatureType.MI_RNA, FeatureType.GENE, FeatureType.M_RNA
     * @param start start position
     * @param stop stop position
     * @param strand SequenceUtils.STRAND_FWD for featues on forward and SequenceUtils.STRAND_REV on reverse strand
     * @param locus locus information
     * @param product description of the protein product
     * @param ecnum ec number
     * @param geneName name of the gene, if it exists (e.g. "dnaA")
     */
    public PersistantFeature(int id, String ecnum, String locus, String product, 
                int start, int stop, int strand, FeatureType type, String geneName) {
        this.id = id;
        this.ecNumber = ecnum;
        this.locus = locus;
        this.product = product;
        this.start = start;
        this.stop = stop;
        this.strand = strand;
        this.type = type;
        this.geneName = geneName;
    }

    public String getEcNumber() {
        return ecNumber;
    }

    public int getId() {
        return id;
    }

    public String getLocus() {
        return locus;
    }

    public String getProduct() {
        return product;
    }

    /**
     * @return start of the feature. Always the smaller value among start and stop.
     */
    public int getStart() {
        return start;
    }

    /**
     * @return stop of the feature. Always the larger value among start and stop.
     */
    public int getStop() {
        return stop;
    }

    /**
     * Returns if the feature is located on the fwd or rev strand.
     * @return SequenceUtils.STRAND_FWD for featues on forward and SequenceUtils.STRAND_REV on reverse strand
     */
    public int getStrand() {
        return strand;
    }

    public FeatureType getType() {
        return type;
    }
    
    /**
     * @return the gene name. Caution: may be null!
     */
    public String getGeneName() {
        return this.geneName;
    }
    
    public boolean hasGeneName() {
        return this.geneName != null ? true : false;
    }

    @Override
    public String toString(){
        return locus;
    }

}
