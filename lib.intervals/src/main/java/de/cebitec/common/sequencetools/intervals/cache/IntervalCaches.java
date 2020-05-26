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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory for IntervalCaches
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IntervalCaches {

    /**
     * Only For debugging and testing purposes.
     */
    public static class StaticListFetchCallback<T> implements IntervalCache.FetchCallback<List<T>> {

        private final List<T> value;

        public StaticListFetchCallback(List<T> value) {
            this.value = value;
        }

        @Override
        public List<T> fetch(Interval<Integer> interval) throws IndexOutOfBoundsException {
            return value.subList(interval.getStart(), interval.getEnd());
        }
    }

    public static class ListExtractCallback<T> implements IntervalCache.ExtractCallback<List<T>> {

        @Override
        public List<T> extract(Interval<Integer> interval, List<T> whole) {
            return whole.subList(interval.getStart(), interval.getEnd());
        }
    }

    public static class ListMergeCallback<T> implements IntervalCache.MergeCallback<List<T>> {

        @Override
        public List<T> merge(List<T> left, List<T> right) {
            List<T> list = new ArrayList<>(left.size() + right.size());
            list.addAll(left);
            list.addAll(right);
            return list;
        }
    }

    public static class ListLengthCallback<T> implements IntervalCache.LengthCallback<List<T>> {

        @Override
        public int length(List<T> value) {
            return value.size();
        }
    }

    /**
     * Factory method for list interval caches.
     *
     * @param <T>
     * @param fetchCallback
     * @return
     */
    public static <T> IntervalCache<List<T>> createListCache(IntervalCache.FetchCallback<List<T>> fetchCallback) {
        return create(
            Collections.<T>emptyList(),
            fetchCallback,
            new ListMergeCallback<T>(),
            new ListExtractCallback<T>(),
            new ListLengthCallback<T>());
    }

    /**
     * Only For debugging and testing purposes.
     */
    public static class StaticStringFetchCallback implements IntervalCache.FetchCallback<String> {

        private final String string;

        public StaticStringFetchCallback(String string) {
            this.string = string;
        }

        @Override
        public String fetch(Interval<Integer> interval) throws IndexOutOfBoundsException {
            return string.substring(interval.getStart(), interval.getEnd());
        }
    }

    public static class StringExtractCallback implements IntervalCache.ExtractCallback<String> {

        @Override
        public String extract(Interval<Integer> interval, String whole) {
            return whole.substring(interval.getStart(), interval.getEnd());
        }
    }

    public static class StringMergeCallback implements IntervalCache.MergeCallback<String> {

        @Override
        public String merge(String left, String right) {
            return left + right;
        }
    }

    public static class StringLengthCallback implements IntervalCache.LengthCallback<String> {

        @Override
        public int length(String value) {
            return value.length();
        }
    }

    public static final IntervalCache.MergeCallback<String> merge = new StringMergeCallback();
    public static final IntervalCache.ExtractCallback<String> extract = new StringExtractCallback();
    public static final IntervalCache.LengthCallback<String> length = new StringLengthCallback();

    /**
     * Factory method for string interval caches.
     *
     * @param fetchCallback
     * @return
     */
    public static IntervalCache<String> createStringCache(IntervalCache.FetchCallback<String> fetchCallback) {
        return create("", fetchCallback, merge, extract, length);
    }

    /**
     * Factory method to instantiate an interval cache for arbitrary sequences.
     *
     * @param <T>
     * @param defaultValue
     * @param fetchCallback
     * @param mergeCallback
     * @param extractCallback
     * @param lengthCallback
     * @return
     */
    public static <T> IntervalCache<T> create(T defaultValue,
                                              IntervalCache.FetchCallback<T> fetchCallback,
                                              IntervalCache.MergeCallback<T> mergeCallback,
                                              IntervalCache.ExtractCallback<T> extractCallback,
                                              IntervalCache.LengthCallback<T> lengthCallback) {
        return new IntervalCacheImpl<>(defaultValue,
                                       fetchCallback,
                                       mergeCallback,
                                       extractCallback,
                                       lengthCallback);
    }
}
