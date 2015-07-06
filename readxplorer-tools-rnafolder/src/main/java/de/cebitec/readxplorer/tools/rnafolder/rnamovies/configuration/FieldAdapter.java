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


import java.lang.reflect.Field;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;


public class FieldAdapter implements ConfigListener {

    private static final Logger LOG = Logger.getLogger( FieldAdapter.class.getName() );

    private final Object configurable;


    public FieldAdapter( Object configurable ) {
        this.configurable = configurable;
    }


    @Override
    public void configurationChanged( ConfigChangedEvent e ) {
        String name;
        Object value;
        Field field;

        name = e.getKey();
        value = e.getValue();

        try {
            field = configurable.getClass().getDeclaredField( name );
            if( value instanceof Boolean ) {
                field.setBoolean( configurable, ((Boolean) value) );
            } else if( value instanceof Integer ) {
                field.setInt( configurable, ((Integer) value) );
            } else if( value instanceof Float ) {
                field.setFloat( configurable, ((Float) value) );
            } else if( value instanceof BoundedRangeModel ) {
                field.setInt( configurable, ((BoundedRangeModel) e.getValue()).getValue() );
            } else {
                field.set( configurable, value );
            }
        } catch( NoSuchFieldException ex ) {
            LOG.severe( "No such field: ".concat( ex.getMessage() ) );
        } catch( IllegalAccessException ex ) {
            LOG.severe( ex.getMessage() );
        }
    }


}
