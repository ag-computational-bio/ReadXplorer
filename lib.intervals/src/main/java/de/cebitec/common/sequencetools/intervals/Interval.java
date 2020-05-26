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
package de.cebitec.common.sequencetools.intervals;

/**
 * An Interval describes a part on an indexed region by a start and a end
 * coordinate. An interval has a type that determines the start position and
 * whether the end position is included or excluded from the interval. To
 * instantiate intervals and interact with them use the methods in the Intervals
 * class.
 *
 * @param <T> The number type the interval uses.
 * @see Intervals
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public interface Interval<T extends Number> {

    public enum Type {

        /**
         * The first position is 1 and the last position n is in the interval,
         * e.g. [1..n]. This is a common case in biological data. Needs a lot of
         * +1-1 juggling, so it should be avoided for calculations. But it is
         * desired for presentation.
         */
        OneClosed,
        /**
         * The first position is 0 and the last position n is not included in
         * the interval, e.g. [0..n). Intervals are converted to this coordinate
         * system by default.
         */
        ZeroOpen,
        /**
         * The first position is 1 and the last position n is not included in
         * the interval, e.g. [1..n).
         */
        OneOpen,
        /**
         * The first position is 0 and the last position n is included in the
         * interval, e.g. [1..n]
         */
        ZeroClosed
    }

    /**
     * The type of the interval.
     *
     * @return
     */
    Type getType();

    /**
     * Calculates the length of the interval.
     *
     * @return
     */
    T getLength();

    /**
     * The start of the interval.
     *
     * @return
     */
    T getStart();

    /**
     * The end of the interval.
     *
     * @return
     */
    T getEnd();

    /**
     * Equivalent to getLength() == 0.
     *
     * @return true if this interval has zero length, false otherwise.
     */
    boolean isEmpty();

    /**
     * Transforms the interval to the given type.
     *
     * @param newType
     * @return The transformed interval.
     */
    Interval<T> as(Type newType);
}
