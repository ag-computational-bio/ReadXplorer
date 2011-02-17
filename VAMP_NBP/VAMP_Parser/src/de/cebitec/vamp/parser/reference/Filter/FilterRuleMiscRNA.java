package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedFeature;

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
