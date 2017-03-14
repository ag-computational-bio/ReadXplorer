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

package de.cebitec.readxplorer.ui.datavisualisation;


import java.awt.Dimension;


/**
 * This interface defines listeners for changes in the size of the interval of
 * the reference sequence, that should be displayed.
 * <p>
 * @author ddoppmeier
 */
public interface LogicalBoundsListener {

    /**
     * Notify the listeners of new bounds.
     * <p>
     * @param bounds the new boudns to set
     */
    void updateLogicalBounds( BoundsInfo bounds );


    /**
     * @return The size of the area, that is used for drawing. Logical bounds
     *         depend on the available size of each listener.
     */
    Dimension getPaintingAreaDimension();


    /**
     * @return true, if the PaintingArea has coordinates to calculate bounds,
     *         false otherwise.
     */
    boolean isPaintingAreaAvailable();


}
