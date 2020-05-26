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
package de.cebitec.common.sequencetools.intervals.cache;

import de.cebitec.common.sequencetools.intervals.Interval;

/**
 * An IntervalCache is used to avoid the loading of whole, possibly large parts of contigous sequences of data, when
 * only (small) parts of them are needed at a time. An IntervalCache needs at leadst three callback methods to work. The
 * fetchcallback to resolve lazily loaded parts of the sequence, a mergecallback in order to merge adjacent sequences to
 * a whole sequence and an extractcallback in order to retrieve subsequences.<br/>
 *
 * Examples for sequences may be lists or strings.
 *
 * @see IntervalCaches
 * 
 * @param <T> The sequencetype
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public interface IntervalCache<T> {

    public interface FetchCallback<T> {

        /**
         * Retrieves a part of the sequence.
         *
         * @param interval
         * @return
         * @throws IndexOutOfBoundsException If a non existent part of the cached sequence is requested.
         */
        T fetch(final Interval<Integer> interval) throws IndexOutOfBoundsException;
    }

    public interface MergeCallback<T> {

        /**
         * Merges two adjacent sequences and returns the merged sequence.
         *
         * @param left
         * @param right
         * @return
         */
        T merge(final T left, final T right);
    }

    public interface ExtractCallback<T> {

        /**
         * Extracts a subinterval from a sequence.
         *
         * @param interval The subinterval in the coordinate system of the <code>whole</code> sequence.
         * @param whole
         * @return
         */
        T extract(final Interval<Integer> interval, final T whole);
    }

    public interface LengthCallback<T> {

        /**
         * Calculates the length for a sequence.
         *
         * @param value
         * @return
         */
        int length(final T value);
    }

    /**
     * Retrieves a substring of the cached string.
     *
     * @param interval
     * @return
     */
    T get(final Interval<Integer> interval);

    /**
     * Clears the cache.
     */
    void invalidate();
}
