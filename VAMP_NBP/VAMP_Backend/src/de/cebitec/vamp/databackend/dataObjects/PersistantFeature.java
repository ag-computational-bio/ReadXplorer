package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.util.polyTree.Node;
import de.cebitec.vamp.util.polyTree.Polytree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A persistant feature. Containing background information about a feature, such
 * as id, ec number, locus, product, start and stop positions, strand and type.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantFeature extends Node implements PersistantFeatureI, Comparable<PersistantFeature> {
    
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
    private List<Integer> parentIds;
    private int frame;

    /**
     * @param id id of the feature in db 
     * @param parentIds The string containing all ids of the parents of this 
     * feature separated by ";", if it has at least one. If not this string is 
     * empty.
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
    public PersistantFeature(int id, String parentIds, String ecnum, String locus, String product, 
                int start, int stop, boolean isFwdStrand, FeatureType type, String featureName) {
        super(type, null);
        this.subFeatures = new ArrayList<>();
        this.id = id;
        this.parentIds = this.separateParentIds(parentIds);
        this.ecNumber = ecnum;
        this.locus = locus;
        this.product = product;
        this.start = start;
        this.stop = stop;
        this.isFwdStrand = isFwdStrand;
        this.type = type;
        this.featureName = featureName;
    }

    /**
     * Separates the parent id string into a list of parent ids.
     * @param parentIds The parent ids string to separate
     * @return A list of parent ids.
     */
    private List<Integer> separateParentIds(String parentIds) {
        List<Integer> parentIdList = new ArrayList<>();
        String[] parentIdArray = parentIds.split(";");
        for (int i = 0; i < parentIdArray.length; ++i) {
            try {
                if (!parentIdArray[i].equals(Properties.NO_PARENT_STRING)) {
                    parentIdList.add(Integer.parseInt(parentIdArray[i]));
                }
            } catch (NumberFormatException e) {
                //ignore and continue
            }
        }
        return parentIdList;
    }

    /**
     * @return The ec number, if it was set.
     */
    public String getEcNumber() {
        return ecNumber;
    }

    /**
     * @return The unique id of this feature
     */
    public int getId() {
        return id;
    }

    /**
     * @return The id of the parent of this feature, if it has one. If not "0"
     * has to be used to signal that this feature is a top level feature.
     */
    public List<Integer> getParentIds() {
        return parentIds;
    }

    /**
     * @return <code>true</code>, if the feature has a locus, 
     * <code>false</code> otherwise.
     */
    public boolean hasLocus() {
        return this.locus != null;
    }
    
    /**
     * @return The locus of the feature, if it is set.
     */
    public String getLocus() {
        return locus;
    }

    /**
     * @return The product of the feature, if it is set.
     */
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

    /**
     * Retrieves the best possible string representation of the feature. First 
     * it checks the feature name, then the locus information, then the EC 
     * number and if all are not given it returns "Feature with start: x, 
     * stop: y".
     * @return the best possible name for the feature.
     */
    @Override
    public String toString(){
        String returnString;
        if (this.featureName != null && !this.featureName.isEmpty()) {
            returnString = this.featureName;   
        } else if (this.locus != null && !this.locus.isEmpty()) {
            returnString = this.locus;         
        } else if (this.ecNumber != null && !this.ecNumber.isEmpty()) {
            returnString = this.ecNumber;           
        } else {
            returnString = "Feature with start: " + this.start + ", stop: " + this.stop;
        }
        
        return returnString;
    }

    /**
     * @param frame The <tt>frame</tt> in which this feature should be displayed
     */
    public void setFrame(int frame) {
        this.frame = frame;
    }

    /**
     * @return The frame in which this feature should be displayed
     */
    public int getFrame() {
        return frame;
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
     * Compares two PersistantFeature based on their start position. '0' is returned for
     * equal start positions, 1, if the start position of the other is larger
     * and -1, if the start position of this mapping is larger.
     * @param feature mapping to compare to this mapping 
     * @return '0' for equal start positions, 1, if the start
     * position of the other is larger and -1, if the start position of this
     * mapping is larger.
     */
    @Override
    public int compareTo(PersistantFeature feature) {
        int ret = 0;
        if (this.start < feature.getStart()) {
            ret = -1;
        } else if (this.start > feature.getStart()) {
            ret = 1;
        }
        return ret;
    } 
    /**
     * Static inner class containig all utility methods for 
     * <tt>PersistantFeatures</tt>.
     */
    public static class Utils {

        /**
         * Utility method for creating a mapping of features to their id.
         * @param features List of features for which the mapping should be
         * created
         * @return The map of feature ids to their corresponding feature
         */
        public static Map<Integer, PersistantFeature> getFeatureMap(List<PersistantFeature> features) {
            Map<Integer, PersistantFeature> featureMap = new HashMap<>();
            for (PersistantFeature feature : features) {
                featureMap.put(feature.getId(), feature); //ids are unique
            }
            return featureMap;
        }

        /**
         * Utility method for adding a list of sub features to their parent
         * features list.
         * @param features the list of features, to which the subfeatures are
         * added
         * @param subFeaturesSorted the sorted list of subfeatures by increasing
         * start position
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
         * Creates a list of polytree structures from a list of features. In a
         * polytree a node can have multiple parents and children. Features
         * without parents are the roots and all features with parents are place
         * as children of their parents in the tree.
         * @param features The features to join in a polytree structure
         * @return the list of polytrees for the features
         */
        public static List<Polytree> createFeatureTrees(List<PersistantFeature> features) {
            Map<Integer, PersistantFeature> featMap = PersistantFeature.Utils.getFeatureMap(features);
            PersistantFeature.Utils.addParentFeatures(featMap, features);
            List<Polytree> featTrees = new ArrayList<>();
            Polytree tree;
            List<Node> roots;
            List<Node> omitList = new ArrayList<>();
            List<Node> parentList;
            for (PersistantFeature feat : features) {

                if (feat.isRoot() && !omitList.contains(feat)) {
                    roots = new ArrayList<>();
                    roots.add(feat);
                    tree = new Polytree(roots);
                    featTrees.add(tree);
                    
                    //check root children for more than one parent and add other parents to roots
                    for (Node child : feat.getNodeChildren()) {
                        parentList = child.getParents();
                        if (parentList.size() > 1) {
                            for (Node parent : parentList) {
                                if (!roots.contains(parent)) {
                                    roots.add(parent);
                                    omitList.add(parent); //these parents will be visited later in the feature list
                                }
                            }
                        }
                    }
                }
            }
            return featTrees;
        }

        /**
         * Utility method for adding a list of sub features to their parent
         * features list.
         * @param features the map of features, to which the subfeatures are
         * added
         * @param featuresSorted the sorted list of features to add to their
         * parents by increasing start position
         */
        public static void addParentFeatures(Map<Integer, PersistantFeature> features,
                List<PersistantFeature> featuresSorted) {

            List<Integer> ids;
            for (PersistantFeature feature : featuresSorted) {
                ids = feature.getParentIds();
                for (int id : ids) {
                    if (features.containsKey(id)) {
                        features.get(id).addChild(feature);
                    } //else we cannot add the child to its parent
                }
            }
        }
    }
}
