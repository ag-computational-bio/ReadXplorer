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

package de.cebitec.common.converter.genbank;

import com.google.common.collect.Iterables;
import de.cebitec.common.parser.fasta.FastaEntry;
import de.cebitec.common.parser.fasta.FastaLineWriter;
import de.cebitec.common.parser.fasta.FastaParser;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public class GenbankSequenceToFastaConverterTest {
    
    public GenbankSequenceToFastaConverterTest() {
    }

    /**
     * Test of convert method, of class GenbankSequenceToFastaConverter.
     */
    @Test
    public void testConvert() throws Exception {
        URL resource = getClass().getResource("testMultipleEntriesGenbank.gbk");
        URL expResource = getClass().getResource("expectedMultipleGenbankFasta.fa");
        Path get = Paths.get(resource.toURI());
        Path expectedPath = Paths.get(expResource.toURI());

        StringWriter sw = new StringWriter();
        GenbankSequenceToFastaConverter converter = GenbankSequenceToFastaConverter.converter(Files.newBufferedReader(get, Charset.forName("UTF-8")), FastaLineWriter.writer(sw));
        converter.convert();
        sw.close();

        Iterable<FastaEntry> parseAll = FastaParser.reader(new StringReader(sw.toString())).parseAll();
        Iterable<FastaEntry> expectedParseAll = FastaParser.fileReader(expectedPath).parseAll();

        assertThat(Iterables.get(parseAll, 0).getSequence(), equalTo(Iterables.get(expectedParseAll, 0).getSequence()));
        assertThat(parseAll, equalTo(expectedParseAll));
        
        URL resourceDamaged = getClass().getResource("testGenbankDamagedHeader.gbk");
        URL expResource2 = getClass().getResource("expectedMultipleGenbankFasta2.fa");
        Path path = Paths.get(resourceDamaged.toURI());
        Path expectedPath2 = Paths.get(expResource2.toURI());

        StringWriter swDamaged = new StringWriter();
        GenbankSequenceToFastaConverter converter2 = GenbankSequenceToFastaConverter.converter(Files.newBufferedReader(path, Charset.forName("UTF-8")), FastaLineWriter.writer(swDamaged));
        converter2.setLaxParser(true);
        converter2.convert();
        swDamaged.close();

        Iterable<FastaEntry> parseAll2 = FastaParser.reader(new StringReader(swDamaged.toString())).parseAll();
        Iterable<FastaEntry> expectedParseAll2 = FastaParser.fileReader(expectedPath2).parseAll();

        assertThat(Iterables.get(parseAll2, 0).getSequence(), equalTo(Iterables.get(expectedParseAll2, 0).getSequence()));
        assertThat(parseAll2, equalTo(expectedParseAll2));

    }
    
}
