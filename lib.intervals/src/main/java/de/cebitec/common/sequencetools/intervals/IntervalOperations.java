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
package de.cebitec.common.sequencetools.intervals;

import java.util.List;

/**
 * This class defines operations on intervals. Internally the operations should operate on ZeroOpen intervals to allow
 * the handling of empty intervals.
 *
 * @param <L> The number type the interval uses.
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public interface IntervalOperations<L extends Number> {

    /**
     * Checks whether the two intervals overlap each other. Overlap is defined such that two intervals have a common
     * subinterval that has a length larger than zero. This means that empty intervals do never overlap to any other
     * interval.
     *
     * @param fst
     * @param snd
     * @return
     */
    public boolean overlap(Interval<L> fst, Interval<L> snd);

    /**
     * Checks whether the two intervals are adjacent to each other. Adjacency is defined such that two intervals are
     * adjacent when they do not overlap and the distance between them is zero.
     *
     * @param fst
     * @param snd
     * @return
     */
    public boolean adjacent(Interval<L> fst, Interval<L> snd);

    /**
     * Checks whether the two intervals overlap or are adjacent.
     *
     * @param fst
     * @param snd
     * @return
     */
    public boolean overlapOrAdjacent(Interval<L> fst, Interval<L> snd);

    /**
     * The distance between two intervals is defined as the minimal distance that is needed to shift the second
     * intervals such that either the starts or ends are equal or that a start and an end are equal.
     *
     * @param fst
     * @param snd
     * @return
     */
    public L distance(Interval<L> fst, Interval<L> snd);

    /**
     * Shifts an interval by a given length, e.g. [2,3] shift by -2 results in [0,1].
     *
     * @param i
     * @param by
     * @return
     */
    public Interval<L> shift(Interval<L> i, L by);

    /* ********* Operations on overlapping or adjacent intervals ************ */
    /**
     * Calculates the union of two adjacent or overlapping intervals.
     *
     * @param fst
     * @param snd
     * @return
     * @throws IllegalArgumentException If the intervals do not overlap.
     */
    public Interval<L> union(Interval<L> fst, Interval<L> snd);

    /**
     * Calculates the intersection of two intervals i.e. the interval common to both.
     *
     * @param fst
     * @param snd The intersection or an empty interval if they do not overlap.
     * @return
     */
    public Interval<L> intersection(Interval<L> fst, Interval<L> snd);

    /**
     * Calculates the complement of two intervals i.e. the interval that is contained in the first interval, but not in
     * the second interval.
     *
     * @param fst
     * @param snd
     * @return
     */
    public Interval<L> complement(Interval<L> fst, Interval<L> snd);

    /**
     * Checks if the fst interval starts at the same position as the snd interval.
     *
     * @param fst
     * @param snd
     * @return
     */
    boolean beginsWith(Interval<L> fst, Interval<L> snd);

    /**
     * Checks if the fst interval completely contains the snd interval.
     *
     * @param fst
     * @param snd
     * @return
     */
    boolean contains(Interval<L> fst, Interval<L> snd);

    /**
     * Checks if the fst interval ends at the same position as the snd interval.
     *
     * @param fst
     * @param snd
     * @return
     */
    boolean endsWith(Interval<L> fst, Interval<L> snd);

    /**
     * Checks if the fst interval does not overlap with the snd interval and is left of it.
     *
     * @param fst
     * @param snd
     * @return
     */
    boolean leftOf(Interval<L> fst, Interval<L> snd);

    /**
     * Checks if the fst interval does not overlap with the snd interval and is right of it
     *
     * @param fst
     * @param snd
     * @return
     */
    boolean rightOf(Interval<L> fst, Interval<L> snd);

    /**
     * Calculates the complement for one interval and a list of intervals.
     *
     * @param fst
     * @param snd
     * @return An empty list if there is no complement, otherwise a list of intervals contained in fst, but not in snd.
     */
    List<Interval<L>> complement(Interval<L> fst, Iterable<? extends Interval<L>> snd);

    /**
     * Creates the smallest interval that encloses all intervals in the iterable.
     *
     * @param intervals
     * @return
     */
    Interval<Integer> enclose(Iterable<? extends Interval<Integer>> intervals);

}
