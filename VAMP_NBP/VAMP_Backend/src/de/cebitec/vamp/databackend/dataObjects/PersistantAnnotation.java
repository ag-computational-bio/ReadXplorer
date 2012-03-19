package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A persistant annotation. Containing background information about a annotation, such as id,
 * ec number, locus, product, start and stop positions, strand and type.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantAnnotation implements PersistantAnnotationI {
    
    private int id;
    private String ecNumber;
    private String locus;
    private String product;
    private int start;
    private int stop;
    private int strand;
    private FeatureType type;
    private String geneName;
    private List<PersistantSubAnnotation> subAnnotations;

    /**
     * @param id id of the annotation in db 
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
    public PersistantAnnotation(int id, String ecnum, String locus, String product, 
                int start, int stop, int strand, FeatureType type, String geneName) {
        this.subAnnotations = new ArrayList<PersistantSubAnnotation>();
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

    /**
     * @return true, if the annotation has a locus, false otherwise
     */
    public boolean hasLocus() {
        return this.locus != null;
    }
    
    public String getLocus() {
        return locus;
    }

    public String getProduct() {
        return product;
    }


    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getStop() {
        return stop;
    }

    /**
     * Returns if the annotation is located on the fwd or rev strand.
     * @return SequenceUtils.STRAND_FWD for featues on forward and SequenceUtils.STRAND_REV on reverse strand
     */
    public int getStrand() {
        return strand;
    }

    @Override
    public FeatureType getType() {
        return type;
    }
    
    /**
     * @return the gene name. Caution: may be null!
     */
    public String getGeneName() {
        return this.geneName;
    }
    
    /**
     * @return true, if the annotation has a gene name, false otherwise
     */
    public boolean hasGeneName() {
        return this.geneName != null;
    }

    @Override
    public String toString(){
        return this.locus != null && !this.locus.isEmpty() ? 
                this.locus : "Annotation with start: " + this.start + ", stop: " + this.stop;
    }

    /**
     * @return the list of sub annotations (e.g. exons) of this annotation
     * or an empty list if there are no sub annotations.
     */
    public List<PersistantSubAnnotation> getSubAnnotations() {
        return subAnnotations;
    }

    /**
     * Adds a sub annotation to the list of sub annotations (e.g. an exon to a gene).
     * @param parsedSubAnnotation the sub annotation to add.
     */
    public void addSubAnnotation(PersistantSubAnnotation parsedSubAnnotation) {
        this.subAnnotations.add(parsedSubAnnotation);
    }
    
    /**
     * Utility method for creating a mapping of annotations to their id.
     * @param annotations list of annotations for which the mapping should be creates
     * @return the map of annotation ids to their corresponding annotation
     */
    public static Map<Integer, PersistantAnnotation> getAnnotationMap(List<PersistantAnnotation> annotations){
        Map<Integer, PersistantAnnotation> annotationMap = new HashMap<Integer, PersistantAnnotation>();
        for (PersistantAnnotation annotation : annotations){
            annotationMap.put(annotation.getId(), annotation); //ids are unique
        }
        return annotationMap;
    }
    
    
    /**
     * Utility method for adding a list of sub annotations to their parent annotations list.
     */
    public static void addSubAnnotations(Map<Integer, PersistantAnnotation> annotationsSorted, 
            List<PersistantSubAnnotation> subAnnotationsSorted) {
        
        int id;
        for (PersistantSubAnnotation subAnnotation : subAnnotationsSorted) {
            id = subAnnotation.getParentId();
            if (annotationsSorted.containsKey(id)) { //just to be on the save side; should not occur
                annotationsSorted.get(id).addSubAnnotation(subAnnotation);
            }
        }
    }
    
    /**
     * Retrieves the best possible name for the annotation. First it checks the gene
     * name, then the locus information and if both are not given it returns
     * "Annotation with start: x, stop: y"
     * @param annotation the annotation whose name is wanted
     * @return the best possible name for the annotation or null, if the annotation was null.
     */
    public static String getAnnotationName(PersistantAnnotation annotation) {
        String annotationName = null;
        if (annotation != null) {
            if (annotation.getGeneName() != null && !annotation.getGeneName().isEmpty()) {
                annotationName = annotation.getGeneName();
            } else if (annotation.getLocus() != null && !annotation.getLocus().isEmpty()) {
                annotationName = annotation.getLocus();
            } else {
                annotationName = "Annotation with start: " + annotation.getStart() + ", stop: " + annotation.getStop();
            }
        }
        return annotationName;
    }
}
