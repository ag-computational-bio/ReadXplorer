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
import de.cebitec.common.parser.data.embl.Description;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class DescriptionParser extends AbstractStringWriter<Description> implements Parser<Description> {

    @Override
    public Description parse(CharSequence data) {
        Description d = new Description();
        d.setDescription(data.toString());
        return d;
    }

    @Override
    public String write(Description data) {
        return data.getDescription();
    }

}
