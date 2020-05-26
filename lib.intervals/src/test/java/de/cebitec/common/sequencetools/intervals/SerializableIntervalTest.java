/*
 * Copyright (C) 2014 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
import static de.cebitec.common.sequencetools.intervals.Intervals.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class SerializableIntervalTest {

    @Test
    public void testEqualsEmptyIntervals() {
        assertThat(createInterval(0, 0), equalTo(createInterval(0, 0)));
    }
    
    @Test
    public void testEqualsEmptyIntervals2() {
        assertThat(createInterval(1, 1), equalTo(createInterval(1, 1)));
    }

}
