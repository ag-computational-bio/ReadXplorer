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


import java.awt.Dimension;


/**
 * This interface defines listeners for changes in the size of the interval to
 * display on the screen (position of zoom level of the reference sequence was
 * changed)
 *
 * @author ddoppmeier
 */
public interface LogicalBoundsListenerI {

    /**
     * Notify the listeners of new bounds
     * <p>
     * @param bounds
     */
    public void updateLogicalBounds( BoundsInfo bounds );


    /**
     *
     * @return the size of the area, that is used for drawing. Logical bounds
     *         depend on the available size of each listener.
     */
    public Dimension getPaintingAreaDimension();


}
