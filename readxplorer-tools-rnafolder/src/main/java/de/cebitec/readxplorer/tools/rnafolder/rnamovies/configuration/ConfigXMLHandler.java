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


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


public class ConfigXMLHandler implements ContentHandler {

    private static final Logger LOG = LoggerFactory.getLogger( ConfigXMLHandler.class.getName() );


    /**
     * The 3 different states the parser can be in
     */
    private static final Integer MODE_CATEGORY = 0;
    private static final Integer MODE_VALUE = 1;
    private static final Integer MODE_CONSTRUCTOR = 2;

    private final Map<String, Category> cats;
    private Map<String, TypeWrapper> vals;

    private final Stack<Attributes> lastAtts;
    private final Stack<StringBuffer> lastChars;
    private final Stack<Integer> lastMode;
    private final Stack<List<Class<?>>> lastCons;
    private final Stack<List<Object>> lastArgs;

    private Object value = null;

    private int mode = MODE_CATEGORY;


    public ConfigXMLHandler( Map<String, Category> cats ) {

        this.cats = cats;

        // init Stacks
        lastChars = new Stack<>();
        lastAtts = new Stack<>();
        lastMode = new Stack<>();
        lastCons = new Stack<>();
        lastArgs = new Stack<>();
    }


    @Override
    public void startElement( String uri,
                              String localName,
                              String qName,
                              Attributes atts )
            throws SAXException {

        lastAtts.push( new AttributesImpl( atts ) );
        lastChars.push( new StringBuffer() );

        if( qName.equalsIgnoreCase( "category" ) ) {
            lastMode.push( mode );
            mode = MODE_CATEGORY;
            vals = new LinkedHashMap<>();
        } else if( qName.equalsIgnoreCase( "value" ) ) {
            lastMode.push( mode );
            mode = MODE_VALUE;
            value = null;
        } else if( qName.equalsIgnoreCase( "object" ) ) {
            lastMode.push( mode );
            mode = MODE_CONSTRUCTOR;
            lastCons.push( new ArrayList<Class<?>>( 5 ) );
            lastArgs.push( new ArrayList<>( 5 ) );
        }
    }


    @Override
    public void endElement( String uri, String localName, String qName )
            throws SAXException {

        final Attributes atts = lastAtts.pop();
        String text = lastChars.pop().toString();
        if( qName.equalsIgnoreCase( "category" ) ) {
            mode = lastMode.pop();

            String key = atts.getValue( "key" );
            key = key == null ? "unnamed" : key;
            int id;
            try {
                text = atts.getValue( "id" );
                id = text == null ? -1 : Integer.parseInt( text );
                id = id < -1 ? -1 : id;
            } catch( NumberFormatException e ) {
                LOG.warn( "Cannot convert {0} to java.lang.Integer.", e.getMessage() );
                id = -1;
            }

            //log.info("Adding new category: "+key+", with "+vals.size()+" values");
            cats.put( key, new Category( id, key, vals ) );
        } else if( qName.equalsIgnoreCase( "value" ) ) {
            mode = lastMode.pop();
            final String key = atts.getValue( "key" );

            if( value != null ) {
                TypeWrapper tw = new TypeWrapper( value );

                for( int i = 0; i < atts.getLength(); i++ ) {
                    tw.putAttribute( atts.getQName( i ), atts.getValue( i ) );
                }

                //log.info("Adding new value: " + key);
                vals.put( key == null ? "unnamed" : key, tw );
            }

        } else if( qName.equalsIgnoreCase( "object" ) ) {
            mode = lastMode.pop();

            final Class<?>[] paramTypes = lastCons.pop().toArray( new Class<?>[]{} );
            final Object[] initArgs = lastArgs.pop().toArray( new Object[]{} );

            try {
                Class<?> class_ = Class.forName( atts.getValue( "class" ) );
                Object obj_ = class_.getConstructor( paramTypes ).newInstance( initArgs );

                //log.info("Loaded "+obj_.getClass().getName()+": "+obj_.toString());

                if( mode == MODE_CONSTRUCTOR ) {
                    if( lastCons.empty() || lastArgs.empty() ) {
                        return;
                    }

                    lastCons.peek().add( class_ );
                    lastArgs.peek().add( obj_ );
                } else if( mode == MODE_VALUE ) {
                    value = obj_;
                }
            } catch( NoSuchMethodException e ) {
                LOG.warn( "Could not find Constructor: {0}", e.getMessage() );
            } catch( InstantiationException e ) {
                LOG.warn( "Could not instantiate: {0}", e.getMessage() );
            } catch( IllegalAccessException | java.lang.reflect.InvocationTargetException e ) {
                LOG.warn( e.getMessage() );
            } catch( ClassNotFoundException e ) {
                LOG.warn( "Could not find class: {0}", e.getMessage() );
            }
        } else if( qName.equalsIgnoreCase( "string" ) ) {
            if( mode == MODE_CONSTRUCTOR ) {
                lastCons.peek().add( text.getClass() );
                lastArgs.peek().add( text );
            } else if( mode == MODE_VALUE ) {
                value = text;
            }
        } else if( qName.equalsIgnoreCase( "int" ) ) {
            Integer ival;
            try {
                ival = new Integer( text );
                if( mode == MODE_CONSTRUCTOR ) {
                    lastCons.peek().add( Integer.TYPE );
                    lastArgs.peek().add( ival );
                } else if( mode == MODE_VALUE ) {
                    value = ival;
                }
            } catch( NumberFormatException e ) {
                LOG.warn( "Cannot convert {0} to java.lang.Integer.", e.getMessage() );
            }
        } else if( qName.equalsIgnoreCase( "float" ) ) {
            Float fval;
            try {
                fval = new Float( text );
                if( mode == MODE_CONSTRUCTOR ) {
                    lastCons.peek().add( Float.TYPE );
                    lastArgs.peek().add( fval );
                } else if( mode == MODE_VALUE ) {
                    value = fval;
                }
            } catch( NumberFormatException e ) {
                LOG.warn( "Cannot convert {0} to java.lang.Float.", e.getMessage() );
            }
        } else if( qName.equalsIgnoreCase( "boolean" ) ) {
            Boolean bval;
            bval = Boolean.valueOf( text );
            if( mode == MODE_CONSTRUCTOR ) {
                lastCons.peek().add( Boolean.TYPE );
                lastArgs.peek().add( bval );
            } else if( mode == MODE_VALUE ) {
                value = bval;
            }
        }
    }


    @Override
    public void setDocumentLocator( Locator locator ) {
    }


    @Override
    public void startDocument()
            throws SAXException {
        LOG.info( "Loading configuration..." );
    }


    @Override
    public void endDocument()
            throws SAXException {
        LOG.info( "Configuration successfully loaded." );
    }


    @Override
    public void characters( char[] ch, int start, int length ) throws SAXException {
        lastChars.peek().append( ch, start, length );
    }


    @Override
    public void ignorableWhitespace( char[] ch, int start, int length )
            throws SAXException {
    }


    @Override
    public void processingInstruction( String target, String data )
            throws SAXException {
    }


    @Override
    public void skippedEntity( String name )
            throws SAXException {
    }


    @Override
    public void startPrefixMapping( String prefix, String uri )
            throws SAXException {
    }


    @Override
    public void endPrefixMapping( String prefix )
            throws SAXException {
    }


}
