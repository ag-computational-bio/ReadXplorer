package de.cebitec.readXplorer.parser.reference.Filter;

import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.parser.common.ParsedFeature;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleCDS implements FilterRuleI{

    @Override
    public boolean appliesRule(ParsedFeature feature) {
        if(feature.getType() == FeatureType.CDS){
            return true;
        } else {
            return false;
        }
    }

}
