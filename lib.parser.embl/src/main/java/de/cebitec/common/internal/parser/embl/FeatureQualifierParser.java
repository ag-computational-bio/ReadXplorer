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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import de.cebitec.common.internal.parser.common.AbstractStringWriter;
import de.cebitec.common.internal.parser.common.Parser;
import de.cebitec.common.parser.data.embl.AntiCodon;
import de.cebitec.common.parser.data.embl.Feature.Qualifier;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class FeatureQualifierParser extends AbstractStringWriter<Map.Entry<Qualifier,?>> implements Parser<Map.Entry<Qualifier, ?>> {

    public static class SimpleEntry<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public Entry<Qualifier, ?> parse(CharSequence data) {
        String textToParse = data.toString().replaceAll("\\s+", " ");

        int indexOf = textToParse.indexOf("=");
        if (indexOf < 0) {
            System.out.println("data = " + data);
        }
        String key = textToParse.substring(0, indexOf).trim().replaceFirst("/", "");
        String value = textToParse.substring(indexOf + 1);
        Object val = null;
        if (!value.startsWith("\"")) {

            if (value.matches("^\\d+$")) {
                val = Integer.parseInt(value);
            } else if (value.matches(AntiCodonParser.anticodonPattern)) {
                AntiCodonParser parser = new AntiCodonParser();
                AntiCodon parse = parser.parse(data);
                val = parse;
            }
        } else {
            String substring = value.substring(1, value.length() - 1);
            if ("translation".equals(key)) {
                substring = substring.replaceAll("\\s+", "");
            }
            val = substring;
        }
        return new SimpleEntry<>(Qualifier.valueOf(key), val);
    }

    @Override
    public String write(Entry<Qualifier, ?> data) {
        int linewidth = 59;
        if (data.getValue() instanceof String) {
            String format = String.format("/%s=\"%s\"", data.getKey(), data.getValue());
            String text = EmblEntryParser.splitToLineLength(format, "[; \\-,]+", linewidth);
            Iterable<String> split = Splitter.on("\n").split(text);
            String out = String.format("%-16s", "") + Joiner.on(String.format("\n%-16s", "")).join(split);
            return out;
        } else if (data.getValue() instanceof AntiCodon) {
            AntiCodonParser parser = new AntiCodonParser();
            return String.format("%-16s/%s=%s", "", data.getKey(), parser.write((AntiCodon) data.getValue()));
        } else {
            return String.format("%-16s/%s=%s", "", data.getKey(), data.getValue());
        }
    }
}
