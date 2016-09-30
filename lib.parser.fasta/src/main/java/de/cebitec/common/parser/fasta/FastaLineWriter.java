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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A fasta writer that acts like a builder for fasta files. It has two methods: writeHeader and appendSequence. Each
 * time the writeHeader method is called a new entry is added to the fasta file. Subsequent calls of appendSequence will
 * add the sequence string. In contrast to the FastaWriter this class does not need to have the whole sequence that
 * should be written in memory, but only the portions that should be written in one step.
 * <br/>
 * This class does also support formatting of the exported fasta sequence, that is trimming or appending single lines
 * such that each line has the given length.
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaLineWriter implements Closeable {

    /**
     * Determines the sequence output linewidth. If it is smaller than one, the whole sequence is written to one line.
     */
    private int lineWidth = 60;
    private final Writer writer;
    private int charcount = 0;
    private boolean lastWasNewline = true;
    /**
     * Determines whether the next entry will be the first entry. Is used to determine whether a newline should be
     * added.
     */
    private boolean firstEntry = true;

    public static FastaLineWriter fileWriter(Path p) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(p, Charset.forName("ASCII"));
        return writer(writer);
    }

    public static FastaLineWriter writer(Writer writer) {
        return new FastaLineWriter(writer);
    }

    private FastaLineWriter(Writer writer) {
        this.writer = writer;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Writes the fasta header. If the given string starts with a &gt; then the string is taken as it is. If it starts
     * with any other character a &gt; is prepended to the header string.
     *
     * @param header
     * @return This instance of the FastaLineWriter for a fluent builder interface.
     * @throws IOException
     */
    public FastaLineWriter writeHeader(String header) throws IOException {
        charcount = 0;
        if (!firstEntry) {
            writer.append("\n");
        } else {
            firstEntry = false;
        }
        if (!header.startsWith(">")) {
            writer.append(">");
        }
        writer.append(header);
        writer.append("\n");
        return this;
    }

    /**
     * Adds a part of the sequence to the fasta entry that was started with the writeHeader method. Subsequent calls to
     * this method will append the sequence to the given entry.
     *
     * @param sequence
     * @return This instance of the FastaLineWriter for a fluent builder interface.
     * @throws IOException
     */
    public FastaLineWriter appendSequence(String sequence) throws IOException {
        for (int i = 0; i < sequence.length(); i++) {
            char charAt = sequence.charAt(i);
            writer.append(charAt);
            lastWasNewline = false;
            charcount++;
            if (charcount % lineWidth == 0) {
                writer.append("\n");
                lastWasNewline = true;
            }
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        if (!lastWasNewline) {
            writer.append("\n");
        }
        writer.close();
    }
}
