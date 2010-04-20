package vamp.parsing.reference.Filter;

import vamp.parsing.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleCDS implements FilterRuleI{

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.CDS){
            return true;
        } else {
            return false;
        }
    }

}
