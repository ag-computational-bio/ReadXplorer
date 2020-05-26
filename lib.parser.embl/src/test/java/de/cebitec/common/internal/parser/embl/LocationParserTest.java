/*
 * Copyright (C) 2015 Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
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
package de.cebitec.common.internal.parser.embl;

import de.cebitec.common.parser.data.embl.Feature;
import de.cebitec.common.sequencetools.intervals.Interval;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class LocationParserTest {

    @Test
    public void testLocationParser() {
        String text = "complement(join(485..651,706..1096))";
        String text1 = "join(485..651,706..1096)";
        String text2 = "485..651";
        Interval<Integer> i1 = Feature.GenomicInterval.createInterval(485, 651, Interval.Type.OneClosed);
        Interval<Integer> i2 = Feature.GenomicInterval.createInterval(706, 1096, Interval.Type.OneClosed);

        LocationParser p = new LocationParser();
        Feature.Location parse = p.parse(text);
        assertThat(parse.isComplement(), is(true));
        assertThat(parse.getLocations(), contains(i1, i2));
        assertThat(p.write(parse), equalTo(text));

        Feature.Location parse1 = p.parse(text1);
        assertThat(parse1.isComplement(), is(false));
        assertThat(parse1.getLocations(), contains(i1, i2));
        assertThat(p.write(parse1), equalTo(text1));

        Feature.Location parse2 = p.parse(text2);
        assertThat(parse2.isComplement(), is(false));
        assertThat(parse2.getLocations(), contains(i1));
        assertThat(p.write(parse2), equalTo(text2));

        Interval<Integer> i3 = Feature.GenomicInterval.createInterval(485, 651, Interval.Type.OneClosed, true, false);
        Interval<Integer> i4 = Feature.GenomicInterval.createInterval(485, 651, Interval.Type.OneClosed, false, true);
        Interval<Integer> i5 = Feature.GenomicInterval.createInterval(706, 1096, Interval.Type.OneClosed, false, true);

        String text4 = "complement(join(485..651,706..>1096))";
        Feature.Location parse4 = p.parse(text4);
        assertThat(parse4.isComplement(), is(true));
        assertThat(parse4.getLocations(), contains(i1, i5));
        assertThat(p.write(parse4), equalTo(text4));

        String text5 = "complement(join(<485..651,706..1096))";
        Feature.Location parse5 = p.parse(text5);
        assertThat(parse5.isComplement(), is(true));
        assertThat(parse5.getLocations(), contains(i3, i2));
        assertThat(p.write(parse5), equalTo(text5));

        String text6 = "join(485..651,706..>1096)";
        Feature.Location parse6 = p.parse(text6);
        assertThat(parse6.isComplement(), is(false));
        assertThat(parse6.getLocations(), contains(i1, i5));
        assertThat(p.write(parse6), equalTo(text6));

        String text7 = "join(<485..651,706..1096)";
        Feature.Location parse7 = p.parse(text7);
        assertThat(parse7.isComplement(), is(false));
        assertThat(parse7.getLocations(), contains(i3, i2));
        assertThat(p.write(parse7), equalTo(text7));

        String text8 = "485..>651";
        Feature.Location parse8 = p.parse(text8);
        assertThat(parse8.isComplement(), is(false));
        assertThat(parse8.getLocations(), contains(i4));
        assertThat(p.write(parse8), equalTo(text8));

        String text9 = "<485..651";
        Feature.Location parse9 = p.parse(text9);
        assertThat(parse9.isComplement(), is(false));
        assertThat(parse9.getLocations(), contains(i3));
        assertThat(p.write(parse9), equalTo(text9));
    }

}
