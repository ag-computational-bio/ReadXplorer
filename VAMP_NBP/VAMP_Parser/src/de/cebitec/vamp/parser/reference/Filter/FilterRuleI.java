package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.parser.common.ParsedAnnotation;


/**
 *
 * @author ddoppmeier
 */
public interface FilterRuleI {

    public boolean appliesRule(ParsedAnnotation annotation);

}
