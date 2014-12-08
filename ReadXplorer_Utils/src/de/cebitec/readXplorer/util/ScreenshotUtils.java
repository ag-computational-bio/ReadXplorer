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

package de.cebitec.readXplorer.util;


import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


/**
 * Class containing all methods for readxplorer, which involve screenshots.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ScreenshotUtils {

    /**
     * Utility class. Not instantiable.
     */
    private ScreenshotUtils() {
    }


    /**
     * Creates an SVG screenshot of an arbitrary <code>Container</code> and
     * opens
     * a save dialog to store the svg somewhere.
     * <p>
     * @param container the <code>Container</code>, for which a screenshot shall
     *                  be
     *                  stored
     */
    @NbBundle.Messages( { "ScreenshotUtils.SuccessMsg=Successfully saved the screenshot in ",
                          "ScreenshotUtils.SuccessHeader=Screenshot saved" } )
    public static void saveScreenshot( final Container container ) {
        try {
            if( container.isShowing() ) {
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                String svgNS = "http://www.w3.org/2000/svg";
                Document document = domImpl.createDocument( svgNS, "svg", null );
                final SVGGraphics2D svgGenerator = new SVGGraphics2D( document );
                Dimension screenSize = ScreenshotUtils.getOptimalScreenSize( container, container.getBounds().getSize() );
                svgGenerator.setSVGCanvasSize( screenSize );
                Dimension compDim = container.getSize();
                if( screenSize.height < compDim.height ) {
                    screenSize.height = compDim.height;
                }
                if( screenSize.width < compDim.width ) {
                    screenSize.width = compDim.width;
                }
                container.setBounds( new Rectangle( screenSize ) );
                container.paintAll( svgGenerator );

                ReadXplorerFileChooser screenFileChooser = new ReadXplorerFileChooser( new String[]{ "svg" }, "svg" ) {
                    private static final long serialVersionUID = 1L;


                    @Override
                    public void save( final String fileLocation ) {

                        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.progress.name" ) );
                        progressHandle.start();

                        Thread exportThread = new Thread( new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    OutputStream file = new FileOutputStream( fileLocation );
                                    Writer out = new OutputStreamWriter( file, "UTF-8" );
                                    svgGenerator.stream( out, false );
                                    out.close();
                                }
                                catch( IOException ex ) {
                                    JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.ErrorMsg", ex.toString() ),
                                                                   NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.FailHeader" ), JOptionPane.ERROR_MESSAGE );
                                }

                                progressHandle.finish();

                                NotificationDisplayer.getDefault().notify( Bundle.ScreenshotUtils_SuccessHeader(),
                                                                           new ImageIcon(), Bundle.ScreenshotUtils_SuccessMsg() + fileLocation, null );

                                Logger.getLogger( this.getClass().getName() ).log( Level.INFO, "Finished writing Excel file!" );
                            }


                        } );
                        exportThread.start();

                    }


                    @Override
                    public void open( String fileLocation ) {
                        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
                    }


                };
                screenFileChooser.openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );
            }
            else {
                JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.FocusErrorMsg" ),
                                               NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.FocusErrorHeader" ), JOptionPane.ERROR_MESSAGE );
            }
        }
        catch( OutOfMemoryError e ) {
            JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.OOMErrorMsg" ),
                                           NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.OOMErrorHeader" ), JOptionPane.ERROR_MESSAGE );
        }
        catch( RuntimeException e ) {
            JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.ErrorMsg", e.toString() ),
                                           NbBundle.getMessage( ScreenshotUtils.class, "ScreenshotUtils.FailHeader" ), JOptionPane.ERROR_MESSAGE );
        }
        System.gc();
    }


    /**
     * Calculates the optimal screen size for the given container. Optimal size
     * means, that all subcomponents can be displayed in their full size. Starts
     * the calculation with the given <code>currentDim</code>.
     * <p>
     * @param container  the container whose optimal subcomponents size shall be
     *                   calculated
     * @param currentDim the current dimension to start with, any smaller
     *                   dimension is ignored. Only if a subcomponent needs more space than given
     *                   here, the dimension is adapted
     * <p>
     * @return The optimal screen size for the given container
     */
    public static Dimension getOptimalScreenSize( Container container, Dimension currentDim ) {
        Component[] comps = container.getComponents();
        for( int i = 0; i < comps.length; ++i ) {
            try {
                currentDim = getOptimalScreenSize( (Container) comps[i], currentDim );
                Component comp = comps[i];
                int width = comp.getWidth();
                int height = comp.getHeight();
                if( comp instanceof JScrollPane ) {
                    JScrollPane pane = (JScrollPane) comp;
                    Dimension scrollViewDim = pane.getViewport().getViewSize();
                    int totalHeight = scrollViewDim.height + comp.getLocationOnScreen().y;
                    if( currentDim.height < totalHeight ) {
                        currentDim.height = totalHeight;
                    }
                    if( currentDim.width < scrollViewDim.width ) {
                        currentDim.width = scrollViewDim.width + comp.getLocationOnScreen().x;
                    }
                }
                if( currentDim.height < height ) {
                    currentDim.height = height + comp.getLocationOnScreen().y;
                }
                if( currentDim.width < width ) {
                    currentDim.width = width + comp.getLocationOnScreen().x;
                }
            }
            catch( IllegalStateException e ) {
                //nothing to do: ignoring non visible components of the current container
            }
        }
        return currentDim;
    }


}
