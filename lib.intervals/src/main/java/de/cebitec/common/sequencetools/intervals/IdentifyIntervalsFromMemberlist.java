/*
 * Copyright (C) 2014 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Given a global interval coordinate system that is assembled from multiple subsequent smaller 'local' interval
 * coordinates, this algorithm calculates the subsets of the 'local' coordinates that are needed to assemble a
 * subinterval in the 'global' coordinate system. <br />
 * <br />
 * The input is a list of members, where each member defines some id, e.g. a string identifier or the real local
 * instance that is part of the global coordinate system, and a length. The output is a list of memberresults where each
 * result reflects the identifier from the input and the 'local' portion of the interval that has to be extracted from
 * the local interval in order to assemble one part of the global interval. <br />
 * <br />
 * Examples usages: <br />
 * - extract a subsequence from an scaffold that is constructed from multiple contigs and gaps.<br />
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IdentifyIntervalsFromMemberlist {

    /**
     * A member defines an identifier and a location in the global coordinate system.
     *
     * @param <T> The identifier type.
     */
    public static interface Member<T> {

        T getId();

        Interval<Integer> getGlobalInterval();
    }

    public static class DefaultMember<T> implements Member<T> {

        private final T id;
        private final Interval<Integer> globalInterval;

        public DefaultMember(T id, Interval<Integer> globalInterval) {
            this.id = id;
            this.globalInterval = globalInterval;
        }

        @Override
        public T getId() {
            return id;
        }

        @Override
        public Interval<Integer> getGlobalInterval() {
            return globalInterval;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.id);
            hash = 67 * hash + Objects.hashCode(this.globalInterval);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DefaultMember<?> other = (DefaultMember<?>) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.globalInterval, other.globalInterval)) {
                return false;
            }
            return true;
        }
    }

    /**
     * A memberresult defines an identifier and a location in the local coordinate system.
     *
     * @param <T> The identifier type.
     */
    public static class MemberResult<T> {

        private final T id;
        private final Interval<Integer> localSubinterval;

        public MemberResult(T id, Interval<Integer> localSubinterval) {
            this.id = id;
            this.localSubinterval = localSubinterval;
        }

        public T getId() {
            return id;
        }

        public Interval<Integer> getLocalSubinterval() {
            return localSubinterval;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.id);
            hash = 83 * hash + Objects.hashCode(this.localSubinterval);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MemberResult<?> other = (MemberResult<?>) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.localSubinterval, other.localSubinterval)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return id.toString() + "[" + localSubinterval.toString() + "]";
        }
    }

    /**
     * Calculates all subintervals from the given members that are located in the given global interval coordinates.
     *
     * @param <T>
     * @param members A list of member. Must not be null.
     * @param interval An interval in the global coordinate system. If the interval is empty it is not only checked
     * whether the interval is overlapping with an existing interval, but if it is adjacent.
     * @return
     */
    public static <T> List<MemberResult<T>> identifySubintervals(List<? extends Member<? extends T>> members, Interval<Integer> interval) {
        List<MemberResult<T>> list = new LinkedList<>();
        IntervalOperations<Integer> operations = Intervals.operations();

        for (Member<? extends T> member : members) {
            final Interval<Integer> global = member.getGlobalInterval();
            Interval<Integer> local = null;
            if (interval.isEmpty() && (operations.contains(global, interval) || operations.adjacent(global, interval))) {
                local = operations.shift(interval, -global.getStart());
                MemberResult<T> result = new MemberResult<>(member.getId(), local);
                list.add(result);
            } else if (operations.overlap(global, interval)) {
                Interval<Integer> intersection = operations.intersection(global, interval);
                local = operations.shift(intersection, -global.getStart());
                MemberResult<T> result = new MemberResult<>(member.getId(), local);
                list.add(result);
            }
        }

        return list;
    }
}
