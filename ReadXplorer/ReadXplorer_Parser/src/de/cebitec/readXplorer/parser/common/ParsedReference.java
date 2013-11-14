package de.cebitec.readXplorer.parser.common;

import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data holder for a parsed reference sequence. Besides that it knows whether
 * the reference features hierarchy is based on the subfeature concept or the 
 * parent id concept and offers a method to transform subfeatures to parent ids.
 * By default the subfeature concept is assumed.
 * 
 * @author ddoppmeier, rhilker
 */
public class ParsedReference {
    
    public static String FINISHED = "ParsingFinished";

    private List<ParsedFeature> features;
    private List<ParsedFeature> finalFeatures;
    private Map<String, Integer> featureIdMap;
    private String sequence;
    private String description;
    private String name;
    private FeatureFilter filter;
    private Timestamp timestamp;
    private int id;
    private int featId;
    private boolean hasSubFeatures;

    /**
     * Data holder for a parsed reference sequence. Besides that it knows
     * whether the reference features hierarchy is based on the subfeature
     * concept or the parent id concept and offers a method to transform
     * subfeatures to parent ids. By default the subfeature concept is assumed.
     * @author ddoppmeier, rhilker
     */
    public ParsedReference() {
        features = new ArrayList<>();
        finalFeatures = new ArrayList<>();
        featureIdMap = new HashMap<>();
        filter = new FeatureFilter();
        hasSubFeatures = true;
    }

    /**
     * Sets the unique id of this feature, which will be used in the db.
     * @param id The unique id of this feature, which will be used in the db.
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * @return The unique id of this feature, which will be used in the db.
     */
    public int getID() {
        return id;
    }
    
    /**
     * @return The timestamp at which this reference was created.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * The timestamp at which this reference was created.
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setFeatureFilter(FeatureFilter filter){
        this.filter = filter;
    }

    public FeatureFilter getFeatureFilter(){
        return filter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int getLength(){
        return sequence.length();
    }

    /**
     * Adds a feature to this reference either in the root level feature list
     * or the sub feature list (features, which have a parent id).
     * @param feature The feature to add
     */
    public void addFeature(ParsedFeature feature){
        // only add valid features according to specified filterrules
        if (filter.isValidFeature(feature)) {
            features.add(feature);
        }
    }

    /**
     * Adds all features in the <tt>featureList</tt> to the list of features of
     * this reference.
     * @param featureList The list of features to add to the list of features of
     * this reference
     */
    public void addAllFeatures(List<ParsedFeature> featureList) {
        for (ParsedFeature feature : featureList) {
            if (filter.isValidFeature(feature)) {
                features.add(feature);
            }
        }
    }

    /**
     * @return The list of root level features.
     */
    public List<ParsedFeature> getFeatures(){
        return features;
    }
    
    /**
     * @return The list of subfeatures (features, which have a parent id).
     */
    public List<ParsedFeature> getSubFeatures() {
        return finalFeatures;
    }

    /**
     * @param featId The latest feature id used in the db. Set it before 
     * distributing feature ids with {@link distributeFeatureIds}.
     */
    public void setFeatId(int featId) {
        this.featId = featId;
    }
    
    /**
     * Set the unique feature ids used in the db for each feature. Also stores a
     * mapping between feature identifier (name) and id, if the feature has an
     * identifier. Further replaces the parent names in the feature by the newly
     * assigned parent ids.
     */
    public void distributeFeatureIds() {
        this.distributeFeatureIds(features); //this way only the feature ids of this reference features can be distributed!
        
        if (hasSubFeatures) {
            this.mutateSubFeatureToParentIds(features);
            this.features = finalFeatures;
            finalFeatures = new ArrayList<>();
        } else {
            this.replaceParentNamesByIds();
        }
    }

    /**
     * Set the unique feature ids used in the db for each feature. Also stores
     * a mapping between feature identifier (name) and id, if the feature has an
     * identifier. Further replaces the parent names in the feature by the
     * newly assigned parent ids.
     * @param id The unique feature id to start with (will be increased by one
     * for each feature in the reference
     * @param featureList list of features for which the ids need to be distributed 
     */
    private void distributeFeatureIds(List<ParsedFeature> featureList) {
    
        for (ParsedFeature feature : featureList) {
            feature.setId(featId);
            if (feature.hasIdentifier()) {
                featureIdMap.put(feature.getIdentifier(), featId);
            }
            ++featId;
            this.distributeFeatureIds(feature.getSubFeatures());
        }
    }

    /**
     * Visits all features in the given list and sets their parentids from the
     * subfeature list contained within each feature. Also all features are
     * added to the finalFeatures list to guarantee the readiness for storing
     * the features in the db.
     * @param featureList The list of features whose parents have to be set
     */
    private void mutateSubFeatureToParentIds(List<ParsedFeature> featureList) {
        List<String> parentIds;
        for (ParsedFeature feature : featureList) {
            parentIds = new ArrayList<>();
            parentIds.add(String.valueOf(feature.getId()));
            finalFeatures.add(feature);
            for (ParsedFeature subFeature : feature.getSubFeatures()) {
                subFeature.setParentIds(parentIds);
                finalFeatures.add(subFeature);
                this.mutateSubFeatureToParentIds(subFeature.getSubFeatures());
            }
        }
    }
    
    /**
     * Set the unique feature ids used in the db for each feature. Also stores a
     * mapping between feature identifier (name) and id, if the feature has an
     * identifier. Further replaces the parent names in the feature by the newly
     * assigned parent ids.
     * @param id The unique feature id to start with (will be increased by one
     * for each feature in the reference
     * @param featureList list of features for which the ids need to be
     * distributed
     */
    private void replaceParentNamesByIds() {
        //replace parent names by parent ids
        List<String> newParentIds;
        for (ParsedFeature feature : this.features) {
            newParentIds = new ArrayList<>();
            for (String parentName : feature.getParentIds()) {
                if (featureIdMap.containsKey(parentName)) {
                    newParentIds.add(String.valueOf(featureIdMap.get(parentName)));
                }
            }
            feature.setParentIds(newParentIds);
        }
    }

    /**
     * @return <tt>true</tt>, if it has subfeatures, <tt>false</tt>, if it
     * already relies on parent ids.
     */
    public boolean hasSubFeatures() {
        return hasSubFeatures;
    }

    /**
     * Set if the hierarchy of the featueres is based on subfeatures or parent 
     * ids.
     * @param hasSubFeatures True, if it has subfeatures, false, if it already 
     * relies on parent ids.
     */
    public void setHasSubFeatures(boolean hasSubFeatures) {
        this.hasSubFeatures = hasSubFeatures;
    }
    
    

}
