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
import de.cebitec.common.parser.data.embl.OrganismClassification;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class OrganismClassificationParser extends AbstractStringWriter<OrganismClassification> implements Parser<OrganismClassification> {

    @Override
    public OrganismClassification parse(CharSequence data) {
        OrganismClassification oc = new OrganismClassification();
        oc.setClassification(Lists.newArrayList(Splitter.on("; ").omitEmptyStrings().trimResults(CharMatcher.is('.')).split(data)));
        return oc;
    }

    @Override
    public String write(OrganismClassification data) {
        return Joiner.on("; ").join(data.getClassification()) + ".";
    }

}
