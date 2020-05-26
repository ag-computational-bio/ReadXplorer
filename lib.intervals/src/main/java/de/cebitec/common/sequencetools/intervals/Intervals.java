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

import de.cebitec.common.sequencetools.intervals.Interval.Type;
import java.util.Comparator;
import java.util.List;

/**
 * Helper and factory class for intervals.
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class Intervals {

    public static final Interval<Integer> EMPTY = emptyInterval();
    public static final IntegerIntervalOperations integerIntervalOperations = new IntegerIntervalOperations();

    public static Interval<Integer> emptyInterval() {
        if (EMPTY == null) {
            return createInterval(0, 0);
        }
        return EMPTY;
    }

    /**
     * Creates an interval for the coordinates given in zeroOpen coordinates.
     *
     * @param start
     * @param end
     * @return
     */
    public static Interval<Integer> createInterval(int start, int end) {
        return createInterval(start, end, Type.ZeroOpen);
    }

    /**
     * Creates an interval ranging from start to end in zeroOpen coordinates. If type is not ZeroOpen the start and end
     * positions of the interval will be transformed to be ZeroOpen.
     *
     * @param start The start of the interval.
     * @param end The end of the interval.
     * @param type The type coordinate system of start and stop
     * @return An interval in ZeroOpen coordinates.
     */
    public static Interval<Integer> createInterval(int start, int end, Type type) {
//        if (start > end) {
//            throw new IllegalArgumentException("Start must be lower than end.");
//        }
        return new SerializableInterval(normalizeStart(start, type), normalizeEnd(end, type), Type.ZeroOpen);
    }

    /**
     * Transforms an interval into another coordinate system.
     *
     * @param interval The interval.
     * @param targetType The target coordinate system.
     * @return
     */
    public static Interval<Integer> transfromInterval(Interval<Integer> interval, Type targetType) {
        Interval<Integer> normalized = normalize(interval);

        return new SerializableInterval(
            deNormalizeStart(normalized.getStart(), targetType),
            deNormalizeEnd(normalized.getEnd(), targetType),
            targetType);
    }

    /**
     * Normalizes an interval to a zeroOpen interval.
     *
     * @param interval
     * @return
     */
    public static Interval<Integer> normalize(Interval<Integer> interval) {
        if (Type.ZeroOpen.equals(interval.getType())) {
            return interval;
        } else {
            return createInterval(interval.getStart(), interval.getEnd(), interval.getType());
        }
    }

    public static IntervalFormatterBuilder<Integer> newFormatter() {
        return new IntervalFormatterBuilder<>();
    }

    public static IntervalOperations<Integer> operations() {
        return integerIntervalOperations;
    }

    /**
     * Corrects the start depending on the type so that it can be used with a java String.
     *
     * @param start
     * @param t
     * @return
     */
    static int normalizeStart(final int start, Type t) {
        switch (t) {
            case OneClosed:
                return start - 1;
            case OneOpen:
                return start - 1;
            default:
                return start;
        }
    }

    /**
     * Corrects the end depending on the type so that it can be used with a java String.
     *
     * @param end an end position
     * @param t the source type
     * @return the new end respective to the java String implementation
     */
    static int normalizeEnd(final int end, final Type t) {
        switch (t) {
            case ZeroClosed:
                return end + 1;
            case OneOpen:
                return end - 1;
            default:
                return end;
        }
    }

    /**
     * Inverse operation for normalizeStart.
     *
     * @param start An start position in ZeroOpen coordinates
     * @param t The target coordinate system
     * @return The transformed end
     */
    static int deNormalizeStart(final int start, Type t) {
        switch (t) {
            case OneClosed:
                return start + 1;
            case OneOpen:
                return start + 1;
            default:
                return start;
        }
    }

    /**
     * Inverse operation for normalizeEnd.
     *
     * @param end An end position in ZeroOpen coordinates
     * @param t The target coordinate system
     * @return The transformed end
     */
    static int deNormalizeEnd(final int end, final Type t) {
        switch (t) {
            case ZeroClosed:
                return end - 1;
            case OneOpen:
                return end + 1;
            default:
                return end;
        }
    }

    /**
     * Sums up the total length of a list of intervals.
     *
     * @param totalLength
     * @return
     */
    public static Integer totalLength(List<? extends Interval<Integer>> totalLength) {
        int i = 0;
        for (Interval<Integer> interval : totalLength) {
            i += interval.getLength();
        }
        return i;
    }

    /**
     * Compares the invervals by start position.
     */
    public static class IntervalComparator implements Comparator<Interval<Integer>> {

        @Override
        public int compare(Interval<Integer> o1, Interval<Integer> o2) {
            return o1.getStart().compareTo(o2.getStart());
        }
    }
}
