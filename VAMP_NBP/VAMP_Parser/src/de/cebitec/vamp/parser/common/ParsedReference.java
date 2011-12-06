package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.parser.reference.Filter.FeatureFilter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedReference {

    private ArrayList<ParsedFeature> features;
    private String sequence;
    private String description;
    private String name;
    private FeatureFilter filter;
    private Timestamp timestamp;
    private int id;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ParsedReference(){
        features = new ArrayList<ParsedFeature>();
        filter = new FeatureFilter();
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

    public void addFeature(ParsedFeature f){
        // only add valid features according to specified filterrules
        if(filter.isValidFeature(f)){
            features.add(f);
        }
    }

    public List<ParsedFeature> getFeatures(){
        return features;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID(){
        return id;
    }

    /**
     * Adds subfeatures to their parent features. Should only be called after all
     * regular features have been imported. If a parent feature cannot cannot be found 
     * the subfeature becomes a regular feature. So they all should contain as much 
     * information as a regular feature, if possible.
     * @param subfeatures the subfeatures to add to their parent feature for this genome.
     */
    public void addSubfeatures(List<ParsedFeature> subfeatures) {
        int lastIndex = 0;
        boolean added = false;
        for (ParsedFeature subfeature : subfeatures){
            //since the features are sorted in this.features we can do this in linear time
            for (int i = lastIndex; i<this.features.size(); ++i){
                ParsedFeature feature = this.features.get(i);
                if (feature.getStrand() == subfeature.getStrand() && feature.getStart() <= subfeature.getStart()
                        && feature.getStop() >= subfeature.getStop()) {
                    feature.addSubfeature(new ParsedSubfeature(subfeature.getStart(), subfeature.getStop(), subfeature.getType()));
                    added = true;
                    lastIndex = i == 0 ? 0 : i-1;
                    break;
                }
            }
            if (!added){ //if there is no parent feature for the subfeature it becomes an ordinary feature
                this.features.add(subfeature);
            }
        }
    }

}
