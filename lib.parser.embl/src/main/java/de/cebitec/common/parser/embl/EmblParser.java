/*
 * Copyright (C) 2012 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
package de.cebitec.common.parser.embl;

import de.cebitec.common.parser.data.embl.EmblEntry;
import de.cebitec.common.internal.parser.embl.EmblEntryParser;
import de.cebitec.common.utilities.parser.AbstractBufferedReaderParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class EmblParser extends AbstractBufferedReaderParser<EmblEntry> {

    private EmblEntryParser eep = new EmblEntryParser();

    private EmblParser(Reader br) {
        super(br);
    }

    public static EmblParser fileReader(Path p) throws IOException {
        BufferedReader reader = Files.newBufferedReader(p, Charset.forName("ASCII"));
        return reader(reader);
    }

    public static EmblParser reader(Reader reader) {
        BufferedReader br;
        if (reader instanceof BufferedReader) {
            BufferedReader bufferedReader = (BufferedReader) reader;
            br = bufferedReader;
        } else {
            br = new BufferedReader(reader);
        }
        return new EmblParser(br);
    }

    @Override
    public void parse(EntryHandler<EmblEntry> eh) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
            if ("//".equals(line.trim())) {
                EmblEntry parse = eep.parse(sb);
                eh.handle(parse);
                sb.delete(0, sb.length());
            }
        }
    }
}
