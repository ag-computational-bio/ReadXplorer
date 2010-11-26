package de.cebitec.vamp.parsing.common;

import de.cebitec.vamp.parsing.reference.Filter.FeatureFilter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public class ParsedReference {

    private ArrayList<ParsedFeature> features;
    private String sequence;
    private String description;
    private String name;
    private FeatureFilter filter;
    private Timestamp timestamp;
    private long id;

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

    public void setID(long id) {
        this.id = id;
    }

    public long getID(){
        return id;
    }

}
