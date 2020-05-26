/*
 * Copyright (C) 2014 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.common.internal.parser.common;

/**
 * @param <T> Type of object to parse and write
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public abstract class ConfigurableParserAndWriter<T> extends AbstractStringWriter<T> implements Parser<T> {
    
    public boolean laxParser = false;

    /**
     * @return <code>true</code> if the parser shall run in lax parsing and
     * writing mode, <code>false</code> <code>false</code> if the parser shall
     * run in validation mode and only accept correct entries.
     * <code>false</code> is the default value.
     */
    public boolean isLaxParser() {
        return laxParser;
    }

    /**
     * @param laxParser Set <code>true</code> if the parser shall run in lax
     * parsing and writing mode, set <code>false</code> if the parser shall run
     * in validation mode and only accept correct entries. <code>false</code> is
     * the default value.
     */
    public void setLaxParser(boolean laxParser) {
        this.laxParser = laxParser;
    }
    
}
