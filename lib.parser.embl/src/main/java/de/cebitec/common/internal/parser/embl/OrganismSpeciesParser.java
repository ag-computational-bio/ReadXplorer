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
package de.cebitec.common.internal.parser.embl;

import de.cebitec.common.internal.parser.common.Parser;
import de.cebitec.common.internal.parser.common.AbstractStringWriter;
import de.cebitec.common.parser.data.embl.OrganismSpecies;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class OrganismSpeciesParser extends AbstractStringWriter<OrganismSpecies> implements Parser<OrganismSpecies> {

    @Override
    public OrganismSpecies parse(CharSequence d) {
        String data = d.toString();
        OrganismSpecies os = new OrganismSpecies();
        if (data.contains("(") && data.contains(")")) {
            int start = data.indexOf("(");
            int end = data.indexOf(")");
            os.setGenusSpecies(data.substring(0, start).trim());
            os.setName(data.substring(start + 1, end).trim());
        } else {
            os.setGenusSpecies(data);
        }
        return os;
    }

    @Override
    public String write(OrganismSpecies data) {
        StringBuilder sb = new StringBuilder();
        sb.append(data.getGenusSpecies());
        if (data.getName() != null) {
            sb.append(" (").append(data.getName()).append(")");
        }
        return sb.toString();
    }

}
