package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.parser.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleTRNA implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.TRNA){
            return true;
        } else {
            return false;
        }
    }

}
