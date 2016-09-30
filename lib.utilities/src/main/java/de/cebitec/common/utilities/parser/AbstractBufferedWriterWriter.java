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
package de.cebitec.common.utilities.parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * An abstract StreamingWriter implementation that uses a BufferedWriter for writing to a stream.
 *
 * @param <T> The type the writer consumes.
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public abstract class AbstractBufferedWriterWriter<T> implements StreamingWriter<T> {

    protected final BufferedWriter writer;

    public AbstractBufferedWriterWriter(Writer reader) {
        if (reader instanceof BufferedWriter) {
            BufferedWriter bufferedWriter = (BufferedWriter) reader;
            this.writer = bufferedWriter;
        } else {
            this.writer = new BufferedWriter(reader);
        }
    }

    @Override
    public void write(Iterable<T> instance) throws IOException {
        for (T t : instance) {
            write(t);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
