package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedAnnotation;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleTRNA implements FilterRuleI {

    @Override
    public boolean appliesRule(ParsedAnnotation annotation) {
        if(annotation.getType() == FeatureType.TRNA){
            return true;
        } else {
            return false;
        }
    }

}
