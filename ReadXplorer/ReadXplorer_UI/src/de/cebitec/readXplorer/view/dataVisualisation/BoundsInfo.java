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
package de.cebitec.readXplorer.view.dataVisualisation;

/**
 * This class stores information about a region that should be displayed for
 * example by an AbstractViewer. It stores left and right borders, current center position,
 * current width and zoom level and also maximal left and right values.
 * 
 * @author ddoppmeier, rhilker
 */
public class BoundsInfo {

    private int logLeft;
    private int logRight;
    private int logWidth;
    private int currentLogPos;
    private int zoomValue;
    private int maxLogRight;
    private int maxLogLeft;
    private int currentChromId;

    /**
     * This class stores information about a region that should be displayed for
     * example by an AbstractViewer. It stores left and right borders, current
     * position, current width and zoom level and also maximal left and right
     * values.
     * @param maxLogLeft the leftmost position of the reference to be shown
     * @param maxLogRight the maximal position of the reference that can be
     * displayed. Normally equals the size of the reference.
     * @param currentLogPos the current center position that should be displayed
     * @param zoomValue The current zoom value.
     * @param currentChromId The id of the currently visible chromosome/reference sequence.
     */
    public BoundsInfo(int maxLogLeft, int maxLogRight, int currentLogPos, int zoomValue, int currentChromId){
        this.maxLogLeft = maxLogLeft;
        this.maxLogRight = maxLogRight;
        this.currentLogPos = currentLogPos;
        this.zoomValue = zoomValue;
        logWidth = 200;
        updateLeftAndRight();
        this.currentChromId = currentChromId;
    }

    /**
     * This class stores information about a region that should be displayed for
     * example by an AbstractViewer. It stores left and right borders, current
     * position, current width and zoom level and also maximal left and right
     * values.
     * @param maxLogLeft the leftmost position of the reference to be shown
     * @param maxLogRight the maximal position of the reference that can be
     * displayed. Normally equals the size of the reference.
     * @param currentLogPos the current center position that should be displayed
     * @param zoomValue The current zoom value.
     * @param currentChromId The id of the currently visible chromosome/reference sequence.
     * @param width  
     */
    public BoundsInfo(int maxLogLeft, int maxLogRight, int currentLogPos, int zoomValue, int currentChromId, int width){
        this.maxLogLeft = maxLogLeft;
        this.maxLogRight = maxLogRight;
        this.currentLogPos = currentLogPos;
        this.zoomValue = zoomValue;
        this.currentChromId = currentChromId;
        this.logWidth = width;
        updateLeftAndRight();
    }

    /**
     * @return the leftmost position of the genome to be shown
     */
    public int getMaxLogLeft() {
        return maxLogLeft;
    }

    /**
     * @return the maximal position of the genome that can be displayed.
     * Normally equals the size of the genome
     */
    public int getMaxLogRight() {
        return maxLogRight;
    }

    /**
     * @return the current center position that should be displayed
     */
    public int getCurrentLogPos() {
        return currentLogPos;
    }

    /**
     * @param currentLogPos the current center position that should be displayed
     */
    public void setCurrentLogPos(int currentLogPos) {
        this.currentLogPos = currentLogPos;
        updateLeftAndRight();
    }

    /**
     * @return the leftmost position of the area currently visible
     */
    public int getLogLeft() {
        return logLeft;
    }

    /**
     * @return the rightmost position of the area currently visible
     */
    public int getLogRight() {
        return logRight;
    }

    /**
     * @return the width in bases of the interval from the genome that is currently visible
     */
    public int getLogWidth() {
        return logWidth;
    }

    /**
     * @param logWidth the width in bases of the interval from the genome that 
     * is currently visible
     */
    public void setLogWidth(int logWidth){
        this.logWidth = logWidth;
        updateLeftAndRight();
    }

    /**
     * @param newLogRight The value, which shall replace the old logRight value.
     * logWidth is updated automatically.
     */
    public void correctLogRight(int newLogRight){
        this.logRight = newLogRight;
        this.logWidth = this.logRight - logLeft +1;
    }

    /**
     * Udate the left and right logical bounds depending on the current center position.
     * Restricts bounds, so that they do not reach out of the maximal bounds
     */
    private void updateLeftAndRight() {
        logLeft = currentLogPos - logWidth/2;
        if(logWidth % 2 == 0 ){
            logRight = currentLogPos + logWidth/2 -1;
        } else {
            logRight = currentLogPos + logWidth/2;
        }

        // out of left boundary
        if(logLeft < maxLogLeft){
            int shift = logLeft * -1 +1;
            logLeft += shift;
            logRight += shift;
        }

        // out of right boundary
        if(logRight > maxLogRight){
            int shift = logRight - maxLogRight;
            logRight -= shift;
            logLeft -= shift;
        }
    }

    /**
     * @return The current zoom value.
     */
    public int getZoomValue() {
        return zoomValue;
    }

    /**
     * @return The id of the currently visible chromosome/reference sequence.
     */
    public int getCurrentChromId() {
        return currentChromId;
    }

    /**
     * @param currentChromId The id of the new visible chromosome/reference sequence.
     */
    public void setCurrentChromId(int currentChromId) {
        this.currentChromId = currentChromId;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof BoundsInfo) {
            BoundsInfo otherBounds = (BoundsInfo) other;
            return otherBounds.getCurrentLogPos() == this.getCurrentLogPos() 
                && otherBounds.getLogLeft() == this.getLogLeft()
                && otherBounds.getLogRight() == this.getLogRight() 
                && otherBounds.getLogWidth() == this.getLogWidth()
                && otherBounds.getMaxLogLeft() == this.getMaxLogLeft() 
                && otherBounds.getMaxLogRight() == this.getMaxLogRight()
                && otherBounds.getZoomValue() == this.getZoomValue()
                && otherBounds.getCurrentChromId() == this.getCurrentChromId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.logLeft;
        hash = 13 * hash + this.logRight;
        hash = 13 * hash + this.logWidth;
        hash = 13 * hash + this.currentLogPos;
        hash = 13 * hash + this.zoomValue;
        hash = 13 * hash + this.maxLogRight;
        hash = 13 * hash + this.maxLogLeft;
        hash = 13 * hash + this.currentChromId;
        return hash;
    }


}
