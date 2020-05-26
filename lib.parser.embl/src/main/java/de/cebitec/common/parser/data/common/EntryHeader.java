/*
 * Copyright (C) 2012 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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

import de.cebitec.common.parser.data.common.TaxonomicDivision;

/**
 * Representation of the ID line.
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class EntryHeader {

    public EntryHeader() {
    }

    public EntryHeader(String primaryAccession, Topology topology, MolecularType molecular_type, TaxonomicDivision taxonomy_division, Integer sequence_length) {
        this.primaryAccession = primaryAccession;
        this.topology = topology;
        this.molecular_type = molecular_type;
        this.taxonomy_division = taxonomy_division;
        this.sequence_length = sequence_length;
    }

    private String primaryAccession;
    private Topology topology;
    private MolecularType molecular_type;
    private TaxonomicDivision taxonomy_division;
    private Integer sequence_length;

    public String getPrimaryAccession() {
        return primaryAccession;
    }

    public void setPrimaryAccession(String primaryAccession) {
        this.primaryAccession = primaryAccession;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    public MolecularType getMolecular_type() {
        return molecular_type;
    }

    public void setMolecular_type(MolecularType molecular_type) {
        this.molecular_type = molecular_type;
    }

    public TaxonomicDivision getTaxonomy_division() {
        return taxonomy_division;
    }

    public void setTaxonomy_division(TaxonomicDivision taxonomy_division) {
        this.taxonomy_division = taxonomy_division;
    }

    public Integer getSequence_length() {
        return sequence_length;
    }

    public void setSequence_length(Integer sequence_length) {
        this.sequence_length = sequence_length;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.primaryAccession != null ? this.primaryAccession.hashCode() : 0);
        hash = 67 * hash + (this.topology != null ? this.topology.hashCode() : 0);
        hash = 67 * hash + (this.molecular_type != null ? this.molecular_type.hashCode() : 0);
        hash = 67 * hash + (this.taxonomy_division != null ? this.taxonomy_division.hashCode() : 0);
        hash = 67 * hash + (this.sequence_length != null ? this.sequence_length.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntryHeader other = (EntryHeader) obj;
        if ((this.primaryAccession == null) ? (other.primaryAccession != null) : !this.primaryAccession.equals(other.primaryAccession)) {
            return false;
        }
        if (this.topology != other.topology) {
            return false;
        }
        if (this.molecular_type != other.molecular_type) {
            return false;
        }
        if (this.taxonomy_division != other.taxonomy_division) {
            return false;
        }
        if (this.sequence_length != other.sequence_length && (this.sequence_length == null || !this.sequence_length.equals(other.sequence_length))) {
            return false;
        }
        return true;
    }

}
