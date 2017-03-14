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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies.util;


import java.lang.reflect.Constructor;
import java.util.Stack;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


public class ActionXMLHandler implements ContentHandler {

    private static final Logger LOG = LoggerFactory.getLogger( ActionXMLHandler.class.getName() );

    private JMenuBar jmb;
    private JToolBar jtb;
    private Class<?>[] consTypes;
    private Object[] consObjs;

    private Stack<Attributes> lastAtts = new Stack<>();
    private Stack<StringBuffer> lastChars = new Stack<>();

    private Stack<JMenu> menuPath = new Stack<>();


    public ActionXMLHandler( ActionContainer ac ) {
        this( ac, new Class<?>[]{}, new Object[]{} );
    }


    public ActionXMLHandler( ActionContainer ac,
                             Class<?>[] consTypes,
                             Object[] consObjs ) {
        this.consTypes = consTypes;
        this.consObjs = consObjs;
        this.jmb = ac.getMenuBar();
        this.jtb = ac.getToolBar();
    }


    @Override
    public void startElement( String uri,
                              String localName,
                              String qName,
                              Attributes atts )
            throws SAXException {
        String name;

        lastAtts.push( new AttributesImpl( atts ) );
        lastChars.push( new StringBuffer() );

        if( qName.equalsIgnoreCase( "menu" ) ) {
            name = atts.getValue( "name" );
            menuPath.push( new JMenu( name == null ? "Unnamed" : name ) );
        } else if( qName.equalsIgnoreCase( "separator" ) ) {
            if( !menuPath.empty() ) {
                menuPath.peek().add( new JSeparator() );
            }
        }
    }


    @Override
    public void endElement( String uri, String localName, String qName )
            throws SAXException {

        Attributes atts = lastAtts.pop();
        lastChars.pop();

        if( qName.equalsIgnoreCase( "menu" ) ) {
            JMenu menu = menuPath.pop();

            String mnemonic = atts.getValue( "mnemonic" );
            if( mnemonic != null && mnemonic.length() > 0 ) {
                menu.setMnemonic( mnemonic.charAt( 0 ) );
            }

            if( menuPath.empty() ) {
                jmb.add( menu );
            } else {
                menuPath.peek().add( menu );
            }

        } else if( qName.equalsIgnoreCase( "action" ) ) {

            try {
                Class<?> class_ = Class.forName( atts.getValue( "class" ) );
                Constructor<?> cons_ = class_.getConstructor( consTypes );
                Object obj_ = cons_.newInstance( consObjs );

                if( !menuPath.empty() && obj_ instanceof AbstractAction ) {
                    JMenuItem item = new JMenuItem( (Action) obj_ );
                    item.setIcon( null );

                    String key = atts.getValue( "key" );
                    if( key != null && key.length() > 0 ) {
                        try {
                            int modifier = Integer.parseInt( atts.getValue( "modifier" ) );
                            KeyStroke accelerator = KeyStroke.getKeyStroke( key.charAt( 0 ), modifier );
                            item.setAccelerator( accelerator );
                        } catch( NumberFormatException e ) {
                            LOG.warn( e.getMessage().concat( " is not a valid int." ) );
                        }
                    }
                    menuPath.peek().add( item );
                }

                String toolBar;
                if( (toolBar = atts.getValue( "toolBar" )) != null &&
                    (Boolean.valueOf( toolBar )) &&
                    obj_ instanceof AbstractAction ) {
                    jtb.add( (Action) obj_ );
                }

            } catch( NoSuchMethodException e ) {
                LOG.warn( "Could not find Constructor: ".concat( e.getMessage() ) );
            } catch( InstantiationException e ) {
                LOG.warn( "Could not instantiate: ".concat( e.getMessage() ) );
            } catch( IllegalAccessException | java.lang.reflect.InvocationTargetException e ) {
                LOG.warn( e.getMessage() );
            } catch( ClassNotFoundException e ) {
                LOG.warn( "Could not find class: ".concat( e.getMessage() ) );
            }
        }
    }


    @Override
    public void setDocumentLocator( Locator locator ) {
    }


    @Override
    public void startDocument()
            throws SAXException {
        LOG.info( "Loading actions..." );
    }


    @Override
    public void endDocument()
            throws SAXException {
        LOG.info( "Actions successfully loaded." );
    }


    @Override
    public void characters( char[] ch, int start, int length )
            throws SAXException {
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
