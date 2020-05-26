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

import static de.cebitec.common.sequencetools.intervals.IdentifyIntervalsFromMemberlist.*;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static de.cebitec.common.sequencetools.intervals.Intervals.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IdentifyIntervalsFromMemberlistTest {

    private List<Member<String>> list;

    @Before
    public void initTestData() {
        list = new LinkedList<>();
        list.add(new DefaultMember<>("s1", createInterval(0, 2)));
        list.add(new DefaultMember<>("s2", createInterval(2, 4)));
        list.add(new DefaultMember<>("s3", createInterval(4, 6)));
        list.add(new DefaultMember<>("s4", createInterval(6, 9)));
    }

    @Test
    public void testIdentifySubintervalsSingleCompleteInterval() {
        assertThat(identifySubintervals(list, createInterval(0, 2)),
                   hasItems(new MemberResult<>("s1", createInterval(0, 2))));
    }

    @Test
    public void testIdentifySubintervalsMultipleCompleteInterval() {
        assertThat(identifySubintervals(list, createInterval(0, 4)),
                   hasItems(new MemberResult<>("s1", createInterval(0, 2)),
                            new MemberResult<>("s2", createInterval(0, 2)) // local coordinates        
        ));
    }

    @Test
    public void testIdentifySubintervalsAllMembers() {
        assertThat(identifySubintervals(list, createInterval(0, 9)),
                   hasItems(new MemberResult<>("s1", createInterval(0, 2)),
                            new MemberResult<>("s2", createInterval(0, 2)),
                            new MemberResult<>("s3", createInterval(0, 2)),
                            new MemberResult<>("s4", createInterval(0, 3))));
    }

    @Test
    public void testIdentifySubintervalsEmtpyInterval() {
        assertThat(identifySubintervals(list, createInterval(4, 4)),
                   hasItems(new MemberResult<>("s3", createInterval(0, 0))));
    }

    @Test
    public void testIdentifySubintervalsEmtpyIntervalAtEnd() {
        assertThat(identifySubintervals(list, createInterval(9, 9)),
                   hasItems(new MemberResult<>("s4", createInterval(3, 3))));
    }

    @Test
    public void testIdentifySubintervalsEmtpyIntervalAtBeginning() {
        List<MemberResult<String>> identifySubintervals = identifySubintervals(list, createInterval(0, 0));
        MemberResult<String> expected = new MemberResult<>("s1", createInterval(0, 0));
        assertThat(identifySubintervals,
                   hasItems(expected));
    }

    @Test
    public void testIdentifySubintervalsMultipleInternalIntervals() {
        assertThat(identifySubintervals(list, createInterval(5, 8)),
                   hasItems(new MemberResult<>("s3", createInterval(1, 2)),
                            new MemberResult<>("s4", createInterval(0, 2))));
    }

    @Test
    public void testIdentifySubintervalsUnavailableInterval() {
        assertThat(identifySubintervals(list, createInterval(9, 20)),
                   is(empty()));
    }
}
