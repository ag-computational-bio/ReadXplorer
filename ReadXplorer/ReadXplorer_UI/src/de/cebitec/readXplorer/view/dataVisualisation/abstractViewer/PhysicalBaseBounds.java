/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;

/**
 * Storage for the physical bounds of the base window.
 * Contains the left and right bounds, width and middle position.
 *
 * @author ddoppmeier
 */
public class PhysicalBaseBounds {

    private double leftPhysBound;
    private double rightPhysBound;
    private double physWidth;
    private double phyMiddle;

    public PhysicalBaseBounds(double leftPhysBound, double rightPhysBound){
        this.leftPhysBound = leftPhysBound;
        this.rightPhysBound = rightPhysBound;
        this.physWidth = rightPhysBound - leftPhysBound + 1;
        phyMiddle = (leftPhysBound + rightPhysBound) / 2;
    }

    /**
     * Returns the left physical bound.
     * @return
     */
    public double getLeftPhysBound() {
        return leftPhysBound;
    }

    /**
     * Returns the right physical bound.
     * @return
     */
    public double getRightPhysBound() {
        return rightPhysBound;
    }

    /**
     * Returns the physical width.
     * @return
     */
    public double getPhysWidth() {
        return physWidth;
    }

    /**
     * Returns the physical center value.
     * @return
     */
    public double getPhyMiddle() {
        return phyMiddle;
    }
    

}
