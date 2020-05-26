/*
 * Copyright (C) 2012 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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

import de.cebitec.common.sequencetools.intervals.Interval.Type;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IntervalsTest {

    /**
     * Test of createInterval method, of class Intervals.
     */
    @Test
    public void testCreateInterval() {
        System.out.println("createInterval");

        Interval<Integer> i = Intervals.createInterval(1, 100, Type.OneClosed);
        assertEquals(Integer.valueOf(0), i.getStart());
        assertEquals(Integer.valueOf(100), i.getEnd());
        assertEquals(Integer.valueOf(100), i.getLength());
        assertEquals(Type.ZeroOpen, i.getType());
    }

    @Test
    public void testEmptyInterval() {
        System.out.println("emptyInterval");

        assertThat(Intervals.EMPTY, notNullValue());
        assertThat(Intervals.EMPTY.isEmpty(), is(true));
        assertThat(Intervals.emptyInterval(), notNullValue());
        assertThat(Intervals.emptyInterval().isEmpty(), is(true));
    }

    @Test
    public void testZeroInterval() {
        System.out.println("zeroInterval");

        Interval<Integer> i = Intervals.createInterval(1, 1);
        System.out.println("i = " + i.as(Type.OneClosed));
        assertEquals(Integer.valueOf(0), i.getLength());
        assertEquals(Type.ZeroOpen, i.getType());
    }

    @Test
    public void testTransformInterval() {
        System.out.println("transformInterval");

        Interval<Integer> i = Intervals.createInterval(1, 100, Type.OneClosed);
        Interval<Integer> t0c = Intervals.transfromInterval(i, Type.OneClosed);

        assertEquals(Integer.valueOf(1), t0c.getStart());
        assertEquals(Integer.valueOf(100), t0c.getEnd());
        assertEquals(Integer.valueOf(100), t0c.getLength());
        assertEquals(Type.OneClosed, t0c.getType());
    }

    /**
     * Test of normalizeStart method, of class Intervals.
     */
    @Test
    public void testNormalizeStart() {
        System.out.println("normalizeStart");
        assertEquals(0, Intervals.normalizeStart(1, Type.OneOpen));
        assertEquals(1000, Intervals.normalizeStart(1001, Type.OneOpen));

        assertEquals(0, Intervals.normalizeStart(1, Type.OneClosed));
        assertEquals(1000, Intervals.normalizeStart(1001, Type.OneClosed));

        assertEquals(1, Intervals.normalizeStart(1, Type.ZeroOpen));
        assertEquals(1001, Intervals.normalizeStart(1001, Type.ZeroOpen));

        assertEquals(1, Intervals.normalizeStart(1, Type.ZeroClosed));
        assertEquals(1001, Intervals.normalizeStart(1001, Type.ZeroClosed));
    }

    /**
     * Test of normalizeEnd method, of class Intervals.
     */
    @Test
    public void testNormalizeEnd() {
        System.out.println("normalizeEnd");
        assertEquals(0, Intervals.normalizeEnd(1, Type.OneOpen));
        assertEquals(1000, Intervals.normalizeEnd(1001, Type.OneOpen));

        assertEquals(1, Intervals.normalizeEnd(1, Type.OneClosed));
        assertEquals(1001, Intervals.normalizeEnd(1001, Type.OneClosed));

        assertEquals(1, Intervals.normalizeEnd(1, Type.ZeroOpen));
        assertEquals(1001, Intervals.normalizeEnd(1001, Type.ZeroOpen));

        assertEquals(2, Intervals.normalizeEnd(1, Type.ZeroClosed));
        assertEquals(1002, Intervals.normalizeEnd(1001, Type.ZeroClosed));
    }

    /**
     * Test of deNormalizeStart method, of class Intervals.
     */
    @Test
    public void testDeNormalizeStart() {
        System.out.println("deNormalizeStart");
        assertEquals(1, Intervals.deNormalizeStart(0, Type.OneOpen));
        assertEquals(1001, Intervals.deNormalizeStart(1000, Type.OneOpen));

        assertEquals(1, Intervals.deNormalizeStart(0, Type.OneClosed));
        assertEquals(1001, Intervals.deNormalizeStart(1000, Type.OneClosed));

        assertEquals(1, Intervals.deNormalizeStart(1, Type.ZeroOpen));
        assertEquals(1001, Intervals.deNormalizeStart(1001, Type.ZeroOpen));

        assertEquals(1, Intervals.deNormalizeStart(1, Type.ZeroClosed));
        assertEquals(1001, Intervals.deNormalizeStart(1001, Type.ZeroClosed));
    }

    /**
     * Test of deNormalizeEnd method, of class Intervals.
     */
    @Test
    public void testDeNormalizeEnd() {
        System.out.println("deNormalizeEnd");
        assertEquals(1, Intervals.deNormalizeEnd(0, Type.OneOpen));
        assertEquals(1001, Intervals.deNormalizeEnd(1000, Type.OneOpen));

        assertEquals(1, Intervals.deNormalizeEnd(1, Type.OneClosed));
        assertEquals(1001, Intervals.deNormalizeEnd(1001, Type.OneClosed));

        assertEquals(1, Intervals.deNormalizeEnd(1, Type.ZeroOpen));
        assertEquals(1001, Intervals.deNormalizeEnd(1001, Type.ZeroOpen));

        assertEquals(1, Intervals.deNormalizeEnd(2, Type.ZeroClosed));
        assertEquals(1001, Intervals.deNormalizeEnd(1002, Type.ZeroClosed));
    }
}
