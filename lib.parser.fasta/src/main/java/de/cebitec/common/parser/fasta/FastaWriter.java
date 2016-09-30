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

import de.cebitec.common.utilities.parser.StreamingWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaWriter implements StreamingWriter<FastaEntry> {

    private final FastaLineWriter lineWriter;

    public static FastaWriter fileWriter(Path p) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(p, Charset.forName("ASCII"));
        return writer(writer);
    }

    public static FastaWriter writer(Writer writer) {
        return new FastaWriter(FastaLineWriter.writer(writer));
    }

    public FastaWriter(FastaLineWriter lineWriter) {
        this.lineWriter = lineWriter;
    }

    public void setLineWidth(int lineWidth) {
        lineWriter.setLineWidth(lineWidth);
    }

    public void writeEntry(String header, String sequence) throws IOException {
        lineWriter.writeHeader(header)
            .appendSequence(sequence);
    }

    @Override
    public void write(FastaEntry instance) throws IOException {
        writeEntry(instance.getHeader(), instance.getSequence());
    }

    @Override
    public void write(Iterable<FastaEntry> instance) throws IOException {
        for (FastaEntry t : instance) {
            write(t);
        }
    }

    @Override
    public void close() throws IOException {
        lineWriter.close();
    }
}
