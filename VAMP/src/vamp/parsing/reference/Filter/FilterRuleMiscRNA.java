package vamp.parsing.reference.Filter;

import vamp.parsing.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleMiscRNA implements FilterRuleI{

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.MISC_RNA){
            return true;
        } else {
            return false;
        }
    }

}
