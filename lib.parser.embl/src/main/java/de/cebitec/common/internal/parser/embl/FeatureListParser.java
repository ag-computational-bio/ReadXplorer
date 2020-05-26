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
import com.google.common.base.Splitter;
import com.google.common.collect.LinkedListMultimap;
import de.cebitec.common.parser.data.embl.Feature.Qualifier;
import de.cebitec.common.internal.parser.embl.FeatureQualifierParser.SimpleEntry;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class FeatureListParser extends AbstractStringWriter<List<Feature>> implements Parser<List<Feature>> {

    private static FeatureHeaderParser fp = new FeatureHeaderParser();
    private static FeatureQualifierParser qp = new FeatureQualifierParser();

    private void processQualifiers(Feature f, StringBuilder sb) {
        if (f.getQualifiers() == null) {
            LinkedListMultimap<Feature.Qualifier, Object> create = LinkedListMultimap.create();
            f.setQualifiers(create);
        }
        if (sb != null && sb.length() > 0) {
            Entry<Qualifier, ?> parse = qp.parse(sb.toString());
            f.getQualifiers().put(parse.getKey(), parse.getValue());
            sb.delete(0, sb.length());
        }
    }

    private void processPart(Feature f, StringBuilder sb) {
        if (isHeader(sb.toString())) {
            f.setHeader(fp.parse(sb.toString()));
            sb.delete(0, sb.length());
        } else if (isQualifier(sb.toString())) {
            processQualifiers(f, sb);
        }
    }

    @Override
    public List<Feature> parse(CharSequence data) {
        List<Feature> list = new LinkedList<>();
        Feature f = null;
        StringBuilder sb = new StringBuilder();

        // collect lines until new qualifier or new header

        for (String line : Splitter.on("\n").omitEmptyStrings().split(data)) {
            if (isQualifier(line)) {
                processPart(f, sb);
            } else if (isHeader(line)) {
                if (f != null) {
                    processPart(f, sb);
                    list.add(f);
                }
                f = new Feature();
            }
            if (sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(line.trim());
        }
        if (f != null) {
            processPart(f, sb);
            list.add(f);
        }
        return list;
    }

    @Override
    public String write(List<Feature> data) {
        StringBuilder sb = new StringBuilder();
        for (Feature feature : data) {
            sb.append(fp.write(feature.getHeader())).append("\n");
            if (feature.getQualifiers() != null) {
                for (Entry<Qualifier, Collection<Object>> entry : feature.getQualifiers().asMap().entrySet()) {
                    for (Object o : entry.getValue()) {
                        SimpleEntry<Qualifier, Object> simpleEntry = new FeatureQualifierParser.SimpleEntry<>(entry.getKey(), o);
                        sb.append(qp.write(simpleEntry)).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    private boolean isQualifier(String line) {
        Pattern p = Pattern.compile("/\\w+=");
        Matcher matcher = p.matcher(line);
        return matcher.find();
    }

    private boolean isHeader(String line) {
        return line.matches("\\w+\\s+[complentji()<>0-9., ]+");
    }
}
