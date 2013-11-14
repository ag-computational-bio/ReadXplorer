package de.cebitec.readXplorer.parser.reference.Filter;

import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.parser.common.ParsedFeature;

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
