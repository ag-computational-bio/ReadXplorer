package de.cebitec.vamp.parsing.reference.Filter;

import de.cebitec.vamp.parsing.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleRepeatUnit implements FilterRuleI{

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.REPEAT_UNIT){
            return true;
        } else {
            return false;
        }
    }

}
