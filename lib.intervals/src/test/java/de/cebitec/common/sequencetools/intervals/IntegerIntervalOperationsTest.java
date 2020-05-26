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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IntegerIntervalOperationsTest {

    IntegerIntervalOperations iio = new IntegerIntervalOperations();

    /**
     * Test of union method, of class IntegerIntervalOperations.
     */
    @Test
    public void testUnion() {
        
        
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnionNonOverlappingNonAdjacent() {
        iio.union(Intervals.createInterval(0, 1, Interval.Type.OneClosed),
                Intervals.createInterval(3, 5, Interval.Type.OneClosed));
    }

//    /**
//     * Test of intersection method, of class IntegerIntervalOperations.
//     */
//    @Test
//    public void testIntersection() {
//    }
//
//    @Test
//    public void testIntersectionNonOverlappingNonAdjacent() {
//        Interval<Integer> intersection = iio.intersection(Intervals.createInterval(0, 1, Interval.Type.OneClosed),
//                                                 Intervals.createInterval(3, 5, Interval.Type.OneClosed));
//        
//        assertThat(intersection.isEmpty(), is(true));
//    }
//    
//    /**
//     * Test of complement method, of class IntegerIntervalOperations.
//     */
//    @Test
//    public void testComplement() {
//        
//    }
}