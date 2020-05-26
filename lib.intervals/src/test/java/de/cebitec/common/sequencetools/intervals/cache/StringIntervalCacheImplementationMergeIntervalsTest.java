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

import de.cebitec.common.sequencetools.intervals.Intervals;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import static org.mockito.Mockito.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class StringIntervalCacheImplementationMergeIntervalsTest {

    private IntervalCacheImpl<String> sc;

    @Before
    public void init() {
        IntervalCache.FetchCallback<String> mock = mock(IntervalCache.FetchCallback.class);
        sc = new IntervalCacheImpl<>("", mock, IntervalCaches.merge, IntervalCaches.extract, IntervalCaches.length);
    }

    @Test
    public void testMergeSingleInterval() {
        sc.addValue(Intervals.createInterval(0, 1), "1");
        sc.mergeAdjacentIntervals(Intervals.createInterval(0, 1));

        assertThat(sc.getMap().entrySet(), hasSize(1));
        assertThat(sc.get(Intervals.createInterval(0, 1)), equalTo("1"));
    }

    @Test
    public void testMergeableIntervals() {
        sc.addValue(Intervals.createInterval(0, 1), "1");
        sc.addValue(Intervals.createInterval(1, 2), "2");
        sc.mergeAdjacentIntervals(Intervals.createInterval(0, 2));

        assertThat(sc.getMap().entrySet(), hasSize(1));
        assertThat(sc.get(Intervals.createInterval(0, 2)), equalTo("12"));
    }

    @Test
    public void testMergeableIntervals2() {
        sc.addValue(Intervals.createInterval(1, 2), "2");
        sc.addValue(Intervals.createInterval(0, 1), "1");
        sc.mergeAdjacentIntervals(Intervals.createInterval(0, 2));

        assertThat(sc.getMap().entrySet(), hasSize(1));
        assertThat(sc.get(Intervals.createInterval(0, 2)), equalTo("12"));
    }

    @Test
    public void testNonMergeableIntervals() {
        sc.addValue(Intervals.createInterval(0, 1), "1");
        sc.addValue(Intervals.createInterval(2, 3), "3");

        sc.mergeAdjacentIntervals(Intervals.createInterval(0, 3));
        assertThat(sc.getMap().entrySet(), hasSize(2));
    }
}
