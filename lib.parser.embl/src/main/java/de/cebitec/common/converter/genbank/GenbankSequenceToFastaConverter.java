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

import de.cebitec.common.converter.Converter;
import de.cebitec.common.parser.fasta.FastaLineWriter;
import de.cebitec.common.parser.data.genbank.Locus;
import de.cebitec.common.internal.parser.genbank.GenbankLocusParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public class GenbankSequenceToFastaConverter implements Converter {
    
    private final GenbankLocusParser locusParser = new GenbankLocusParser();
    private final FastaLineWriter fastaWriter;
    private final BufferedReader reader;

    public GenbankSequenceToFastaConverter(FastaLineWriter fastaWriter, BufferedReader reader) {
        this.fastaWriter = fastaWriter;
        this.reader = reader;
    }
    
    /**
     * Get a converter, which only parses the sequences contained in a Genbank
     * file line by line and removes all characters except the DNA. All
     * sequences are then written in a new multiple Fasta file.
     * @param emblPath Path to the Genbank file to convert into fasta
     * @param fastaPath Path to the fasta output file
     * @return the new converter object
     * @throws IOException
     */
    public static GenbankSequenceToFastaConverter fileConverter(Path emblPath, Path fastaPath) throws IOException {
        BufferedReader reader = Files.newBufferedReader(emblPath, Charset.forName("ASCII"));
        FastaLineWriter fileWriter = FastaLineWriter.fileWriter(fastaPath);
        return converter(reader, fileWriter);
    }
    
    /**
     * Get a converter, which only parses the sequences contained in a Genbank
     * file line by line and removes all characters except the DNA. All
     * sequences are then written in a new multiple Fasta file.
     * @param genbankReader Reader of a Genbank file to convert into fasta
     * @param fastaWriter Fasta writer to write the fasta output
     * @return the new converter object
     */
    public static GenbankSequenceToFastaConverter converter(Reader genbankReader, FastaLineWriter fastaWriter) {
        BufferedReader br;
        if (genbankReader instanceof BufferedReader) {
            BufferedReader bufferedReader = (BufferedReader) genbankReader;
            br = bufferedReader;
        } else {
            br = new BufferedReader(genbankReader);
        }
        return new GenbankSequenceToFastaConverter(fastaWriter, br);
    }
    
    
    /**
     * Run the conversion of a Genbank file into a fasta file. Only the sequences
     * contained in a Genbank file are parsed line by line and all characters
     * except the DNA are removed. All sequences are then written in a new
     * multiple Fasta file.
     *
     * @throws IOException
     */
    @Override
    public void convert() throws IOException {
        String line;
        boolean inSequenceBlock = false;
        while ((line = reader.readLine()) != null) {

            if (line.startsWith("LOCUS")) {
                Locus id = locusParser.parse(line.substring(12));
                fastaWriter.writeHeader(id.getPrimaryAccession());
            }
            if (line.startsWith("ORIGIN")) {
                inSequenceBlock = true;
            } else if (line.startsWith("//")) {
                inSequenceBlock = false;
            } else if (inSequenceBlock) {
                String seqline = line.replaceAll("\\s+", "").replaceAll("\\d+", "");
                fastaWriter.appendSequence(seqline);
            }

        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
        fastaWriter.close();
    }

    /**
     * @param laxParser Set <code>true</code> if the id line parser shall run in
     * lax parsing and writing mode, set <code>false</code> if the parser shall
     * run in validation mode and only accept correct entries.
     * <code>false</code> is the default value.
     */
    public void setLaxParser(boolean laxParser) {
        locusParser.setLaxParser(laxParser);
    }
}
