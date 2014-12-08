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
package de.cebitec.readXplorer.parser.common;


/**
 * @author ddoppmeier
 * <p>
 * Data structure representing a gap in the reference genome.
 */
public class ParsedReferenceGap {

    private long absPos;
    private char base;
    private int order;


    /**
     * Data structure representing a gap in the reference genome.
     * <p>
     * @param absPos absolute position of the gap in the reference genome
     * @param base   base of the gap in the mapping
     * @param order  the order at which position in a larger gap it occurs (1,
     *               2, 3...)
     */
    public ParsedReferenceGap( int absPos, char base, int order ) {
        this.absPos = absPos;
        this.base = base;
        this.order = order;
    }


    /**
     * @return the absolute position of the gap in the reference genome
     */
    public long getAbsPos() {
        return absPos;
    }


    /**
     * @return the base of the gap in the mapping
     */
    public char getBase() {
        return base;
    }


    /**
     * @return at which position in a larger gap it occurs (1, 2, 3...)
     */
    public int getOrder() {
        return order;
    }


    @Override
    public boolean equals( Object obj ) {
        if( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final ParsedReferenceGap other = (ParsedReferenceGap) obj;
        if( this.absPos != other.getAbsPos()
            || this.base != other.getBase()
            || this.order != other.getOrder() ) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (int) (this.absPos ^ (this.absPos >>> 32));
        hash = 71 * hash + this.base;
        hash = 71 * hash + this.order;
        return hash;
    }


}
