package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedAnnotation;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleRRNA implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedAnnotation annotation) {
        if(annotation.getType() == FeatureType.RRNA){
            return true;
        } else {
            return false;
        }
    }

}
