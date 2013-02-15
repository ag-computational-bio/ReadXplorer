package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.parser.common.ParsedFeature;


/**
 *
 * @author ddoppmeier
 */
public interface FilterRuleI {

    public boolean appliesRule(ParsedFeature feature);

}
