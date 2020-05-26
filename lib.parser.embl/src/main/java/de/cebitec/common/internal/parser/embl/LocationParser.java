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
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import de.cebitec.common.parser.data.embl.Feature.GenomicInterval;
import de.cebitec.common.parser.data.embl.Feature.Location;
import de.cebitec.common.sequencetools.intervals.Interval;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class LocationParser extends AbstractStringWriter<Location> implements Parser<Location> {

    @Override
    public Location parse(CharSequence data) {
        Location l = new Location();
        Matcher matcher = Pattern.compile("(complement)?\\(?(join)?\\(?([<>0-9.,]+)?\\)?").matcher(data);
        matcher.find();
        l.setComplement("complement".equals(matcher.group(1)));
        String positions = matcher.group(3);
        List<GenomicInterval> list = new LinkedList<>();
        for (String pos : Splitter.on(",").split(positions)) {
            String[] split = pos.split("\\.\\.");
            boolean leftOpen = false;
            boolean rightOpen = false;
            if (split[0].startsWith("<")) {
                leftOpen = true;
                split[0] = split[0].substring(1);
            }
            if (split[1].startsWith(">")) {
                rightOpen = true;
                split[1] = split[1].substring(1);
            }
            list.add(GenomicInterval.createInterval(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Interval.Type.OneClosed, leftOpen, rightOpen));
        }
        l.setLocations(list);
        return l;
    }

    @Override
    public String write(Location data) {
        StringBuilder sb = new StringBuilder();
        if (data.isComplement()) {
            sb.append("complement(");
        }
        if (data.getLocations().size() > 1) {
            sb.append("join(");
        }
        String join = Joiner.on(",").join(Lists.transform(data.getLocations(), new Function<GenomicInterval, String>() {
            @Override
            public String apply(GenomicInterval input) {
                GenomicInterval as = input.as(Interval.Type.OneClosed);
                StringBuilder stringBuilder = new StringBuilder();
                if (as.isLeftopen()) {
                    stringBuilder.append("<");
                }
                stringBuilder.append(as.getStart()).append("..");
                if (as.isRightopen()) {
                    stringBuilder.append(">");
                }
                stringBuilder.append(as.getEnd());

                return stringBuilder.toString();
            }
        }));
        sb.append(join);
        if (data.getLocations().size() > 1) {
            sb.append(")");
        }
        if (data.isComplement()) {
            sb.append(")");
        }
        String s = EmblEntryParser.splitToLineLength(sb.toString(), "[,]", 59);
        return Joiner.on(String.format("\n%-16s", "")).join(Splitter.on("\n").split(s));
    }
}
