package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedAnnotation;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleRepeatUnit implements FilterRuleI{

    @Override
    public boolean appliesRule(ParsedAnnotation annotation) {
        if(annotation.getType() == FeatureType.REPEAT_UNIT){
            return true;
        } else {
            return false;
        }
    }

}
