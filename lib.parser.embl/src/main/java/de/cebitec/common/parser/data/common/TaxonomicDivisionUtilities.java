/*
 * Copyright (C) 2014 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.common.parser.data.common;

/**
 * Utility class for Taxonomic Divisions.
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public class TaxonomicDivisionUtilities {
        /**
     * @param <T> an implementation of TaxonomicDivision
     * @param values iterable of taxonomic divisions from which the code shall 
     * be retrieved.
     * @param code The three letter code for the desired taxonomic division
     * @return The desired taxonomic division or null, if it does not exist
     */
    public static <T extends TaxonomicDivision> T getByCode(Iterable<? extends T> values, String code) {
        for (T td : values) {
            if (code.equals(td.getCode())) {
                return td;
            }
        }
        return null;
    }
}
