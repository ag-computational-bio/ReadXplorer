package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.parser.common.ParsedAnnotation;

/**
 *
 * @author ddoppmeier
 */
public class FilterRuleMiscRNA implements FilterRuleI{

    @Override
    public boolean appliesRule(ParsedAnnotation annotation) {
        if(annotation.getType() == FeatureType.MISC_RNA){
            return true;
        } else {
            return false;
        }
    }

}
