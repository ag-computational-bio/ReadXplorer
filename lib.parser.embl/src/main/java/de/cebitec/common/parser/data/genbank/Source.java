/*
 * Copyright (C) 2015 Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
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

import de.cebitec.common.parser.data.embl.OrganismClassification;
import de.cebitec.common.parser.data.embl.OrganismSpecies;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class Source {

    private OrganismSpecies species;
    private OrganismClassification classification;

    public OrganismSpecies getSpecies() {
        return species;
    }

    public void setSpecies(OrganismSpecies species) {
        this.species = species;
    }

    public OrganismClassification getClassification() {
        return classification;
    }

    public void setClassification(OrganismClassification classification) {
        this.classification = classification;
    }
    
}
