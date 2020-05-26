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
import de.cebitec.common.parser.data.embl.Keywords;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class KeywordsParser extends AbstractStringWriter<Keywords> implements Parser<Keywords> {

    @Override
    public Keywords parse(CharSequence data) {
        Keywords kw = new Keywords();
        List<String> list = new LinkedList<>();
        for (String keyword : Splitter.on("; ").trimResults(CharMatcher.is('.')).omitEmptyStrings().split(data)) {
            list.add(keyword);
        }
        kw.setKeywords(list);
        return kw;
    }

    @Override
    public String write(Keywords data) {
        return Joiner.on("; ").join(data.getKeywords()) + ".";
    }

}
