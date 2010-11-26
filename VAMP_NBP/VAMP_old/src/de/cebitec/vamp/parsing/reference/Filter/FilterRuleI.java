package de.cebitec.vamp.parsing.reference.Filter;

import de.cebitec.vamp.parsing.common.ParsedFeature;


/**
 *
 * @author ddoppmeier
 */
public interface FilterRuleI {

    public boolean appliesRule(ParsedFeature feature);

}
