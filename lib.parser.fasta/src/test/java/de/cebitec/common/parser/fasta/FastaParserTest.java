/*
 * Copyright (C) 2013 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
package de.cebitec.common.parser.fasta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaParserTest {

    public FastaParserTest() {
    }

    /**
     * Test of parse method, of class InefficientFastaParser.
     */
    @Test
    public void testParse() throws IOException {
        String text = ">test some other text\n"
                + "atagc\n"
                + "atata\n"
                + "\n"
                + ">test2 some other text\n"
                + "atagc\n"
                + "agggta\n";
        BufferedReader br = new BufferedReader(new StringReader(text));

        FastaParser reader = FastaParser.reader(br);
        
        Iterable<FastaEntry> entries = reader.parseAll();
        if (entries instanceof List) {
            List<FastaEntry> list = (List<FastaEntry>) entries;
            assertThat(list.size(), equalTo(2));
            assertThat(list.get(0).getName(), equalTo("test"));
            assertThat(list.get(0).getSequence(), equalTo("atagcatata"));
            assertThat(list.get(1).getName(), equalTo("test2"));
            assertThat(list.get(1).getSequence(), equalTo("atagcagggta"));
        }
    }
    
    @Test(expected = FastaParserException.class)
    public void testParseNullFile() throws IOException {
        Path p = null;
        FastaParser.fileReader(p);
    }
}