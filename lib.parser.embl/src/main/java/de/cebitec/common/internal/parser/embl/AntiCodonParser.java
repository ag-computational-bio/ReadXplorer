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
import de.cebitec.common.parser.data.embl.AntiCodon;
import de.cebitec.common.parser.data.embl.Feature;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class AntiCodonParser extends AbstractStringWriter<AntiCodon> implements Parser<AntiCodon> {

    public static final String anticodonPattern = "\\(pos:(.+),aa:(\\w+)\\)";

    @Override
    public AntiCodon parse(CharSequence data) {
        Pattern pattern = Pattern.compile(anticodonPattern);
        Matcher matcher = pattern.matcher(data);
        matcher.find();
        String locationString = matcher.group(1);
        LocationParser parser = new LocationParser();
        Feature.Location location = parser.parse(locationString);
        String aminoacidString = matcher.group(2);
        AntiCodon ac = new AntiCodon();
        ac.setLocation(location);
        ac.setAminoacid(aminoacidString);
        return ac;
    }

    @Override
    public String write(AntiCodon data) {
        LocationParser lp = new LocationParser();
        return String.format("(pos:%s,aa:%s)", lp.write(data.getLocation()), data.getAminoacid());
    }
}
