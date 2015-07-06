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


/**
 * A filter rule.
 *
 * @author ddoppmeier
 */
public interface FilterRuleI {

    /**
     * Applies this rule to the given feature.
     * @param feature The feature to which to apply the rule
     * @return true, if the rule applies, false otherwise
     */
    boolean appliesRule( ParsedFeature feature );


}
