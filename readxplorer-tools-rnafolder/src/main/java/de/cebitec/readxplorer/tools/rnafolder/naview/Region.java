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


public class Region {

    protected int number;
    protected int start1, end1, start2, end2;


    protected Region( int number, int start1, int end1, int start2, int end2 ) {
        this.number = number;
        this.start1 = start1;
        this.end1 = end1;
        this.start2 = start2;
        this.end2 = end2;
    }


    public int getStart1() {
        return start1;
    }


    public int getEnd1() {
        return end1;
    }


    public int getStart2() {
        return start2;
    }


    public int getEnd2() {
        return end2;
    }


    public boolean equals( Region region ) {
        return (this.start1 == region.start1
                && this.end1 == region.end1 && this.start2 == region.start2
                && this.end2 == region.end2);
    }


    public String toString() {
        return ("Region #" + number + ": ("
                + start1 + "-" + end1 + "), (" + start2 + "-" + end2
                + ") Gap: " + (start2 - end1 + 1));
    }


}
