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

package de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer;


import java.awt.Color;
import java.awt.Rectangle;


/**
 * Contains all data for painting a Brick.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class BrickData {

    private final Brick brick;
    private final Color brickColor;
    private final Rectangle rectangle;
    private final int labelCenter;


    /**
     * Contains all data for painting a Brick.
     * <p>
     * @param brick       The brick to represent
     * @param rectangle   Rectangle representing the brick
     * @param brickColor  Color of the brick rectangle
     * @param labelCenter Center position of the brick label
     */
    public BrickData( Brick brick, Rectangle rectangle, Color brickColor, int labelCenter ) {
        this.brick = brick;
        this.brickColor = brickColor;
        this.rectangle = rectangle;
        this.labelCenter = labelCenter;
    }


    /**
     * @return The brick to represent
     */
    public Brick getBrick() {
        return brick;
    }


    /**
     * @return Rectangle representing the brick
     */
    public Rectangle getRectangle() {
        return rectangle;
    }


    /**
     * @return Color of the brick rectangle
     */
    public Color getBrickColor() {
        return brickColor;
    }


    /**
     * @return x position of the brick label
     */
    public int getLabelCenter() {
        return labelCenter;
    }


    /**
     * @return The type string of the brick stored in this data object.
     */
    @Override
    public String toString() {
        return this.brick.getTypeString();
    }


}
