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
public class IntegerIntervalAdjacentOperationTest {

    private IntegerIntervalOperations ops = new IntegerIntervalOperations();
    private Interval<Integer> fst;
    private Interval<Integer> snd;
    private boolean adjacent;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // ZeroOpen intervals
            // both empty nonadjacent
            {Intervals.createInterval(0, 0), Intervals.createInterval(1, 1), false},
            // both empty adjacent
            {Intervals.createInterval(0, 0), Intervals.createInterval(0, 0), true},
            // fst emtpy nonadjacent
            {Intervals.createInterval(0, 0), Intervals.createInterval(1, 2), false},
            // fst emtpy adjacent
            {Intervals.createInterval(0, 0), Intervals.createInterval(0, 2), true},
            // snd emtpy nonadjacent
            {Intervals.createInterval(1, 2), Intervals.createInterval(0, 0), false},
            // snd emtpy adjacent
            {Intervals.createInterval(0, 2), Intervals.createInterval(0, 0), true},
            // snd below fst nonadjacent
            {Intervals.createInterval(0, 2), Intervals.createInterval(-2, -1), false},
            // snd above fst nonadjacent
            {Intervals.createInterval(0, 2), Intervals.createInterval(3, 5), false},
            // snd below fst adjacent
            {Intervals.createInterval(0, 2), Intervals.createInterval(-2, 0), true},
            // snd above fst adjacent
            {Intervals.createInterval(0, 2), Intervals.createInterval(2, 5), true},
            // OneClosed intervals
            {Intervals.createInterval(0, 0, Interval.Type.OneClosed), Intervals.createInterval(1, 1, Interval.Type.OneClosed), true},
            {Intervals.createInterval(0, 1, Interval.Type.OneClosed), Intervals.createInterval(3, 3, Interval.Type.OneClosed), false},
            {Intervals.createInterval(0, 2, Interval.Type.OneClosed), Intervals.createInterval(2, 3, Interval.Type.OneClosed), false}
        });
    }

    public IntegerIntervalAdjacentOperationTest(Interval<Integer> fst, Interval<Integer> snd, boolean adjacent) {
        this.fst = fst;
        this.snd = snd;
        this.adjacent = adjacent;
    }

    @Test
    public void testAdjacent() {
        assertThat(ops.adjacent(fst, snd), is(adjacent));
    }
}