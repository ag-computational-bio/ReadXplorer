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

package de.cebitec.readxplorer.view.datavisualisation;


import de.cebitec.readxplorer.view.datavisualisation.abstractviewer.AbstractViewer;
import java.awt.Rectangle;


/**
 * Utility class for common methods to aid painting of data.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class PaintUtilities {

    /**
     * No instantiation allowed.
     */
    private PaintUtilities() {
    }


    /**
     * Calculates the physical (pixel) boundaries of some component with a start
     * and stop position in reference coordinates.
     * <p>
     * @param refStart start of the component in reference coordinates
     * @param refStop  stop of the component in reference coordinates
     * @param viewer   The viewer in which the component is shown later
     * @param phyLeft  Left physical (pixel) of the component in the viewer
     * @param height   Height to set for the rectangle
     * <p>
     * @return
     */
    public static Rectangle calcBlockBoundaries( int refStart, int refStop, AbstractViewer viewer, int phyLeft, int height ) {
        int startPixel = (int) viewer.getPhysBoundariesForLogPos( refStart ).getLeftPhysBound();
        int stopPixel = (int) viewer.getPhysBoundariesForLogPos( refStop ).getRightPhysBound();
        int absLength = stopPixel - startPixel;
        int minBlockLength = 3;
        absLength = absLength < minBlockLength ? minBlockLength : absLength;
        return new Rectangle( startPixel - phyLeft, 0, absLength, height );
    }


}
