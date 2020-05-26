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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IntegerIntervalListComplementOperationTest {
    private IntegerIntervalOperations ops = new IntegerIntervalOperations();

    @Test
    public void testEmptyCollection() {
        Interval<Integer> wanted = Intervals.createInterval(0, 100);
        List<Interval<Integer>> given = Collections.<Interval<Integer>>emptyList();

        List<Interval<Integer>> result = ops.complement(wanted, given);

        List<Interval<Integer>> expected = Collections.singletonList(wanted);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testCompleteOverlap() {
        Interval<Integer> wanted = Intervals.createInterval(0, 100);
        List<Interval<Integer>> given = Collections.singletonList(wanted);

        List<Interval<Integer>> result = ops.complement(wanted, given);

        assertThat(result, empty());
    }

    @Test
    public void testPartOfLargeInterval() {
        Interval<Integer> wanted = Intervals.createInterval(10, 100);
        List<Interval<Integer>> given = Collections.singletonList(Intervals.createInterval(0, 200));

        List<Interval<Integer>> result = ops.complement(wanted, given);

        assertThat(result, empty());
    }

    @Test
    public void testLeftOverlap() {
        Interval<Integer> wanted = Intervals.createInterval(10, 100);
        List<Interval<Integer>> given = Collections.singletonList(Intervals.createInterval(0, 20));

        List<Interval<Integer>> result = ops.complement(wanted, given);
        List<Interval<Integer>> expected = Collections.singletonList(Intervals.createInterval(20, 100));

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testRightOverlap() {
        Interval<Integer> wanted = Intervals.createInterval(10, 100);
        List<Interval<Integer>> given = Collections.singletonList(Intervals.createInterval(90, 200));

        List<Interval<Integer>> result = ops.complement(wanted, given);
        List<Interval<Integer>> expected = Collections.singletonList(Intervals.createInterval(10, 90));

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testMiddleOverlap() {
        Interval<Integer> wanted = Intervals.createInterval(0, 100);
        List<Interval<Integer>> given = Collections.singletonList(Intervals.createInterval(20, 30));

        List<Interval<Integer>> result = ops.complement(wanted, given);
        List<Interval<Integer>> expected = Arrays.asList(Intervals.createInterval(0, 20),
                                                         Intervals.createInterval(30, 100));

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testMultipleOverlaps() {
        Interval<Integer> wanted = Intervals.createInterval(0, 100);
        List<Interval<Integer>> given = Arrays.asList(
            Intervals.createInterval(0, 10),
            Intervals.createInterval(20, 30),
            Intervals.createInterval(90, 200)
        );

        List<Interval<Integer>> result = ops.complement(wanted, given);
        List<Interval<Integer>> expected = Arrays.asList(Intervals.createInterval(10, 20),
                                                         Intervals.createInterval(30, 90));

        assertThat(result, equalTo(expected));
    }

}
