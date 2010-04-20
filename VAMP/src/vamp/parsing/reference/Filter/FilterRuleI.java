package vamp.parsing.reference.Filter;

import vamp.parsing.common.ParsedFeature;


/**
 *
 * @author ddoppmeier
 */
public interface FilterRuleI {

    public boolean appliesRule(ParsedFeature feature);

}
