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
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
@RunWith(Parameterized.class)
public class IntegerIntervalOverlapOperationTest {

    private IntegerIntervalOperations ops = new IntegerIntervalOperations();
    private Interval<Integer> fst;
    private Interval<Integer> snd;
    private boolean overlap;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // ZeroOpen intervals
            // empty intervals
            {Intervals.createInterval(0, 0), Intervals.createInterval(0, 0), false},
            {Intervals.createInterval(0, 0), Intervals.createInterval(1, 1), false},
            // fst emtpy
            {Intervals.createInterval(0, 0), Intervals.createInterval(0, 1), false},
            // snd empty
            {Intervals.createInterval(0, 1), Intervals.createInterval(0, 0), false},
            // no overlap snd below fst
            {Intervals.createInterval(0, 1), Intervals.createInterval(-2, -1), false},
            // no overlap snd above fst
            {Intervals.createInterval(0, 1), Intervals.createInterval(1, 2), false},
            // overlap snd below fst
            {Intervals.createInterval(0, 2), Intervals.createInterval(-1, 1), true},
            // overlap snd above fst
            {Intervals.createInterval(0, 2), Intervals.createInterval(1, 3), true},
            // snd contained in fst
            {Intervals.createInterval(0, 4), Intervals.createInterval(1, 3), true},
            // fst contained in snd
            {Intervals.createInterval(1, 3), Intervals.createInterval(0, 4), true},

            // OneClosed intervals
            {Intervals.createInterval(0, 0, Interval.Type.OneClosed), Intervals.createInterval(2, 3, Interval.Type.OneClosed), false},
            {Intervals.createInterval(0, 1, Interval.Type.OneClosed), Intervals.createInterval(2, 3, Interval.Type.OneClosed), false},
            {Intervals.createInterval(0, 2, Interval.Type.OneClosed), Intervals.createInterval(2, 3, Interval.Type.OneClosed), true}
        });
    }

    public IntegerIntervalOverlapOperationTest(Interval<Integer> fst, Interval<Integer> snd, boolean overlap) {
        this.fst = fst;
        this.snd = snd;
        this.overlap = overlap;
    }

    @Test
    public void testOverlap() {
        assertThat(ops.overlap(fst, snd), is(overlap));
    }
}