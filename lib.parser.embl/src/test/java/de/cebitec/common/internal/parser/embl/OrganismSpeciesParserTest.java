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
package de.cebitec.common.internal.parser.embl;

import de.cebitec.common.parser.data.embl.OrganismSpecies;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class OrganismSpeciesParserTest {

    @Test
    public void testOrganismSpeciesParser() {
        String text = "Genus species (name)";
        OrganismSpeciesParser p = new OrganismSpeciesParser();
        OrganismSpecies parse = p.parse(text);

        assertThat(parse.getGenusSpecies(), equalTo("Genus species"));
        assertThat(parse.getName(), equalTo("name"));
        assertThat(p.write(parse), equalTo(text));

        String text2 = "unidentified bacterium B8";
        OrganismSpecies parse1 = p.parse(text2);
        assertThat(parse1.getGenusSpecies(), equalTo(text2));
        assertThat(parse1.getName(), nullValue());
        assertThat(p.write(parse1), equalTo(text2));
    }

}
