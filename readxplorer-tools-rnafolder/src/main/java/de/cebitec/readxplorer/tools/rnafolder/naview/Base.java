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

package de.cebitec.readxplorer.tools.rnafolder.naview;


import java.awt.geom.Point2D;


public class Base {

    protected int mate;
    protected double x = 9999.0;
    protected double y = 9999.0;
    protected boolean extracted = false;
    protected Region region = null;


    protected Base( int mate ) {
        this.mate = mate;
    }


    public int getMate() {
        return mate;
    }


    public Point2D getPosition() {
        return new Point2D.Double( x, y );
    }


    public Region getRegion() {
        return region;
    }


}
