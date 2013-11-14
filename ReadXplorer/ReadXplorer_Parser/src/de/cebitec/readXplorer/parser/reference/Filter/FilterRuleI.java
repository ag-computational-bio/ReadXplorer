package de.cebitec.readXplorer.parser.reference.Filter;

import de.cebitec.readXplorer.parser.common.ParsedFeature;


/**
 *
 * @author ddoppmeier
 */
public interface FilterRuleI {

    public boolean appliesRule(ParsedFeature feature);

}
