package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.parser.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleRRNA implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.RRNA){
            return true;
        } else {
            return false;
        }
    }

}
