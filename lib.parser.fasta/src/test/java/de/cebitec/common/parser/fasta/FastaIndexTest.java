/*
 * Copyright (C) 2014 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaIndexTest {

    FastaIndexer indexer = new FastaIndexer();

    @Test
    public void testIndexValidFastaFileUnix() throws URISyntaxException, IOException {
        Path fasta = Paths.get(getClass().getResource("fastaWithMultiLineEntries.fas").toURI());
        Path expectedIndex = Paths.get(getClass().getResource("fastaWithMultiLineEntries.fas.fai").toURI());
        List<FastaIndexEntry> createIndex = indexer.createIndex(fasta);
        List<FastaIndexEntry> expected = new FastaIndexReader().read(expectedIndex);
        assertThat(createIndex, equalTo(expected));
    }

    @Test
    public void testIndexValidFastaFileWindows() throws URISyntaxException, IOException {
        Path fasta = Paths.get(getClass().getResource("fastaWithMultiLineEntriesWindows.fas").toURI());
        Path expectedIndex = Paths.get(getClass().getResource("fastaWithMultiLineEntriesWindows.fas.fai").toURI());
        List<FastaIndexEntry> createIndex = indexer.createIndex(fasta);
        List<FastaIndexEntry> expected = new FastaIndexReader().read(expectedIndex);
        assertThat(createIndex, equalTo(expected));
    }

    @Test
    public void testIndexValidSinglelineFastaFileWindows() throws URISyntaxException, IOException {
        Path fasta = Paths.get(getClass().getResource("fastaWithSingleLineEntriesWindows.fas").toURI());
        Path expectedIndex = Paths.get(getClass().getResource("fastaWithSingleLineEntriesWindows.fas.fai").toURI());
        List<FastaIndexEntry> createIndex = indexer.createIndex(fasta);
        List<FastaIndexEntry> expected = new FastaIndexReader().read(expectedIndex);
        assertThat(createIndex, equalTo(expected));
    }
    
    @Test
    public void testIndexValidSinglelineFastaFileUnix() throws URISyntaxException, IOException {
        Path fasta = Paths.get(getClass().getResource("fastaWithSingleLineEntries.fas").toURI());
        Path expectedIndex = Paths.get(getClass().getResource("fastaWithSingleLineEntries.fas.fai").toURI());
        List<FastaIndexEntry> createIndex = indexer.createIndex(fasta);
        List<FastaIndexEntry> expected = new FastaIndexReader().read(expectedIndex);
        assertThat(createIndex, equalTo(expected));
    }
    
    @Test
    public void testIndexValidFastaWithTrailingNewlines() throws URISyntaxException, IOException {
        Path fasta = Paths.get(getClass().getResource("fastaWithTrailingEmptyLines.fas").toURI());
        Path expectedIndex = Paths.get(getClass().getResource("fastaWithTrailingEmptyLines.fas.fai").toURI());
        List<FastaIndexEntry> createIndex = indexer.createIndex(fasta);
        List<FastaIndexEntry> expected = new FastaIndexReader().read(expectedIndex);
        assertThat(createIndex, equalTo(expected));
    }

    @Test(expected = IllegalStateException.class)
    public void testIndexFastaWithVariableLengths() throws IOException, URISyntaxException {
        Path resource = Paths.get(getClass().getResource("fastaWithMultiLineEntriesWithDifferentLengths.fas").toURI());
        indexer.createIndex(resource);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testIndexFastaWithEmptyLineInSequence() throws IOException, URISyntaxException {
        Path resource = Paths.get(getClass().getResource("fastaWithEmptyLineInSequence.fas").toURI());
        indexer.createIndex(resource);
    }
    
}
