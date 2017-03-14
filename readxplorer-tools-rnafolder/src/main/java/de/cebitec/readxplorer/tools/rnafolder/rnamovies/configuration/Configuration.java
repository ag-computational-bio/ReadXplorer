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


import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class Configuration {

    private final Map<String, Category> categories = Collections.synchronizedMap( new LinkedHashMap<String, Category>() );


    public Configuration( InputStream in ) throws IOException, SAXException {
        XMLReader parser;
        ContentHandler handler;

        parser = XMLReaderFactory.createXMLReader();
        handler = new ConfigXMLHandler( categories );

        parser.setContentHandler( handler );
        parser.parse( new InputSource( in ) );
    }


    public void initAll() {
        Iterator<Category> cats;
        for( cats = categories.values().iterator(); cats.hasNext(); ) {
            cats.next().init();
        }
    }


    public void addConfigListener( ConfigListener listener ) {
        Iterator<Category> cats;
        for( cats = categories.values().iterator(); cats.hasNext(); ) {
            cats.next().addConfigListener( listener );
        }
    }


    public Map<String, Category> getCategories() {
        return Collections.unmodifiableMap( categories );
    }


    public Category getCategory( String name ) {
        if( !categories.containsKey( name ) )
            throw new NoSuchElementException( "No such configuration category " + name + "." );

        return categories.get( name );
    }


}
