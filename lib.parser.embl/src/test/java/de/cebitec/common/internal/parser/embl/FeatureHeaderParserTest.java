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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek - Lukas.Jelonek at computational.bio.uni-giessen.de
 */
public class FeatureHeaderParserTest {

    @Test
    public void testFeatureHeaderParser() {
        String text = "gene            7314..9761";
        String text2 = "CDS             complement(join(1216354..1217437,1217439..1217509))";

        FeatureHeaderParser p = new FeatureHeaderParser();
        LocationParser lp = new LocationParser();

        Feature.FeatureHeader parse = p.parse(text);
        Feature.Location location = lp.parse("7314..9761");
        assertThat(parse.getKey(), equalTo(Feature.FeatureKey.gene));
        assertThat(parse.getLocation(), equalTo(location));
        assertThat(p.write(parse), equalTo(text));

        Feature.FeatureHeader parse2 = p.parse(text2);
        Feature.Location location2 = lp.parse("complement(join(1216354..1217437,1217439..1217509))");
        assertThat(parse2.getKey(), equalTo(Feature.FeatureKey.CDS));
        assertThat(parse2.getLocation(), equalTo(location2));
        assertThat(p.write(parse2), equalTo(text2));
    }
}
