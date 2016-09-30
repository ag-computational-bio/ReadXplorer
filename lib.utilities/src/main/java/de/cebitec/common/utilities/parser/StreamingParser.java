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

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A streaming parser parses a data store e.g. a file and passes the parsed objects to an EntryHandler that then may
 * process the entry.
 *
 * @param <T> The type the parser generates.
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public interface StreamingParser<T> extends Closeable {

    /**
     * An EntryHandler is used by the StreamingParser and has the purpose to process the parsed entry.
     *
     * @param <T> The type the handler handles.
     */
    public static interface EntryHandler<T> {

        void handle(T instance);
    }

    /**
     * An entry handler that collects all entries in a list.
     *
     * @param <T>
     */
    public static class CollectEntryHandler<T> implements EntryHandler<T> {

        private List<T> list = new LinkedList<>();

        @Override
        public void handle(T instance) {
            list.add(instance);
        }

        public List<T> getList() {
            return list;
        }
    }

    /**
     * Parses the data store and sends each parsed entry to the entryhandler.
     *
     * @param handler
     * @throws IOException
     */
    void parse(EntryHandler<T> handler) throws IOException;

    /**
     * Parses the whole data store and returns all entries.
     *
     * @return An iterable containing all entries in the data store.
     * @throws IOException
     */
    Iterable<T> parseAll() throws IOException;
}
