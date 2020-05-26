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

import static de.cebitec.common.sequencetools.intervals.Intervals.operations;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IntegerIntervalOperations implements IntervalOperations<Integer> {

    @Override
    public boolean beginsWith(Interval<Integer> fst, Interval<Integer> snd) {
        return fst.as(Interval.Type.ZeroOpen).getStart() == snd.as(Interval.Type.ZeroOpen).getStart();
    }

    @Override
    public boolean endsWith(Interval<Integer> fst, Interval<Integer> snd) {
        return fst.as(Interval.Type.ZeroOpen).getEnd() == snd.as(Interval.Type.ZeroOpen).getEnd();
    }

    @Override
    public boolean contains(Interval<Integer> fst, Interval<Integer> snd) {
        fst = fst.as(Interval.Type.ZeroOpen);
        snd = snd.as(Interval.Type.ZeroOpen);

        return fst.getStart() <= snd.getStart() && fst.getEnd() >= snd.getEnd() // inside borders
                && snd.getStart() < fst.getEnd(); // for empty snd interval
    }

    @Override
    public boolean overlap(Interval<Integer> fst, Interval<Integer> snd) {
        if (fst.isEmpty() || snd.isEmpty()) {
            return false;
        }
        // normalize
        fst = fst.as(Interval.Type.ZeroOpen);
        snd = snd.as(Interval.Type.ZeroOpen);

        if (fst.getStart() > snd.getStart()) {
            Interval<Integer> tmp = fst;
            fst = snd;
            snd = tmp;
        }

        return fst.equals(snd) || !(fst.getEnd() - 1 < snd.getStart());
    }

    @Override
    public boolean adjacent(Interval<Integer> fst, Interval<Integer> snd) {
        // normalize
        fst = fst.as(Interval.Type.ZeroOpen);
        snd = snd.as(Interval.Type.ZeroOpen);

        if (overlap(fst, snd)) {
            return false;
        }

        // sort by start
        if (fst.getStart() > snd.getStart()) {
            Interval<Integer> tmp = fst;
            fst = snd;
            snd = tmp;
        }

        return distance(fst, snd) == 0;
    }

    @Override
    public boolean overlapOrAdjacent(Interval<Integer> fst, Interval<Integer> snd) {
        return overlap(fst, snd) || adjacent(fst, snd);
    }

    @Override
    public Integer distance(Interval<Integer> fst, Interval<Integer> snd) {
        int startDist = fst.getStart() - snd.getStart();
        int endDist = fst.getEnd() - snd.getEnd();
        int startEndDist = fst.getStart() - snd.getEnd();
        int endStartDist = fst.getEnd() - snd.getStart();

        int min = Integer.MAX_VALUE;

        if (Math.abs(startDist) < Math.abs(min)) {
            min = startDist;
        }
        if (Math.abs(endDist) < Math.abs(min)) {
            min = endDist;
        }
        if (Math.abs(startEndDist) < Math.abs(min)) {
            min = startEndDist;
        }
        if (Math.abs(endStartDist) < Math.abs(min)) {
            min = endStartDist;
        }
        return min;
    }

    @Override
    public Interval<Integer> union(Interval<Integer> fst, Interval<Integer> snd) {
        if (!overlapOrAdjacent(fst, snd)) {
            throw new IllegalArgumentException("Can't union the non overlapping and non adjacent intervals. " + fst + "; " + snd);
        }
        // normalize
        fst = fst.as(Interval.Type.OneClosed);
        snd = snd.as(Interval.Type.OneClosed);

        // sort by start
        if (fst.getStart() > snd.getStart()) {
            Interval<Integer> tmp = fst;
            fst = snd;
            snd = tmp;
        }

        int nstart = fst.getStart();
        int nend = fst.getEnd() > snd.getEnd() ? fst.getEnd() : snd.getEnd();

        return Intervals.createInterval(nstart, nend, Interval.Type.OneClosed);
    }

    @Override
    public Interval<Integer> intersection(Interval<Integer> fst, Interval<Integer> snd) {
        if (!overlap(fst, snd)) {
            return Intervals.EMPTY;
        }
        // normalize
        fst = fst.as(Interval.Type.OneClosed);
        snd = snd.as(Interval.Type.OneClosed);

        // sort by start
        if (fst.getStart() > snd.getStart()) {
            Interval<Integer> tmp = fst;
            fst = snd;
            snd = tmp;
        }

        int nstart = snd.getStart() > fst.getStart() ? snd.getStart() : fst.getStart();
        int nend = fst.getEnd() < snd.getEnd() ? fst.getEnd() : snd.getEnd();

        return Intervals.createInterval(nstart, nend, Interval.Type.OneClosed);
    }

    @Override
    public Interval<Integer> complement(Interval<Integer> fst, Interval<Integer> snd) {
        if (!overlap(fst, snd)) {
            return fst;
        }
        if (fst.equals(snd)) {
            return Intervals.EMPTY;
        }
        // normalize
        fst = fst.as(Interval.Type.OneClosed);
        snd = snd.as(Interval.Type.OneClosed);

        return Intervals.createInterval(fst.getStart(), snd.getStart() - 1, Interval.Type.OneClosed);
    }

    @Override
    public Interval<Integer> shift(Interval<Integer> i, Integer by) {
        Interval<Integer> as = i.as(Interval.Type.ZeroOpen);
        return Intervals.createInterval(as.getStart() + by, as.getEnd() + by);
    }

    @Override
    public boolean leftOf(Interval<Integer> fst, Interval<Integer> snd) {
        // normalize
        fst = fst.as(Interval.Type.OneClosed);
        snd = snd.as(Interval.Type.OneClosed);
        if (overlap(fst, snd)) {
            return false;
        } else {
            return fst.getEnd() < snd.getStart();
        }
    }

    @Override
    public boolean rightOf(Interval<Integer> fst, Interval<Integer> snd) {
        // normalize
        fst = fst.as(Interval.Type.OneClosed);
        snd = snd.as(Interval.Type.OneClosed);
        if (overlap(fst, snd)) {
            return false;
        } else {
            return snd.getEnd() < fst.getStart();
        }
    }

    @Override
    public List<Interval<Integer>> complement(Interval<Integer> reference, Iterable<? extends Interval<Integer>> parts) {
        if (isEmpty(parts)) {
            return Collections.singletonList(reference);
        }
        List<Interval<Integer>> output = new LinkedList<>();
        Interval<Integer> previous = null;

        for (Interval<Integer> interval : parts) {
            if (operations().contains(interval, reference)) {
                return Collections.<Interval<Integer>>emptyList();
            } else {
                Interval<Integer> intersection = operations().intersection(reference, interval);
                Interval<Integer> complementInterval;
                if (previous == null) {
                    complementInterval = Intervals.createInterval(reference.getStart(), intersection.getStart());
                } else {
                    complementInterval = Intervals.createInterval(previous.getEnd(), intersection.getStart());
                }
                if (!complementInterval.isEmpty()) {
                    output.add(complementInterval);
                }
                previous = intersection;
            }
        }

        if (previous != null) {
            Interval<Integer> complementInterval = Intervals.createInterval(previous.getEnd(), reference.getEnd());
            if (!complementInterval.isEmpty()) {
                output.add(complementInterval);
            }
        }

        return output;
    }

    @Override
    public Interval<Integer> enclose(Iterable<? extends Interval<Integer>> interval) {
        if (isEmpty(interval)) {
            return Intervals.EMPTY;
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Interval<Integer> i : interval) {
            i = i.as(Interval.Type.ZeroOpen);
            min = Math.min(min, i.getStart());
            max = Math.max(max, i.getEnd());
        }
        return Intervals.createInterval(min, max);
    }

    private static boolean isEmpty(Iterable<?> iterable) {
        if (iterable instanceof Collection) {
            return ((Collection<?>) iterable).isEmpty();
        }
        return !iterable.iterator().hasNext();
    }

}
