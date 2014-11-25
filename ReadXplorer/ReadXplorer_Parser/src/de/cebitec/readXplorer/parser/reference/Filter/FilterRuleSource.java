package de.cebitec.readXplorer.parser.reference.Filter;

import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.parser.common.ParsedFeature;

/**
 * @author ddoppmeier
 * 
 * This filter rule returns true for features of the sequence type FeatureType.SOURCE.
 */
public class FilterRuleSource implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.SOURCE){
            return true;
        } else {
            return false;
        }
    }

}
