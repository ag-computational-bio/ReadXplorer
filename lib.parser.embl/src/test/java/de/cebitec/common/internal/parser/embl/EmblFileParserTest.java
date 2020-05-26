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

import de.cebitec.common.parser.data.embl.EmblEntry;
import de.cebitec.common.parser.embl.EmblParser;
import de.cebitec.common.parser.embl.EmblWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class EmblFileParserTest {

    @Test
    public void testEmblFileParser() throws IOException, URISyntaxException, Exception {
        URL resource = getClass().getResource("test.embl");
        Path get = Paths.get(resource.toURI());
        String text = new String(Files.readAllBytes(get));
        EmblParser p = EmblParser.fileReader(get);
        Iterable<EmblEntry> parse = p.parseAll();
        p.close();

        StringWriter sw = new StringWriter(text.length());
        EmblWriter ew = EmblWriter.writer(sw);

        if (parse instanceof List) {
            List<EmblEntry> list = (List<EmblEntry>) parse;
            assertThat(list, hasSize(1));
            ew.write(list);
            ew.close();
            assertThat(fixNewLines(sw.toString()), equalTo(fixNewLines(text)));
        }
    }
    
    /**
     * Convert all \r\n to \n
     * @param s
     * @return 
     */
    private String fixNewLines(String s) {
        return s.replaceAll("\r\n", "\n");
    }
}
