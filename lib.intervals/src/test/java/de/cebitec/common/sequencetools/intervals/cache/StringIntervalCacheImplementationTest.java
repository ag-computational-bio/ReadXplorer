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
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class StringIntervalCacheImplementationTest {

    private IntervalCache<String> sc;

    @Before
    public void init() {
        sc = IntervalCaches.createStringCache(demoCallback);
    }

    final String demoString = "01234567890abcdef";
    final IntervalCache.FetchCallback<String> demoCallback = new IntervalCache.FetchCallback<String>() {

        @Override
        public String fetch(Interval<Integer> interval) throws IndexOutOfBoundsException {
            return demoString.substring(interval.getStart(), interval.getEnd());
        }
    };

    // test empty interval
    // test contained interval
    @Test(expected = NullPointerException.class)
    public void testMissingCallback() {
        sc = IntervalCaches.createStringCache(null);
    }

    @Test
    public void testGetEmptyInterval() {
        Interval<Integer> wanted = Intervals.createInterval(0, 0);
        String expected = "";

        String result = sc.get(wanted);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetWholeInterval() throws InterruptedException {
        Interval<Integer> wanted = Intervals.createInterval(0, 17);
        String expected = demoString;

        String result = sc.get(wanted);
        assertThat(result, equalTo(expected));

        // force garbage collection of weak references inside string cache implementation
        result = null;
        System.gc();

        result = sc.get(wanted);
        assertThat(result, equalTo(expected));
    }

}
