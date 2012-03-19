package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedAnnotation;

/**
 * @author ddoppmeier
 * 
 * This filter rule returns true for features of the sequence type FeatureType.SOURCE.
 */
public class FilterRuleSource implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedAnnotation annotation) {
        if(annotation.getType() == FeatureType.SOURCE){
            return true;
        } else {
            return false;
        }
    }

}
