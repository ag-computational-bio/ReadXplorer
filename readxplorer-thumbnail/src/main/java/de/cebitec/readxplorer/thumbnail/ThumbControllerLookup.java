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

package de.cebitec.readxplorer.thumbnail;


import java.util.Collection;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 *
 * @author denis
 */
public class ThumbControllerLookup extends AbstractLookup {

    private static final long serialVersionUID = 1L;

    private InstanceContent content = null;
    private static ThumbControllerLookup def = new ThumbControllerLookup();


    public ThumbControllerLookup( InstanceContent content ) {
        super( content );
        this.content = content;
    }


    public ThumbControllerLookup() {
        this( new InstanceContent() );
    }


    public void add( Object instance ) {
        content.add( instance );
    }


    public void remove( Object instance ) {
        content.remove( instance );
    }


    public <T> void removeAll( Class<T> clazz ) {
        Collection<? extends T> col = lookupAll( clazz );
        for( T o : col ) {
            remove( o );
        }
    }


    public static ThumbControllerLookup getDefault() {
        return def;
    }


}
