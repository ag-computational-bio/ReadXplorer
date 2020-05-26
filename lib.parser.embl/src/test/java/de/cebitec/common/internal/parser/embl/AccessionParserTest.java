/*
 * Copyright (C) 2015 Lukas Jelonek <Lukas.Jelonek at computational.bio.uni-giessen.de>
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

import de.cebitec.common.parser.data.embl.Accession;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class AccessionParserTest {

    @Test
    public void testAccessionParser() {
        String accs = "Y00001; X00001-X00005; X00008; Z00001-Z00005;";
        AccessionParser p = new AccessionParser();
        Accession parse = p.parse(accs);

        assertThat(parse.getNumbers(), contains("Y00001", "X00001-X00005", "X00008", "Z00001-Z00005"));
        assertThat(p.write(parse), equalTo(accs));
        String longaccs = "Y00001; X00001-X00005; X00008; Z00001-Z00005; Y00001; X00001-X00005; X00008; Z00001-Z00005;";
        String expected = "Y00001; X00001-X00005; X00008; Z00001-Z00005; Y00001; X00001-X00005;\n"
            + "X00008; Z00001-Z00005;";

        assertThat(p.write(p.parse(longaccs)), equalTo(expected));
    }
}
