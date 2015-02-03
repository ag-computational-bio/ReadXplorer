/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.databackend.dataObjects;


import de.cebitec.readxplorer.utils.PositionUtils;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.polytree.Node;
import de.cebitec.readxplorer.utils.polytree.Polytree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A persistent feature. Containing background information about a feature, such
 * as id, ec number, locus, product, start and stop positions, strand and type.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistentFeature extends Node implements PersistentFeatureI,
                                                       Comparable<PersistentFeature> {

    private final int id;
    private final int chromId;
    private final String ecNumber;
    private final String locus;
    private final String product;
    private final int start;
    private final int stop;
    private final boolean isFwdStrand;
    private final FeatureType type;
    private final String featureName;
//    private List<PersistentSubFeature> subFeatures;
    private final List<Integer> parentIds;
    private int frame;


    /**
     * @param id          id of the feature in db
     * @param chromId     Chromosome id of the feature
     * @param parentIds   The string containing all ids of the parents of this
     *                    feature separated by ";", if it has at least one. If not this string is
     *                    empty.
     * @param type        FeatureType.CDS, FeatureType.REPEAT_UNIT,
     *                    FeatureType.R_RNA, FeatureType.SOURCE,
     *                    FeatureType.T_RNA, FeatureType.MISC_RNA,
     *                    FeatureType.MI_RNA, FeatureType.GENE,
     *                    FeatureType.M_RNA
     * @param start       start position
     * @param stop        stop position
     * @param isFwdStrand SequenceUtils.STRAND_FWD for featues on forward and
     *                    SequenceUtils.STRAND_REV on reverse strand
     * @param locus       locus information
     * @param product     description of the protein product
     * @param ecnum       ec number
     * @param featureName name of the feature, if it exists (e.g. "dnaA")
     */
    public PersistentFeature( int id, int chromId, String parentIds, String ecnum, String locus, String product,
                              int start, int stop, boolean isFwdStrand, FeatureType type, String featureName ) {
        super( type, null );
//        this.subFeatures = new ArrayList<>();
        this.id = id;
        this.chromId = chromId;
        this.parentIds = this.separateParentIds( parentIds );
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
     * <p>
     * @param parentIds The parent ids string to separate
     * <p>
     * @return A list of parent ids.
     */
    private List<Integer> separateParentIds( String parentIds ) {
        String[] parentIdArray = parentIds.split( ";" );
        List<Integer> parentIdList = new ArrayList<>( parentIdArray.length );
        for( String parentId : parentIdArray ) {
            try {
                if( !Properties.NO_PARENT_STRING.equals( parentId ) ) {
                    parentIdList.add( Integer.parseInt( parentId ) );
                }
            }catch( NumberFormatException e ) {
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
     * @return The chromosome id of this feature.
     */
    public int getChromId() {
        return chromId;
    }


    /**
     * @return The id of the parent of this feature, if it has one. If not "0"
     *         has to be used to signal that this feature is a top level feature.
     */
    public List<Integer> getParentIds() {
        return Collections.unmodifiableList( parentIds );
    }


    /**
     * @return <code>true</code>, if the feature has a locus,
     *         <code>false</code> otherwise.
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


    /**
     * @return start of the feature. Always the smaller value among start and
     *         stop.
     */
    @Override
    public int getStart() {
        return start;
    }


    /**
     * @return stop of the feature. Always the larger value among start and
     *         stop.
     */
    @Override
    public int getStop() {
        return stop;
    }


    /**
     * @return The length of the feature in base pairs.
     */
    public int getLength() {
        return stop - start + 1;
    }


    /**
     * @return The start position on the feature strand = smaller position for
     *         features on the fwd and larger position for features on the rev strand.
     */
    public int getStartOnStrand() {
        return this.isFwdStrand ? start : stop;
    }


    /**
     * @return The stop position on the feature strand = smaller position for
     *         features on the rev and larger position for features on the fwd strand.
     */
    public int getStopOnStrand() {
        return this.isFwdStrand ? stop : start;
    }


    /**
     * Returns if the feature is located on the fwd or rev strand.
     * <p>
     * @return true for featues on forward and false on reverse strand
     */
    public boolean isFwdStrand() {
        return isFwdStrand;
    }


    /**
     * @return SequenceUtils.STRAND_FWD_STRING ("Fwd") or
     *         SequenceUtils.STRAND_REV_STRING ("Rev")
     */
    public String isFwdStrandString() {
        return isFwdStrand ? SequenceUtils.STRAND_FWD_STRING : SequenceUtils.STRAND_REV_STRING;
    }


    /**
     * @return the type of the feature among: FeatureType.CDS,
     *         FeatureType.REPEAT_UNIT, FeatureType.R_RNA,
     *         FeatureType.SOURCE, FeatureType.T_RNA, FeatureType.MISC_RNA,
     *         FeatureType.MI_RNA, FeatureType.GENE,
     *         FeatureType.M_RNA
     */
    @Override
    public FeatureType getType() {
        return type;
    }


    /**
     * @return the gene name. Caution: may be null!
     */
    public String getName() {
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
     * <p>
     * @return the best possible name for the feature.
     */
    @Override
    public String toString() {
        String returnString;
        if( this.featureName != null && !this.featureName.isEmpty() ) {
            returnString = this.featureName;
        }
        else if( this.locus != null && !this.locus.isEmpty() ) {
            returnString = this.locus;
        }
        else if( this.ecNumber != null && !this.ecNumber.isEmpty() ) {
            returnString = this.ecNumber;
        }
        else {
            returnString = "Feature with start: " + this.start + ", stop: " + this.stop;
        }

        return returnString;
    }


    /**
     * @param frame The <tt>frame</tt> in which this feature should be displayed
     */
    public void setFrame( int frame ) {
        this.frame = frame;
    }


    /**
     * @return The frame in which this feature should be displayed
     */
    public int getFrame() {
        return frame;
    }

//    /**
//     * @return the list of sub features (e.g. exons) of this feature
//     * or an empty list if there are no sub features.
//     */
//    public List<PersistentSubFeature> getSubFeatures() {
//        return subFeatures;
//    }
//
//    /**
//     * Adds a sub feature to the list of sub features (e.g. an exon to a gene).
//     * @param parsedSubFeature the sub feature to add.
//     */
//    public void addSubFeature(PersistentSubFeature parsedSubFeature) {
//        this.subFeatures.add(parsedSubFeature);
//    }

    /**
     * Compares two PersistentFeature based on their start position. '0' is
     * returned for
     * equal start positions, 1, if the start position of the other is larger
     * and -1, if the start position of this mapping is larger.
     * <p>
     * @param feature mapping to compare to this mapping
     * <p>
     * @return '0' for equal start positions, 1, if the start
     *         position of the other is larger and -1, if the start position of this
     *         mapping is larger.
     */
    @Override
    public int compareTo( PersistentFeature feature ) {
        int ret = 0;
        if( this.start < feature.getStart() ) {
            ret = -1;
        }
        else if( this.start > feature.getStart() ) {
            ret = 1;
        }
        return ret;
    }


    /**
     * Static inner class containig all utility methods for
     * <tt>PersistentFeatures</tt>.
     */
    public static class Utils {

        /**
         * Utility class, no instantiation allowed.
         */
        private Utils() {
        }


        /**
         * @param feature feature whose frame has to be determined
         * <p>
         * @return 1, 2, 3, -1, -2, -3 depending on the reading frame of the
         *         feature
         */
        public static int determineFrame( PersistentFeature feature ) {
            int frame;

            if( feature.isFwdStrand() ) { // forward strand
                frame = PositionUtils.determineFwdFrame( feature.getStart() );
            }
            else { // reverse strand. start <= stop ALWAYS! so use stop for reverse strand
                frame = PositionUtils.determineRevFrame( feature.getStop() );
            }
            return frame;
        }


        /**
         * Utility method for creating a mapping of features to their id.
         * <p>
         * @param features List of features for which the mapping should be
         *                 created
         * <p>
         * @return The map of feature ids to their corresponding feature
         */
        public static Map<Integer, PersistentFeature> getFeatureMap( List<PersistentFeature> features ) {
            Map<Integer, PersistentFeature> featureMap = new HashMap<>( features.size() );
            for( PersistentFeature feature : features ) {
                featureMap.put( feature.getId(), feature ); //ids are unique
            }
            return featureMap;
        }


        /**
         * Utility method for creating a mapping of features to their locus.
         * <p>
         * @param features List of features for which the mapping should be
         *                 created
         * <p>
         * @return The map of feature locus to the corresponding feature
         */
        public static Map<String, PersistentFeature> getFeatureLocusMap( List<PersistentFeature> features ) {
            Map<String, PersistentFeature> featureMap = new HashMap<>( features.size() );
            for( PersistentFeature feature : features ) {
                featureMap.put( feature.getLocus(), feature ); //not necessarily unique, but should be found at the same position
            }
            return featureMap;
        }

//        /**
//         * Utility method for adding a list of sub features to their parent
//         * features list.
//         * @param features the list of features, to which the subfeatures are
//         * added
//         * @param subFeaturesSorted the sorted list of subfeatures by increasing
//         * start position
//         */
//        public static void addSubFeatures(Map<Integer, PersistentFeature> features,
//                List<PersistentSubFeature> subFeaturesSorted) {
//
//            int id;
//            for (PersistentSubFeature subFeature : subFeaturesSorted) {
//                id = subFeature.getParentId();
//                if (features.containsKey(id)) { //just to be on the save side; should not occur
//                    features.get(id).addSubFeature(subFeature);
//                }
//            }
//        }

        /**
         * Creates a list of polytree structures from a list of features. In a
         * polytree a node can have multiple parents and children. Features
         * without parents are the roots and all features with parents are place
         * as children of their parents in the tree.
         * <p>
         * @param features The features to join in a polytree structure
         * <p>
         * @return the list of polytrees for the features
         */
        public static List<Polytree> createFeatureTrees( List<PersistentFeature> features ) {
            PersistentFeature.Utils.addParentFeatures( features );
            List<Polytree> featTrees = new ArrayList<>( features.size() );
            List<Node> omitList = new ArrayList<>( 10 );
            for( PersistentFeature feat : features ) {
                if( feat.isRoot() && !omitList.contains( feat ) ) {
                    List<Node> roots = new ArrayList<>( 10 );
                    roots.add( feat );
                    Polytree tree = new Polytree( roots );
                    featTrees.add( tree );

                    //check root children for more than one parent and add other parents to roots
                    for( Node child : feat.getNodeChildren() ) {
                        List<Node> parentList = child.getParents();
                        if( parentList.size() > 1 ) {
                            for( Node parent : parentList ) {
                                if( !roots.contains( parent ) ) {
                                    roots.add( parent );
                                    omitList.add( parent ); //these parents will be visited later in the feature list
                                }
                            }
                        }
                    }
                }
            }
            return featTrees;
        }


        /**
         * Utility method for creating a parent - children relationship for the
         * given list of features. Wrapper for "getFeatureMap()" and
         * addParentFeature()" with a map and feature list parameters.
         * <p>
         * @param featuresSorted the sorted list of features who shall be added
         *                       in their correct hierarchy by increasing start position
         */
        public static void addParentFeatures( List<PersistentFeature> featuresSorted ) {
            Map<Integer, PersistentFeature> featMap = PersistentFeature.Utils.getFeatureMap( featuresSorted );
            PersistentFeature.Utils.addParentFeatures( featMap, featuresSorted );
        }


        /**
         * Utility method for adding a list of sub features to their parent
         * features list.
         * <p>
         * @param features       the map of features, to which the subfeatures
         *                       are
         *                       added
         * @param featuresSorted the sorted list of features to add to their
         *                       parents by increasing start position
         */
        public static void addParentFeatures( Map<Integer, PersistentFeature> features,
                                              List<PersistentFeature> featuresSorted ) {

            for( PersistentFeature feature : featuresSorted ) {
                List<Integer> ids = feature.getParentIds();
                for( int id : ids ) {
                    if( features.containsKey( id ) ) {
                        features.get( id ).addChild( feature );
                    } //else we cannot add the child to its parent
                }
            }
        }


        /**
         * Creates a new array list of the genomic features, which are among the
         * allowed feature types. All features, whose feature type is not in the
         * <code>selectedFeatureTypes</code> list is dismissed.
         * <p>
         * @param featuresToFilter     the list of features to filter
         * @param selectedFeatureTypes the set of feature types, which shall be
         *                             contained in the returned list of features
         * <p>
         * @return the filtered list of features. Only features of a type from
         *         the <code>selectedFeatureTypes</code> are returned
         */
        public static List<PersistentFeature> filterFeatureTypes( List<PersistentFeature> featuresToFilter, Set<FeatureType> selectedFeatureTypes ) {
            List<PersistentFeature> newFeatures = new ArrayList<>( featuresToFilter.size() );
            for( PersistentFeature feature : featuresToFilter ) {
                if( selectedFeatureTypes.contains( feature.getType() ) ) {
                    newFeatures.add( feature );
                }
            }
            return newFeatures;
        }


        /**
         * Creates a new array list of the genomic features, which are of the
         * allowed feature type. All features, whose feature type is different
         * than the <code>selectedFeatureType</code> is dismissed.
         * <p>
         * @param featuresToFilter    the list of features to filter
         * @param selectedFeatureType the feature type, which shall be contained
         *                            in the returned list of features
         * <p>
         * @return the filtered list of features. Only features of the type from
         *         the <code>selectedFeatureType</code> are returned
         */
        public static List<PersistentFeature> filterFeatureTypes( List<PersistentFeature> featuresToFilter, FeatureType selectedFeatureType ) {
            Set<FeatureType> featureTypeSet = new HashSet<>();
            featureTypeSet.add( selectedFeatureType );
            return Utils.filterFeatureTypes( featuresToFilter, featureTypeSet );
        }


    }

}
