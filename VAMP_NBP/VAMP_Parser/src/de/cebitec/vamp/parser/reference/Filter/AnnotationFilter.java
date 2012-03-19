package de.cebitec.vamp.parser.reference.Filter;

import de.cebitec.vamp.parser.common.ParsedAnnotation;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public class AnnotationFilter {

    // valid annotation if one of the whitelist rules is applied
    private ArrayList<FilterRuleI> whitelist;
    // valid annotations have to apply all blacklist rules
    // e.g. o FilterRuleCDS added to the blacklist means, that a CDS Type annotation is not accepted
    private ArrayList<FilterRuleI> blacklist;

    public AnnotationFilter(){
        whitelist = new ArrayList<FilterRuleI>();
        blacklist = new ArrayList<FilterRuleI>();
    }

    public void addBlacklistRule(FilterRuleI rule){
        blacklist.add(rule);
    }

    public void addWhitelistRule(FilterRuleI rule){
        whitelist.add(rule);
    }

    public boolean isValidAnnotation(ParsedAnnotation annotation){

        boolean whitelistAccepted = false;
        boolean blacklistAccepted = true;

        if(!whitelist.isEmpty()){
            // valid if one of the rules applies
            Iterator<FilterRuleI> it = whitelist.iterator();
            while(it.hasNext()){
                FilterRuleI rule = it.next();
                if(rule.appliesRule(annotation)){
                    whitelistAccepted = true;
                    break;
                }
            }
        } else {
            // no rules in whitelist to apply: annotation is accepted
            whitelistAccepted = true;
        }

        if(!blacklist.isEmpty()){
            // valid if no rule applies
            Iterator<FilterRuleI> it = blacklist.iterator();
            while(it.hasNext()){
                FilterRuleI rule = it.next();
                if(rule.appliesRule(annotation)){
                    blacklistAccepted = false;
                    break;
                }
            }
        } else {
            // no rules in blacklist to apply: annotation is accepted
            blacklistAccepted = true;
        }

        if(blacklistAccepted && whitelistAccepted){
            return true;
        } else {
            return false;
        }

    }

}
