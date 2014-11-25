package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.parser.common.ParsedFeature;

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
