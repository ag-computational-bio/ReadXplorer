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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class ListIntervalCacheImplementationAddValueTest {

    private IntervalCacheImpl<List<Integer>> sc;

    @Before
    public void init() {
        IntervalCache.FetchCallback<List<Integer>> mock = mock(IntervalCache.FetchCallback.class);
        sc = new IntervalCacheImpl<>(
            Collections.<Integer>emptyList(),
            mock,
            new IntervalCaches.ListMergeCallback<Integer>(),
            new IntervalCaches.ListExtractCallback<Integer>(),
            new IntervalCaches.ListLengthCallback<Integer>());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullList() {
        sc.addValue(Intervals.createInterval(0, 1), null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullInterval() {
        sc.addValue(null, Arrays.asList(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverlappingInterval() {
        sc.addValue(Intervals.createInterval(0, 1), Arrays.asList(1));
        sc.addValue(Intervals.createInterval(0, 2), Arrays.asList(1,2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntervalListLengthMismatch() {
        sc.addValue(Intervals.createInterval(0, 1), Arrays.asList(1,2));
    }
}
