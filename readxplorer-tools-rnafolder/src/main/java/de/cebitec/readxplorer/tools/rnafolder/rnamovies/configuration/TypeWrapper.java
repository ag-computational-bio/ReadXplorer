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


import java.util.HashMap;
import java.util.Map;


public class TypeWrapper {

    private Object obj;
    private final Map<String, String> attributes;


    public TypeWrapper( Object obj ) {
        this.obj = obj;
        attributes = new HashMap<>();
    }


    public Object getObject() {
        return obj;
    }


    public void setObject( Object obj ) {
        this.obj = obj;
    }


    public void putAttribute( String key, String value ) {
        attributes.put( key, value );
    }


    public String getAttribute( String key ) {
        return attributes.get( key );
    }


    public boolean contains( String key ) {
        return attributes.containsKey( key );
    }


}
