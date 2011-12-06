package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A persistant feature. Containing background information about a feature, such as id,
 * ec number, locus, product, start and stop positions, strand and type.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantFeature implements PersistantFeatureI {
    
    private int id;
    private String ecNumber;
    private String locus;
    private String product;
    private int start;
    private int stop;
    private int strand;
    private FeatureType type;
    private String geneName;
    private List<PersistantSubfeature> subfeatures;

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
        this.subfeatures = new ArrayList<PersistantSubfeature>();
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
     * @return true, if the feature has a locus, false otherwise
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
     * Returns if the feature is located on the fwd or rev strand.
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
     * @return true, if the feature has a gene name, false otherwise
     */
    public boolean hasGeneName() {
        return this.geneName != null;
    }

    @Override
    public String toString(){
        return locus;
    }

    /**
     * @return the list of subfeatures (e.g. exons) of this feature 
     * or an empty list if there are no subfeatures.
     */
    public List<PersistantSubfeature> getSubfeatures() {
        return subfeatures;
    }

    /**
     * Adds a subfeature to the list of subfeatures (e.g. an exon to a gene).
     * @param parsedSubfeature the subfeature to add.
     */
    public void addSubfeature(PersistantSubfeature parsedSubfeature) {
        this.subfeatures.add(parsedSubfeature);
    }
    
    /**
     * Utility method for creating a mapping of features to their id.
     * @param features list of features for which the mapping should be creates
     * @return the map of feature ids to their corresponding feature
     */
    public static Map<Integer, PersistantFeature> getFeatureMap(List<PersistantFeature> features){
        Map<Integer, PersistantFeature> featureMap = new HashMap<Integer, PersistantFeature>();
        for (PersistantFeature feature : features){
            featureMap.put(feature.getId(), feature); //ids are unique
        }
        return featureMap;
    }
    
    
    /**
     * Utility method for adding a list of subfeatures to their parent features list.
     */
    public static void addSubfeatures(Map<Integer, PersistantFeature> featuresSorted, List<PersistantSubfeature> subfeaturesSorted) {
        int id;

        for (PersistantSubfeature subfeature : subfeaturesSorted) {
            id = subfeature.getParentId();
            if (featuresSorted.containsKey(id)) { //just to be on the save side; should not occur
                featuresSorted.get(id).addSubfeature(subfeature);
            }
        }
    }
}
