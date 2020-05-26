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

import de.cebitec.common.parser.data.common.DataClass;
import de.cebitec.common.parser.data.common.EntryHeader;
import de.cebitec.common.parser.data.common.MolecularType;
import de.cebitec.common.parser.data.common.Topology;

/**
 * Representation of the ID line in EMBL format.
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public class Identification extends EntryHeader {
    
    private Integer sequenceVersion;
    private DataClass data_class;

    public Identification() {
    }
    
    public Identification(String primaryAccession, Integer sequenceVersion, Topology topology, MolecularType molecular_type, 
            DataClass data_class, EmblTaxonomicDivision taxonomy_division, Integer sequence_length) {
        super(primaryAccession, topology, molecular_type, taxonomy_division, sequence_length);
        this.sequenceVersion = sequenceVersion;
        this.data_class = data_class;
    }
    
    public Integer getSequenceVersion() {
        return sequenceVersion;
    }

    public void setSequenceVersion(Integer sequenceVersion) {
        this.sequenceVersion = sequenceVersion;
    }

    public DataClass getData_class() {
        return data_class;
    }

    public void setData_class(DataClass data_class) {
        this.data_class = data_class;
    }
    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 67 * hash + (this.sequenceVersion != null ? this.sequenceVersion.hashCode() : 0);
        hash = 67 * hash + (this.data_class!= null ? this.data_class.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = super.equals(obj);
        if (isEqual) {
            final Identification other = (Identification) obj;
            if (this.sequenceVersion != other.sequenceVersion && (this.sequenceVersion == null || !this.sequenceVersion.equals(other.sequenceVersion))) {
                isEqual = false;
            }
            if (this.data_class != other.data_class) {
                isEqual = false;
            }
        }
        return isEqual;
    }

}
