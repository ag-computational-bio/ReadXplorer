package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.parser.common.ParsedFeature;

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
