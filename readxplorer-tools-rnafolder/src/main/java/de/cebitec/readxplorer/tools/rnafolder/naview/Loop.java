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
import java.util.ArrayList;
import java.util.List;


public class Loop {

    protected int number = 0;
    protected double x = 9999.0;
    protected double y = 9999.0;
    private double r = 0.0;
    protected List<Connection> connections;
    private boolean mark = false;
    static final double rt2_2 = 0.7071068;
    static final double lencut = 0.5;


    protected Loop( int number ) {
        this.number = number;
        this.connections = new ArrayList<>();
    }


    protected Connection connect( int start, int end, Loop loop, Region region ) {
        Connection connection = new Connection( start, end, loop, region );
        connections.add( connection );
        return connection;
    }


    protected int getDepth() {
        int count, ret, d;

        if( this.connections.size() <= 1 )
            return 0;

        if( this.mark )
            return -1;

        this.mark = true;
        count = ret = 0;
        for( Connection connection : this.connections ) {
            d = connection.loop.getDepth();
            if( d >= 0 ) {
                if( count++ == 0 )
                    ret = d;
                else if( ret > d )
                    ret = d;
            }
        }
        this.mark = false;
        return ret + 1;
    }


    protected double getRadius( int nb ) {
        double mindit, ci, dt, sumn, sumd, radius, dit;
        int i, j, end, start, imindit;
        Connection connection, cnext;

        if( r > 0.0 )
            return r;

        imindit = 0;
        do {
            mindit = 1.0e10;
            for( sumd = 0.0, sumn = 0.0, i = 0;
                 i < connections.size();
                 i++ ) {
                connection = connections.get( i );
                j = i + 1;
                if( j >= connections.size() )
                    j = 0;
                cnext = connections.get( j );
                end = connection.end;
                start = cnext.start;
                if( start < end )
                    start += nb + 1;
                dt = cnext.angle - connection.angle;
                if( dt <= 0.0 )
                    dt += 2 * Math.PI;
                if( !connection.extruded )
                    ci = start - end;
                else
                    ci = dt <= Math.PI / 2 ? 2.0 : 1.5;
                sumn += dt * (1.0 / ci + 1.0);
                sumd += dt * dt / ci;
                dit = dt / ci;
                if( dit < mindit && !connection.extruded && ci > 1.0 ) {
                    mindit = dit;
                    imindit = i;
                }
            }
            radius = sumn / sumd;
            if( radius < rt2_2 )
                radius = rt2_2;
            if( mindit * radius < lencut ) {
                connections.get( imindit ).extruded = true;
            }
        }
        while( mindit * radius < lencut );
        r = radius;
        return radius;
    }


    public Point2D getPosition() {
        return new Point2D.Double( x, y );
    }


    public double getRadius() {
        return r;
    }


    public Connection[] getConnections() {
        return connections.toArray( new Connection[]{} );
    }


    public String toString() {
        return ("Loop #" + number + ": " + connections.size() + " connections");
    }


}
