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
package de.cebitec.common.parser.embl;

import de.cebitec.common.parser.data.embl.EmblEntry;
import de.cebitec.common.internal.parser.embl.EmblEntryParser;
import de.cebitec.common.utilities.parser.AbstractBufferedWriterWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class EmblWriter extends AbstractBufferedWriterWriter<EmblEntry> {

    private EmblEntryParser eep = new EmblEntryParser();
    boolean first = true;
    
    public static EmblWriter fileWriter(Path p) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(p, Charset.forName("ASCII"));
        return writer(writer);
    }

    public static EmblWriter writer(java.io.Writer writer) {
        BufferedWriter bw;
        if (writer instanceof BufferedWriter) {
            bw = (BufferedWriter) writer;
        } else {
            bw = new BufferedWriter(writer);
        }
        return new EmblWriter(bw);
    }
    
    private EmblWriter(java.io.Writer bw) {
        super(bw);
    }
    
    @Override
    public void write(EmblEntry t) throws IOException {
        String write = eep.write(t);
        if (!first) {
            writer.write("\n");
        } else {
            first = false;
        }
        writer.write(write);
    }   
}
