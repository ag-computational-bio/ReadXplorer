package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.parser.common.ParsedFeature;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public class FeatureFilter {

    // valid feature if one of the whitelist rules is applied
    private ArrayList<FilterRuleI> whitelist;
    // valid features have to apply all blacklist rules
    // e.g. o FilterRuleCDS added to the blacklist means, that a CDS Type feature is not accepted
    private ArrayList<FilterRuleI> blacklist;

    public FeatureFilter() {
        whitelist = new ArrayList<>();
        blacklist = new ArrayList<>();
    }

    public void addBlacklistRule(FilterRuleI rule) {
        blacklist.add(rule);
    }

    public void addWhitelistRule(FilterRuleI rule) {
        whitelist.add(rule);
    }

    public boolean isValidFeature(ParsedFeature feature) {

        boolean whitelistAccepted = false;
        boolean blacklistAccepted = true;

        if (!whitelist.isEmpty()) {
            // valid if one of the rules applies
            Iterator<FilterRuleI> it = whitelist.iterator();
            while (it.hasNext()) {
                FilterRuleI rule = it.next();
                if (rule.appliesRule(feature)) {
                    whitelistAccepted = true;
                    break;
                }
            }
        } else {
            // no rules in whitelist to apply: feature is accepted
            whitelistAccepted = true;
        }

        if (!blacklist.isEmpty()) {
            // valid if no rule applies
            Iterator<FilterRuleI> it = blacklist.iterator();
            while (it.hasNext()) {
                FilterRuleI rule = it.next();
                if (rule.appliesRule(feature)) {
                    blacklistAccepted = false;
                    break;
                }
            }
        } else {
            // no rules in blacklist to apply: feature is accepted
            blacklistAccepted = true;
        }

        if (blacklistAccepted && whitelistAccepted) {
            return true;
        } else {
            return false;
        }

    }

}
