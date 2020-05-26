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

import de.cebitec.common.parser.data.embl.AntiCodon;
import de.cebitec.common.parser.data.embl.Feature;
import de.cebitec.common.sequencetools.intervals.Interval;
import java.util.Arrays;
import java.util.Map;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class FeatureQualifierParserTest {

    @Test
    public void testFeatureQualifierParser() {
        String text = "                /codon_start=1";
        String text2 = "                /locus_tag=\"ACP_1055\"";

        FeatureQualifierParser p = new FeatureQualifierParser();
        Map.Entry<Feature.Qualifier, ?> parse = p.parse(text);
        assertThat(parse.getKey(), equalTo(Feature.Qualifier.codon_start));
        assertThat((Integer) parse.getValue(), equalTo(1));
        assertThat(p.write(parse), equalTo(text));

        Map.Entry<Feature.Qualifier, ?> parse2 = p.parse(text2);
        assertThat(parse2.getKey(), equalTo(Feature.Qualifier.locus_tag));
        assertThat((String) parse2.getValue(), equalTo("ACP_1055"));
        assertThat(p.write(parse2), equalTo(text2));

        String text3 = "                /experiment=\"experimental evidence, no additional details\n"
            + "                recorded\"";
        Map.Entry<Feature.Qualifier, ?> parse3 = p.parse(text3);
        assertThat(parse3.getKey(), equalTo(Feature.Qualifier.experiment));
        assertThat((String) parse3.getValue(), equalTo("experimental evidence, no additional details recorded"));
        assertThat(p.write(parse3), equalTo(text3));

        String text4 = "                /anticodon=(pos:complement(14208..14206),aa:Trp)";
        Map.Entry<Feature.Qualifier, ?> parse4 = p.parse(text4);
        assertThat(parse4.getKey(), equalTo(Feature.Qualifier.anticodon));
        AntiCodon ac = (AntiCodon) parse4.getValue();
        assertThat(ac.getAminoacid(), equalTo("Trp"));
        Feature.Location l = new Feature.Location();
        l.setComplement(true);
        l.setLocations(Arrays.asList(Feature.GenomicInterval.createInterval(14208, 14206, Interval.Type.OneClosed)));
        assertThat(ac.getLocation(), equalTo(l));
        assertThat(p.write(parse4), equalTo(text4));

    }
}
