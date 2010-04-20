package vamp.parsing.reference.Filter;

import vamp.parsing.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleTRNA implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.T_RNA){
            return true;
        } else {
            return false;
        }
    }

}
