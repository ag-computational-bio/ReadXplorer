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

package de.cebitec.readxplorer.utils;


import de.cebitec.readxplorer.utils.filechooser.ReadXplorerFileChooser;
import de.cebitec.readxplorer.utils.svg.BatikSvgExporter;
import de.cebitec.readxplorer.utils.svg.JFreeSvgExporter;
import de.cebitec.readxplorer.utils.svg.SvgExporter;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.MissingResourceException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;


/**
 * Class containing all methods for readxplorer, which involve screenshots.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class ScreenshotUtils {

    private static final Logger LOG = LoggerFactory.getLogger( ScreenshotUtils.class.getName() );

    private static final String SVG = "svg";


    /**
     * Utility class. Not instantiable.
     */
    private ScreenshotUtils() {
    }


    /**
     * Creates an SVG screenshot of an arbitrary <code>Container</code> and
     * opens a save dialog to store the svg somewhere.
     * <p>
     * @param container the <code>Container</code>, for which a screenshot shall
     *                  be stored
     */
    @NbBundle.Messages( { "ScreenshotUtils.SuccessMsg=Successfully saved the screenshot in ",
                          "ScreenshotUtils.SuccessHeader=Screenshot saved", 
                          "# {0} - file", "ScreenshotUtils.ErrorMsg=Something went wrong during storing of the screenshot: {0}", 
                          "ScreenshotUtils.FailHeader=Screenshot Export Error", 
                          "ScreenshotUtils.OOMErrorHeader=Out of Memory Error", 
                          "ScreenshotUtils.OOMErrorMsg=ReadXplorer is out of memory! Please restart the application with more RAM! (.../readxplorer/ext/readxplorer.conf)", 
                          "ScreenshotUtils.FocusErrorHeader=Focus Problem", 
                          "ScreenshotUtils.FocusErrorMsg=The component, of which a screenshot shall be saved, needs to be visible! Please focus the desired component!", 
                          "ScreenshotUtils.progress.name=Exporting screenshot..." } )
    public static void saveScreenshot( final Container container ) {
        try {
            if( container.isShowing() ) {

                final Dimension screenSize = ScreenshotUtils.getOptimalScreenSize( container, container.getBounds().getSize() );
                Dimension compDim = container.getSize();
                if( screenSize.height < compDim.height ) {
                    screenSize.height = compDim.height;
                }
                if( screenSize.width < compDim.width ) {
                    screenSize.width = compDim.width;
                }

                final SvgExporter svgExporter;
                if( OsUtils.isWindows() ) {
                    svgExporter = new BatikSvgExporter();
                } else {
                    svgExporter = new JFreeSvgExporter();
                }
                container.setBounds( new Rectangle( screenSize ) );
                svgExporter.paintToExporter( container, screenSize );

                ReadXplorerFileChooser screenFileChooser = new ReadXplorerFileChooser( new String[]{ SVG }, SVG ) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void save( final String fileLocation ) {

                        final ProgressHandle progressHandle = ProgressHandle.createHandle( Bundle.ScreenshotUtils_progress_name() );
                        progressHandle.start();

                        Thread exportThread = new Thread( new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    svgExporter.exportSvg( fileLocation );
                                } catch( IOException ex ) {
                                    JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), Bundle.ScreenshotUtils_ErrorMsg( ex.toString() ),
                                                                   Bundle.ScreenshotUtils_FailHeader(), JOptionPane.ERROR_MESSAGE );
                                }

                                progressHandle.finish();

                                NotificationDisplayer.getDefault().notify( Bundle.ScreenshotUtils_SuccessHeader(),
                                                                           new ImageIcon(), Bundle.ScreenshotUtils_SuccessMsg() + fileLocation, null );

                                LOG.info( "Finished writing SVG file!" );
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
            } else {
                JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), Bundle.ScreenshotUtils_FocusErrorMsg(),
                                               Bundle.ScreenshotUtils_FocusErrorHeader(), JOptionPane.ERROR_MESSAGE );
            }
        } catch( OutOfMemoryError e ) {
            JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), Bundle.ScreenshotUtils_OOMErrorMsg(),
                                           Bundle.ScreenshotUtils_OOMErrorHeader(), JOptionPane.ERROR_MESSAGE );
        } catch( HeadlessException | MissingResourceException | DOMException e ) {
            JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), Bundle.ScreenshotUtils_ErrorMsg( e.toString() ),
                                           Bundle.ScreenshotUtils_FailHeader(), JOptionPane.ERROR_MESSAGE );
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
     *                   dimension is ignored. Only if a subcomponent needs more
     *                   space than given here, the dimension is adapted
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
            } catch( IllegalStateException ise ) {
                LOG.warn( ise.getMessage() );
                //nothing to do: ignoring non visible components of the current container
            }
        }
        return currentDim;
    }


}
