/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.parser.reference.filter;


import de.cebitec.readxplorer.parser.common.ParsedFeature;
import java.util.ArrayList;


/**
 *
 * @author ddoppmeier
 */
public class FeatureFilter {

    // valid feature if one of the whitelist rules is applied
    private final ArrayList<FilterRuleI> whitelist;
    // valid features have to apply all blacklist rules
    // e.g. o FilterRuleCDS added to the blacklist means, that a CDS Type feature is not accepted
    private final ArrayList<FilterRuleI> blacklist;


    public FeatureFilter() {
        whitelist = new ArrayList<>();
        blacklist = new ArrayList<>();
    }


    public void addBlacklistRule( FilterRuleI rule ) {
        blacklist.add( rule );
    }


    public void addWhitelistRule( FilterRuleI rule ) {
        whitelist.add( rule );
    }


    public boolean isValidFeature( ParsedFeature feature ) {

        boolean whitelistAccepted = false;
        boolean blacklistAccepted = true;

        if( !whitelist.isEmpty() ) {
            // valid if one of the rules applies
            for( FilterRuleI rule : whitelist ) {
                if( rule.appliesRule( feature ) ) {
                    whitelistAccepted = true;
                    break;
                }
            }
        } else {
            // no rules in whitelist to apply: feature is accepted
            whitelistAccepted = true;
        }

        if( !blacklist.isEmpty() ) {
            // valid if no rule applies
            for( FilterRuleI rule : blacklist ) {
                if( rule.appliesRule( feature ) ) {
                    blacklistAccepted = false;
                    break;
                }
            }
        } else {
            // no rules in blacklist to apply: feature is accepted
            blacklistAccepted = true;
        }

        return blacklistAccepted && whitelistAccepted;

    }


}
