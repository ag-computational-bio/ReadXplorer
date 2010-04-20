package vamp.parsing.reference.Filter;

import vamp.parsing.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
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
