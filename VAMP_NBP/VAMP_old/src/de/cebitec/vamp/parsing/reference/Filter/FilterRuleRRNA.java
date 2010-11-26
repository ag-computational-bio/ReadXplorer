package de.cebitec.vamp.parsing.reference.Filter;

import de.cebitec.vamp.parsing.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleRRNA implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedFeature feature) {

        if(feature.getType() == FeatureType.R_RNA){
            return true;
        } else {
            return false;
        }
    }

}
