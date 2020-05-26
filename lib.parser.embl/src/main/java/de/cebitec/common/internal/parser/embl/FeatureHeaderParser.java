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
package de.cebitec.common.internal.parser.embl;

import de.cebitec.common.internal.parser.common.Parser;
import de.cebitec.common.internal.parser.common.AbstractStringWriter;
import de.cebitec.common.parser.data.embl.Feature;
import de.cebitec.common.parser.data.embl.Feature.FeatureHeader;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class FeatureHeaderParser extends AbstractStringWriter<FeatureHeader>  implements Parser<FeatureHeader> {
    public static LocationParser lp = new LocationParser();

    @Override
    public FeatureHeader parse(CharSequence data) {
        FeatureHeader fh = new FeatureHeader();
        String[] split = data.toString().trim().replaceAll("\\s+", " ").split(" ",2);
        fh.setKey(Feature.FeatureKey.fromString(split[0]));
        fh.setLocation(lp.parse(split[1].replaceAll(" ", "")));
        return fh;
    }

    @Override
    public String write(FeatureHeader data) {
        return String.format("%-16s%s", data.getKey(), lp.write(data.getLocation()));
    }

}
