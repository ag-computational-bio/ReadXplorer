package de.cebitec.readXplorer.parser.reference.Filter;

import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.parser.common.ParsedFeature;

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
