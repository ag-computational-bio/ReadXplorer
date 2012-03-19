package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.api.objects.FeatureType;
import java.util.List;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedAnnotation {
    
    private FeatureType type;
    private Integer start;
    private Integer stop;
    private Integer strand;
    private String locusTag;
    private String product;
    private String ecNumber;
    private String geneName;
    private List<ParsedSubAnnotation> subAnnotations;

    /**
     * Contains all available information about a persistant annotation
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
     * @param subAnnotations the list of sub annotations belonging to this annotation
     */
    public ParsedAnnotation(FeatureType type, int start, int stop, int strand, String locusTag, String product, 
                String ecNumber, String geneName, List<ParsedSubAnnotation> subAnnotations){
        this.type = type; // if type is null, 0 is assumed, which is equal to FeatureType.UNDEFINED
        this.start = start;
        this.stop = stop;
        this.strand = strand;
        this.locusTag = locusTag;
        this.product = product;
        this.ecNumber = ecNumber;
        this.geneName = geneName;
        this.subAnnotations = subAnnotations;
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
     * @return start of the annotation. Always the smaller value among start and stop.
     */
    public int getStart() {
        return start;
    }

    /**
     * @return stop of the annotation. Always the larger value among start and stop.
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
     * @return the list of exons of this annotation or an empty list if there are no exons
     */
    public List<ParsedSubAnnotation> getSubAnnotations() {
        return subAnnotations;
    }

    /**
     * Adds a sub annotation to the list of sub annotations (e.g. an exon to a gene).
     * @param parsedSubAnnotation the sub annotation to add.
     */
    public void addSubAnnotation(ParsedSubAnnotation parsedSubAnnotation) {
        this.subAnnotations.add(parsedSubAnnotation);
    }

}
