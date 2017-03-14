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

package de.cebitec.readxplorer.ui.datavisualisation.abstractviewer;


/**
 * Storage for the physical bounds of the base window.
 * Contains the left and right bounds, width and middle position.
 *
 * @author ddoppmeier
 */
public class PhysicalBaseBounds {

    private final double leftPhysBound;
    private final double rightPhysBound;
    private final double physWidth;
    private final double phyMiddle;


    /**
     * Storage for the physical bounds of the base window. Contains the left
     * and right bounds, width and middle position.
     * <p>
     * @param leftPhysBound  the left physical boundary (pixel)
     * @param rightPhysBound the right physical boundary (pixel) (pixel)
     */
    public PhysicalBaseBounds( double leftPhysBound, double rightPhysBound ) {
        this.leftPhysBound = leftPhysBound;
        this.rightPhysBound = rightPhysBound;
        this.physWidth = rightPhysBound - leftPhysBound + 1;
        phyMiddle = (leftPhysBound + rightPhysBound) / 2;
    }


    /**
     * @return the left physical boundary (pixel)
     */
    public double getLeftPhysBound() {
        return leftPhysBound;
    }


    /**
     * @return the right physical boundary (pixel)
     */
    public double getRightPhysBound() {
        return rightPhysBound;
    }


    /**
     * @return the physical width.
     */
    public double getPhysWidth() {
        return physWidth;
    }


    /**
     * @return the physical center value.
     */
    public double getPhyMiddle() {
        return phyMiddle;
    }


}
