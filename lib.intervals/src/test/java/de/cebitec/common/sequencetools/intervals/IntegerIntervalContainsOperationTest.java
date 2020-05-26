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
public class IntegerIntervalContainsOperationTest {

    private IntegerIntervalOperations ops = new IntegerIntervalOperations();
    private Interval<Integer> fst;
    private Interval<Integer> snd;
    private boolean contains;

    @Parameterized.Parameters    
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // ZeroOpen intervals
            // same intervals
            {Intervals.createInterval(0, 10), Intervals.createInterval(0, 10), true},
            // not contained
            {Intervals.createInterval(0, 10), Intervals.createInterval(2, 11), false},
            // empty interval at end
            {Intervals.createInterval(0, 10), Intervals.createInterval(10, 10), false}
        });
    }

    public IntegerIntervalContainsOperationTest(Interval<Integer> fst, Interval<Integer> snd, boolean contains) {
        this.fst = fst;
        this.snd = snd;
        this.contains = contains;
    }

    @Test
    public void testContains() {
        assertThat(ops.contains(fst, snd), is(contains));
    }
}