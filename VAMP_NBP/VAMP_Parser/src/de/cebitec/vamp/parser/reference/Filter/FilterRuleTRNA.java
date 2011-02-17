package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedFeature;

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
