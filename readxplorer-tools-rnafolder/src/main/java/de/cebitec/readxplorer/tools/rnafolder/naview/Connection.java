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


public class Connection {

    protected int start = 0;
    protected int end = 0;
    protected double xrad = 0.0;
    protected double yrad = 0.0;
    protected double angle = 0.0;
    protected boolean broken = false;
    protected boolean extruded = false;
    protected Loop loop = null;
    protected Region region = null;


    protected Connection( int start, int end, Loop loop, Region region ) {
        this.start = start;
        this.end = end;
        this.loop = loop;
        this.region = region;
    }


    public double getAngle() {
        return angle;
    }


    public Point2D getRad() {
        return new Point2D.Double( xrad, yrad );
    }


    public int getStart() {
        return start;
    }


    public int getEnd() {
        return end;
    }


    public Loop getLoop() {
        return loop;
    }


    public Region getRegion() {
        return region;
    }


    public boolean isBroken() {
        return broken;
    }


    public boolean isExtruded() {
        return extruded;
    }


    public boolean isConnected( Connection cnext ) {
        if( this.extruded )
            return true;
        else if( this.end + 1 == cnext.start )
            return true;
        else
            return false;
    }


    public String toString() {
        return ("Loop #" + loop.number + " -> Region #" + region.number
                + " (" + start + "-" + end + ")");
    }


}
