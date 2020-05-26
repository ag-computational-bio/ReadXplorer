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
package de.cebitec.common.parser.data.embl;

import java.util.List;

/**
 * Representation of the OC tag.
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class OrganismClassification {

    private List<String> classification;

    public OrganismClassification() {
    }

    public OrganismClassification(List<String> classification) {
        this.classification = classification;
    }

    public List<String> getClassification() {
        return classification;
    }

    public void setClassification(List<String> classification) {
        this.classification = classification;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.classification != null ? this.classification.hashCode() : 0);
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
        final OrganismClassification other = (OrganismClassification) obj;
        if (this.classification != other.classification && (this.classification == null || !this.classification.equals(other.classification))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OrganismClassification{" + "classification=" + classification + '}';
    }
}
