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
package de.cebitec.readXplorer.tools.rnaFolder.rnamovies.util;


import java.lang.reflect.Constructor;
import java.util.Stack;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


public class ActionXMLHandler implements ContentHandler {

    private static final Logger log = Logger.getLogger( "ActionXMLHandler" );

    private JMenuBar jmb;
    private JToolBar jtb;
    private Class[] consTypes;
    private Object[] consObjs;

    private Stack<Attributes> lastAtts = new Stack<Attributes>();
    private Stack<StringBuffer> lastChars = new Stack<StringBuffer>();

    private Stack<JMenu> menuPath = new Stack<JMenu>();


    public ActionXMLHandler( ActionContainer ac ) {
        this( ac, new Class[]{}, new Object[]{} );
    }


    public ActionXMLHandler( ActionContainer ac,
                             Class[] consTypes,
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
        }
        else if( qName.equalsIgnoreCase( "separator" ) ) {
            if( !menuPath.empty() )
                menuPath.peek().add( new JSeparator() );
        }
    }


    @Override
    public void endElement( String uri, String localName, String qName )
            throws SAXException {
        int modifier;
        String text, toolBar, mnemonic, key;
        Attributes atts;
        KeyStroke accelerator;
        JMenu menu;
        JMenuItem item;
        Class class_ = null;
        Constructor cons_ = null;
        Object obj_ = null;

        atts = lastAtts.pop();
        text = lastChars.pop().toString();

        if( qName.equalsIgnoreCase( "menu" ) ) {
            menu = menuPath.pop();

            if( (mnemonic = atts.getValue( "mnemonic" )) != null && mnemonic.length() > 0 )
                menu.setMnemonic( mnemonic.charAt( 0 ) );

            if( menuPath.empty() )
                jmb.add( menu );
            else
                menuPath.peek().add( menu );

        }
        else if( qName.equalsIgnoreCase( "action" ) ) {

            try {
                class_ = Class.forName( atts.getValue( "class" ) );
                cons_ = class_.getConstructor( consTypes );
                obj_ = cons_.newInstance( consObjs );

                if( !menuPath.empty() && obj_ instanceof AbstractAction ) {
                    item = new JMenuItem( (AbstractAction) obj_ );
                    item.setIcon( null );

                    key = atts.getValue( "key" );
                    if( key != null && key.length() > 0 ) {
                        try {
                            modifier = Integer.parseInt( atts.getValue( "modifier" ) );
                            accelerator = KeyStroke.getKeyStroke( key.charAt( 0 ), modifier );
                            item.setAccelerator( accelerator );
                        }
                        catch( NumberFormatException e ) {
                            log.warning( e.getMessage().concat( " is not a valid int." ) );
                        }
                    }
                    menuPath.peek().add( item );
                }

                if( (toolBar = atts.getValue( "toolBar" )) != null
                    && (Boolean.valueOf( toolBar )).booleanValue()
                    && obj_ instanceof AbstractAction )
                    jtb.add( (AbstractAction) obj_ );

            }
            catch( NoSuchMethodException e ) {
                log.warning( "Could not find Constructor: ".concat( e.getMessage() ) );
            }
            catch( InstantiationException e ) {
                log.warning( "Could not instantiate: ".concat( e.getMessage() ) );
            }
            catch( IllegalAccessException e ) {
                log.warning( e.getMessage() );
            }
            catch( java.lang.reflect.InvocationTargetException e ) {
                log.warning( e.getMessage() );
            }
            catch( ClassNotFoundException e ) {
                log.warning( "Could not find class: ".concat( e.getMessage() ) );
            }
        }
    }


    @Override
    public void setDocumentLocator( Locator locator ) {
    }


    @Override
    public void startDocument()
            throws SAXException {
        log.info( "Loading actions..." );
    }


    @Override
    public void endDocument()
            throws SAXException {
        log.info( "Actions successfully loaded." );
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
