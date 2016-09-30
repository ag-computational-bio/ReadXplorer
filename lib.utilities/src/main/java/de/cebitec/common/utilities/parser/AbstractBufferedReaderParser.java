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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * An abstract StreamingParser implementation that uses a BufferedReader for reading a stream.
 *
 * @param <T> The type the parser generates.
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public abstract class AbstractBufferedReaderParser<T> implements StreamingParser<T> {

    protected final BufferedReader reader;

    public AbstractBufferedReaderParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            BufferedReader bufferedReader = (BufferedReader) reader;
            this.reader = bufferedReader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    @Override
    public Iterable<T> parseAll() throws IOException {
        CollectEntryHandler<T> handler = new CollectEntryHandler<>();
        parse(handler);
        return handler.getList();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
