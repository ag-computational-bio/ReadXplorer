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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies;


import de.cebitec.readxplorer.tools.rnafolder.naview.PairTable;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;


/**
 * Interface Movie defines a central type describing a Movie. All
 * important functions and constants are defined in this interface.
 *
 * @author Alexander Kaiser - akaiser(at)techfak.uni-bielefeld.de
 * @author Jan Krueger - jkrueger(at)techfak.uni-bielefeld.de
 */
public interface Movie {

    /**
     * limit minimal zoom
     */
    public static final int MIN_ZOOM = 25;
    /**
     * limit maximal zoom
     */
    public static final int MAX_ZOOM = 400;
    /**
     * frames per millisecond
     */
    public static final float FPMS = 0.009f;


    /**
     * Set all necessary data to current movie object
     * <p>
     * @param List<Point2D[]> - A list of Point2D arrays describing the 2D
     *                        coordinates of the structure.
     * @param List<PairTable> - A list of Naview Pair table objects
     * @param String          - The title of current set of structurs, visible
     *                        within the graphics context.
     * @param String          -
     * @param int             - maximal frame width
     * @param int             - maximal fram height
     */
    public abstract void setMovie( List<Point2D[]> frames, List<PairTable> pairs,
                                   String title, String chars, int maxWidth, int maxHeight );


    /**
     * Set all necessary data to current movie object
     * <p>
     * @param List<Point2D[]> - A list of Point2D arrays describing the 2D
     *                        coordinates of the structure.
     * @param List<PairTable> - A list of Naview Pair table objects
     * @param String          - The title of current set of structurs, visible
     *                        within the graphics context.
     * @param String          -
     * @param int             - maximal frame width
     * @param int             - maximal fram height
     * @param boolean         - true if we work in GUI mode
     */
    public abstract void setMovie( List<Point2D[]> frames, List<PairTable> pairs,
                                   String title, String chars, int maxWidth, int maxHeight, boolean gui );


    /**
     * @return Return the zoom factor for the structures
     */
    public abstract int getZoom();


    /**
     * Set the zoom factor of a structure within the frame
     */
    public abstract void setZoom( int zoom );


    /**
     * @todo Ask Alex what this exactly do ...
     */
    public abstract void zoomFit();


    /**
     * @return Return the current frame index
     */
    public abstract int getFrameIdx();


    /**
     * @return Return the number of frames (structures)
     */
    public abstract int numFrames();


    /**
     * Jump to the specified frame(structure)
     * <p>
     * @param int Index of the frame
     */
    public abstract void gotoFrame( int idx );


    /**
     * @return Return the backgroundcolor
     */
    public abstract Color getBackground();


    /**
     * @return Return current rendering hints
     */
    public abstract RenderingHints getRenderingHints();


    /**
     * Draw the frame with given index on the graphics context.
     * <p>
     * @param Graphics2D - Graphics context
     * @param int        - Index of the frame
     */
    public abstract void drawFrame( Graphics2D gc, int idx );


    /**
     * Draw the frame with given index on the graphics context.
     * <p>
     * @param Graphics2D - Graphics context
     * @param int        - Index of the frame
     * @param int        - number of interpolation between two structures
     * @param int        - interpolation step
     */
    public abstract void drawFrame( Graphics2D gc,
                                    int idx,
                                    int numSteps,
                                    int step );


    /**
     * Return the specified frame as BufferedImage
     *
     * @param int     - Index of the frame
     * @param int     - number of interpolation between two structures
     * @param int     - interpolation step
     * @param boolean - resize the structure to a maximum that it fit inside the
     *                image dimensions
     * @param boolean - transparency support for the generated image
     * @param int     - zoom factor of the structure within the frame
     * @param int     - width in pixel of the generated image
     * @param int     - height in pixel of the generated image
     */
    public abstract BufferedImage getFrame( int idx,
                                            int numSteps,
                                            int step,
                                            boolean fit,
                                            boolean transparent,
                                            int zoom,
                                            int x,
                                            int y );


    /**
     * Return the specified frame as BufferedImage
     *
     * @param int     - Index of the frame
     * @param boolean - resize the structure to a maximum that it fit inside the
     *                image dimensions
     * @param boolean - transparency support for the generated image
     * @param int     - zoom factor of the structure within the frame
     * @param int     - width in pixel of the generated image
     * @param int     - height in pixel of the generated image
     */
    public abstract BufferedImage getFrame( int index,
                                            boolean fit,
                                            boolean transparent,
                                            int zoom,
                                            int x,
                                            int y );


    /**
     * @return Return true if the movie is running
     */
    public abstract boolean isRunning();


    /**
     * Reset all preferences to default
     */
    public abstract void reset();


    /**
     * Start the movie
     */
    public abstract void start();


    /**
     * Stop the movie
     */
    public abstract void stop();


    /**
     * Step one frame(structure) back
     */
    public abstract void bskip();


    /**
     * Step one frame(structure) forward
     */
    public abstract void fskip();


    /**
     * @return Return the maximal width over all frames in pixel
     */
    public abstract int getMaxWidth();


    /**
     * @return Return the maximal height over all frames in pixel
     */
    public abstract int getMaxHeight();


    /**
     * @return Return the width of current frame in pixel
     */
    public abstract int getWidth();


    /**
     * @return Return the height of the current frame in pixel
     */
    public abstract int getHeight();


}
