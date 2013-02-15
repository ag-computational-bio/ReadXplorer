package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.util.SequenceUtils;
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
    private boolean isFwdStrand;
    private FeatureType type;
    private String featureName;
    private List<PersistantSubFeature> subFeatures;

    /**
     * @param id id of the feature in db 
     * @param type FeatureType.CDS, FeatureType.REPEAT_UNIT, FeatureType.R_RNA, FeatureType.SOURCE,
              FeatureType.T_RNA, FeatureType.MISC_RNA, FeatureType.MI_RNA, FeatureType.GENE, FeatureType.M_RNA
     * @param start start position
     * @param stop stop position
     * @param isFwdStrand SequenceUtils.STRAND_FWD for featues on forward and SequenceUtils.STRAND_REV on reverse strand
     * @param locus locus information
     * @param product description of the protein product
     * @param ecnum ec number
     * @param featureName name of the feature, if it exists (e.g. "dnaA")
     */
    public PersistantFeature(int id, String ecnum, String locus, String product, 
                int start, int stop, boolean isFwdStrand, FeatureType type, String featureName) {
        this.subFeatures = new ArrayList<>();
        this.id = id;
        this.ecNumber = ecnum;
        this.locus = locus;
        this.product = product;
        this.start = start;
        this.stop = stop;
        this.isFwdStrand = isFwdStrand;
        this.type = type;
        this.featureName = featureName;
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
     * @return true for featues on forward and false on reverse strand
     */
    public boolean isFwdStrand() {
        return isFwdStrand;
    }
    
    /**
     * @return SequenceUtils.STRAND_FWD_STRING ("Fwd") or SequenceUtils.STRAND_REV_STRING ("Rev")
     */
    public String isFwdStrandString() {
        return isFwdStrand ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING;
    }

    /**
     * @return the type of the feature among: FeatureType.CDS, FeatureType.REPEAT_UNIT, FeatureType.R_RNA, 
     *      FeatureType.SOURCE,  FeatureType.T_RNA, FeatureType.MISC_RNA, FeatureType.MI_RNA, FeatureType.GENE, 
     *      FeatureType.M_RNA
     */
    @Override
    public FeatureType getType() {
        return type;
    }
    
    /**
     * @return the gene name. Caution: may be null!
     */
    public String getFeatureName() {
        return this.featureName;
    }
    
    /**
     * @return true, if the feature has a gene name, false otherwise
     */
    public boolean hasFeatureName() {
        return this.featureName != null;
    }

    @Override
    public String toString(){
        return this.locus != null && !this.locus.isEmpty() ? 
                this.locus : "Feature with start: " + this.start + ", stop: " + this.stop;
    }

    /**
     * @return the list of sub features (e.g. exons) of this feature
     * or an empty list if there are no sub features.
     */
    public List<PersistantSubFeature> getSubFeatures() {
        return subFeatures;
    }

    /**
     * Adds a sub feature to the list of sub features (e.g. an exon to a gene).
     * @param parsedSubFeature the sub feature to add.
     */
    public void addSubFeature(PersistantSubFeature parsedSubFeature) {
        this.subFeatures.add(parsedSubFeature);
    }
    
    /**
     * Utility method for creating a mapping of features to their id.
     * @param features list of features for which the mapping should be creates
     * @return the map of feature ids to their corresponding feature
     */
    public static Map<Integer, PersistantFeature> getFeatureMap(List<PersistantFeature> features){
        Map<Integer, PersistantFeature> featureMap = new HashMap<>();
        for (PersistantFeature feature : features){
            featureMap.put(feature.getId(), feature); //ids are unique
        }
        return featureMap;
    }
    
    
    /**
     * Utility method for adding a list of sub features to their parent features list.
     * @param features the list of features, to which the subfeatures are added
     * @param subFeaturesSorted the sorted list of subfeatures by increasing start position
     */
    public static void addSubFeatures(Map<Integer, PersistantFeature> features, 
            List<PersistantSubFeature> subFeaturesSorted) {
        
        int id;
        for (PersistantSubFeature subFeature : subFeaturesSorted) {
            id = subFeature.getParentId();
            if (features.containsKey(id)) { //just to be on the save side; should not occur
                features.get(id).addSubFeature(subFeature);
            }
        }
    }
    
    /**
     * Retrieves the best possible name for the feature. First it checks the gene
     * name, then the locus information and if both are not given it returns
     * "Feature with start: x, stop: y"
     * @param feature the feature whose name is wanted
     * @return the best possible name for the feature or null, if the feature was null.
     */
    public static String getFeatureName(PersistantFeature feature) {
        String featureName = null;
        if (feature != null) {
            if (feature.getFeatureName() != null && !feature.getFeatureName().isEmpty()) {
                featureName = feature.getFeatureName();
            } else if (feature.getLocus() != null && !feature.getLocus().isEmpty()) {
                featureName = feature.getLocus();
            } else {
                featureName = "Feature with start: " + feature.getStart() + ", stop: " + feature.getStop();
            }
        }
        return featureName;
    }
}
