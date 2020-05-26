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
public class IntegerIntervalBeginsWithOperationTest {

    private IntegerIntervalOperations ops = new IntegerIntervalOperations();
    private Interval<Integer> fst;
    private Interval<Integer> snd;
    private boolean beginsWith;
    
    @Parameterized.Parameters    
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // ZeroOpen intervals
            // does not begin with
            {Intervals.createInterval(0, 10), Intervals.createInterval(2, 10), false},
            // same
            {Intervals.createInterval(0, 10), Intervals.createInterval(0, 10), true},
            {Intervals.createInterval(0, 10), Intervals.createInterval(2, 10, Interval.Type.OneClosed), false},
            {Intervals.createInterval(0, 10), Intervals.createInterval(1, 10, Interval.Type.OneClosed), true},
        });
    }

    public IntegerIntervalBeginsWithOperationTest(Interval<Integer> fst, Interval<Integer> snd, boolean beginsWith) {
        this.fst = fst;
        this.snd = snd;
        this.beginsWith = beginsWith;
    }

    @Test
    public void testContains() {
        assertThat(ops.beginsWith(fst, snd), is(beginsWith));
    }
}