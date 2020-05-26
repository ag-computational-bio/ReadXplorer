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

import de.cebitec.common.parser.data.embl.Sequence;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class SequenceParserTest {

    @Test
    public void testSequenceParser() {
        String text = "Sequence 95 BP; 33 A; 14 C; 12 G; 36 T; 0 other;\n"
            + "     aaacaaacca aatatggatt ttattgtagc catatttgct ctgtttgtta ttagctcatt        60\n"
            + "     aaacaaacca aatatggatt ttattgtagc catat                                   95";

        SequenceParser p = new SequenceParser();
        Sequence parse = p.parse(text);
        assertThat(parse.getSequence(),
                   equalTo("aaacaaaccaaatatggattttattgtagccatatttgctctgtttgttattagctcattaaacaaaccaaatatggattttattgtagccatat"));
        assertThat(p.write(parse), equalTo(text));

        String text2 = "Sequence 60 BP; 18 A; 9 C; 8 G; 25 T; 0 other;\n"
            + "     aaacaaacca aatatggatt ttattgtagc catatttgct ctgtttgtta ttagctcatt        60";
        assertThat(p.write(p.parse(text2)), equalTo(text2));
        String text3 = "Sequence 50 BP; 16 A; 7 C; 7 G; 20 T; 0 other;\n"
            + "     aaacaaacca aatatggatt ttattgtagc catatttgct ctgtttgtta                   50";
        assertThat(p.write(p.parse(text3)), equalTo(text3));
    }

}
