package de.cebitec.readXplorer.parser.reference.Filter;

import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.parser.common.ParsedFeature;

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
