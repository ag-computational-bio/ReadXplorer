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

/**
 * A streaming writer can be used to write instances to a datastore, e.g. a file element by element.
 *
 * @param <T> The type the writer consumes.
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public interface StreamingWriter<T> extends Closeable {

    /**
     * Writes the instance to the underlying store.
     *
     * @param instance An instance. Must not be null.
     * @throws IOException
     */
    void write(T instance) throws IOException;

    /**
     * Writes an interable of instances to the underlying store.
     *
     * @param instance An interable of instances. Must not be null.
     * @throws IOException
     */
    void write(Iterable<T> instance) throws IOException;
}
