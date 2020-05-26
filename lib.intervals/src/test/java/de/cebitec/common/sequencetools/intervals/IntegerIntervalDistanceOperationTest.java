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
public class IntegerIntervalDistanceOperationTest {

    private IntegerIntervalOperations ops = new IntegerIntervalOperations();
    private Interval<Integer> fst;
    private Interval<Integer> snd;
    private Integer distance;
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // ZeroOpen intervals
            // equal
            {Intervals.createInterval(0, 2), Intervals.createInterval(0, 2), 0},
            // below overlapping
            {Intervals.createInterval(0, 2), Intervals.createInterval(-1, 1), 1},
            // above overlapping
            {Intervals.createInterval(0, 2), Intervals.createInterval(1, 3), -1},
            // below non-overlapping
            {Intervals.createInterval(0, 2), Intervals.createInterval(-2, -1), 1},
            // above non-overlapping
            {Intervals.createInterval(0, 2), Intervals.createInterval(3, 4), -1},
            // contained
            {Intervals.createInterval(0, 4), Intervals.createInterval(2, 3), 1},
            
            {Intervals.createInterval(0, 1), Intervals.createInterval(2, 3), -1},
            {Intervals.createInterval(0, 2), Intervals.createInterval(2, 3), 0},
            {Intervals.createInterval(0, 3), Intervals.createInterval(2, 3), 0},
            
            // OneClosed intervals
            {Intervals.createInterval(0, 0, Interval.Type.OneClosed), Intervals.createInterval(2, 3, Interval.Type.OneClosed), -1},
            {Intervals.createInterval(0, 1, Interval.Type.OneClosed), Intervals.createInterval(2, 3, Interval.Type.OneClosed), 0},
            {Intervals.createInterval(0, 2, Interval.Type.OneClosed), Intervals.createInterval(2, 4, Interval.Type.OneClosed), 1}
        });
    }

    public IntegerIntervalDistanceOperationTest(Interval<Integer> fst, Interval<Integer> snd, Integer distance) {
        this.fst = fst;
        this.snd = snd;
        this.distance = distance;
    }

    @Test
    public void testDistance() {
        assertThat(ops.distance(fst, snd), is(distance));
    }
}