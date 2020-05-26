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

package de.cebitec.common.parser.data.genbank;

import de.cebitec.common.parser.data.common.EntryHeader;
import de.cebitec.common.parser.data.common.MolecularType;
import de.cebitec.common.parser.data.common.TaxonomicDivision;
import de.cebitec.common.parser.data.common.Topology;

/**
 * Representation of the ID line in Genbank format.
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public class Locus extends EntryHeader {
    
    private String date;

    public Locus() {
    }
    
    public Locus(String date, String primaryAccession, Topology topology, MolecularType molecular_type, GenbankTaxonomicDivision taxonomy_division, Integer sequence_length) {
        super(primaryAccession, topology, molecular_type, taxonomy_division, sequence_length);
        this.date = date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public GenbankTaxonomicDivision getTaxonomy_division() {
        return (GenbankTaxonomicDivision) super.getTaxonomy_division();
    }
    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 67 * hash + (this.date != null ? this.date.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = super.equals(obj);
        if (isEqual) {
            final Locus other = (Locus) obj;
            if (!this.date.equals(other.date)) {
                isEqual = false;
            }
        }
        return isEqual;
    }
}
