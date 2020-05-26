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
import de.cebitec.common.sequencetools.intervals.Intervals;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class ListIntervalCacheImplementationTest {

    private IntervalCacheImpl<List<Integer>> sc;

    @Before
    public void init() {
        sc = new IntervalCacheImpl<>(
            Collections.<Integer>emptyList(),
            demoCallback,
            new IntervalCaches.ListMergeCallback<Integer>(),
            new IntervalCaches.ListExtractCallback<Integer>(),
            new IntervalCaches.ListLengthCallback<Integer>());
    }

    final List<Integer> demoData = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    final IntervalCache.FetchCallback<List<Integer>> demoCallback = new IntervalCache.FetchCallback<List<Integer>>() {

        @Override
        public List<Integer> fetch(Interval<Integer> interval) throws IndexOutOfBoundsException {
            return demoData.subList(interval.getStart(), interval.getEnd());
        }
    };

    @Test(expected = NullPointerException.class)
    public void testMissingCallback() {
        IntervalCaches.createListCache((IntervalCache.FetchCallback<List<Integer>>)null);
    }

    @Test
    public void testGetEmptyInterval() {
        Interval<Integer> wanted = Intervals.createInterval(0, 0);
        List<Integer> expected = Collections.EMPTY_LIST;

        List<Integer> result = sc.get(wanted);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetWholeInterval() throws InterruptedException {
        Interval<Integer> wanted = Intervals.createInterval(0, 17);
        List<Integer> expected = demoData;

        List<Integer> result = sc.get(wanted);
        assertThat(result, equalTo(expected));

        // force garbage collection of weak references inside string cache implementation
        result = null;
        System.gc();

        result = sc.get(wanted);
        assertThat(result, equalTo(expected));
    }

}
