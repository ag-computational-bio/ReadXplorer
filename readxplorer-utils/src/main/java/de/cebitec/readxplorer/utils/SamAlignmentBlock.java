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

package de.cebitec.readxplorer.utils;


/**
 * A block representing a part of a sam alignment.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SamAlignmentBlock {

    private final int refStart;
    private final int refStop;


    /**
     * A block representing a part of a sam alignment.
     * <p>
     * @param refStart block start in reference coordinates
     * @param refStop block stop in reference coordinates
     */
    public SamAlignmentBlock( int refStart, int refStop ) {
        this.refStart = refStart;
        this.refStop = refStop;
    }


    /**
     * @return block start in reference coordinates
     */
    public int getRefStart() {
        return refStart;
    }


    /**
     * @return block stop in reference coordinates
     */
    public int getRefStop() {
        return refStop;
    }
    
    
    /**
     * @return The length of the block.
     */
    public int getLength() {
        return Math.abs( refStop - refStart ) + 1;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.refStart;
        hash = 29 * hash + this.refStop;
        return hash;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final SamAlignmentBlock other = (SamAlignmentBlock) obj;
        if( this.refStart != other.getRefStart() ) {
            return false;
        }
        if( this.refStop != other.getRefStop() ) {
            return false;
        }
        return true;
    }


}
