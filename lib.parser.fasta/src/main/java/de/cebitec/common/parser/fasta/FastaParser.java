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

import de.cebitec.common.utilities.parser.AbstractBufferedReaderParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaParser extends AbstractBufferedReaderParser<FastaEntry> {

    private FastaParser(Reader br) {
        super(br);
    }

    private void clear(StringBuilder sb) {
        sb.delete(0, sb.length());
    }

    public static void checkFile(Path file) {
        if (file == null) {
            throw new FastaParserException("Fasta file must not be null.");
        }
        if (!Files.exists(file) || Files.isDirectory(file)) {
            throw new FastaParserException("Can't open fasta file " + file);
        }
    }

    public static FastaParser fileReader(Path file) throws IOException {
        checkFile(file);
        BufferedReader br = Files.newBufferedReader(file, Charset.forName("ASCII"));
        return reader(br);
    }

    public static FastaParser reader(Reader reader) {
        return new FastaParser(reader);
    }

    @Override
    public void parse(EntryHandler<FastaEntry> handler) throws IOException {
        if (handler == null) {
            throw new FastaParserException("EntryHandler must not be null.");
        }
        StringBuilder name = new StringBuilder();
        StringBuilder header = new StringBuilder();
        StringBuilder sequence = new StringBuilder();
        boolean first = true;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(">")) {
                if (first) {
                    first = false;
                } else {
                    handler.handle(new FastaEntry(header.toString(), name.toString(), sequence.toString()));
                    clear(name);
                    clear(header);
                    clear(sequence);
                }
                header.append(line);
                name.append(line.split("\\s")[0].substring(1));
            } else {
                sequence.append(line.trim().replaceAll("\\s", ""));
            }
        }
        handler.handle(new FastaEntry(header.toString(), name.toString(), sequence.toString()));
    }

    public Collection<String> parseIds() throws IOException {
        final List<String> list = new LinkedList<>();

        parse(new EntryHandler<FastaEntry>() {
            @Override
            public void handle(FastaEntry instance) {
                list.add(instance.getName());
            }
        });
        return list;
    }
}
