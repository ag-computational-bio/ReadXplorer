package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.api.objects.FeatureType;
import java.util.List;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedFeature {
    
    private FeatureType type;
    private Integer start;
    private Integer stop;
    private Integer strand;
    private String locusTag;
    private String product;
    private String ecNumber;
    private String geneName;
    private List<ParsedSubfeature> subfeatures;

    /**
     * Contains all available information about a persistant feature
     * @param type FeatureType.CDS, FeatureType.REPEAT_UNIT, FeatureType.R_RNA, FeatureType.SOURCE,
                   FeatureType.T_RNA, FeatureType.MISC_RNA, FeatureType.MI_RNA, FeatureType.GENE,
                   FeatureType.M_RNA (mandatory)
     * @param start start position (mandatory)
     * @param stop stop position (mandatory)
     * @param strand SequenceUtils.STRAND_FWD for featues on forward and SequenceUtils.STRAND_REV on reverse strand
     * @param locusTag locus information
     * @param product description of the protein product
     * @param ecNumber ec number
     * @param geneName name of the gene, if it exists (e.g. "dnaA")
     * @param subfeatures the list of subfeatures belonging to this feature
     */
    public ParsedFeature(FeatureType type, int start, int stop, int strand, String locusTag, String product, 
                String ecNumber, String geneName, List<ParsedSubfeature> subfeatures){
        this.type = type; // if type is null, 0 is assumed, which is equal to FeatureType.UNDEFINED
        this.start = start;
        this.stop = stop;
        this.strand = strand;
        this.locusTag = locusTag;
        this.product = product;
        this.ecNumber = ecNumber;
        this.geneName = geneName;
        this.subfeatures = subfeatures;
    }

    public boolean hasEcNumber(){
        return ecNumber != null;
    }

    public String getEcNumber() {
        return ecNumber;
    }
    
    public boolean hasGeneName() {
        return this.geneName != null;
    }

    public String getGeneName() {
        return this.geneName;
    }

    public boolean hasLocusTag(){
        return locusTag != null;
    }

    public String getLocusTag() {
        return locusTag;
    }

    public boolean hasProduct(){
        return product != null;
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

   public boolean hasStrand(){
        return strand != null && strand.intValue() != 0;
    } 

    public int getStrand() {
        return strand;
    }

    public FeatureType getType() {
        return type;
    }

    /**
     * @return the list of exons of this feature or an empty list if there are no exons
     */
    public List<ParsedSubfeature> getSubfeatures() {
        return subfeatures;
    }

    /**
     * Adds a subfeature to the list of subfeatures (e.g. an exon to a gene).
     * @param parsedSubfeature the subfeature to add.
     */
    public void addSubfeature(ParsedSubfeature parsedSubfeature) {
        this.subfeatures.add(parsedSubfeature);
    }

}
