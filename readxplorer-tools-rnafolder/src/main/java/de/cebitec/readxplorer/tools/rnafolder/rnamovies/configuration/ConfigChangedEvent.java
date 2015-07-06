/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies.configuration;


import java.util.EventObject;


public class ConfigChangedEvent extends EventObject {

    private final int id;

    private final String key;

    private final Object value;


    public ConfigChangedEvent( Object source, int id, String key, Object value ) {
        super( source );
        this.id = id;
        this.key = key;
        this.value = value;
    }


    public int getId() {
        return id;
    }


    public String getKey() {
        return key;
    }


    public Object getValue() {
        return value;
    }


}
