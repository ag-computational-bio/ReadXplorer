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

package de.cebitec.common.parser.data.embl;

import de.cebitec.common.parser.data.common.TaxonomicDivision;

/**
 * Taxonomic division for EMBL format.
 * 
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public enum EmblTaxonomicDivision implements TaxonomicDivision {

    Bacteriophage("PHG"),
    Environmental("ENV"),
    Fungal("FUN"),
    Human("HUM"),
    Invertebrate("INV"),
    Mus_musculus("MUS"),
    Plant("PLN"),
    Prokaryote("PRO"),
    Other_Mammal("MAM"),
    Other_Rodent("ROD"),
    Other_Vertebrate("VRT"),
    Synthetic("SYN"),
    Transgenic("TGN"),
    Unclassified("UNC"),
    Viral("VRL");

    private final String code;

    private EmblTaxonomicDivision(String code) {
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.toString();
    }

}
