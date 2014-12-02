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
package de.cebitec.readXplorer.databackend.dataObjects;


import java.io.Serializable;


/**
 * Depicts a comparable difference in a mapping to the reference genome.
 * Therefore, it contains base, position, number of replicates and strand
 * information.
 *
 * @author ddoppmeier, Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class Difference extends BasicDiff implements Comparable<Difference>,
                                                     Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * Depicts a comparable difference in a mapping to the reference genome.
     * Therefore, it contains base, position, number of replicates, strand
     * information, base quality and mapping quality value.
     * <p>
     * @param position        position of the diff in the reference
     * @param base            the base already converted to the correct strand
     *                        and upper
     *                        case
     * @param isForwardStrand <code>true</code> if the diff was on the fwd
     *                        strand,
     *                        <code>false</code> otherwise
     * @param count           number of replicates of the diff
     * @param baseQuality     The phred base quality value for the diff base in
     *                        the
     *                        read.
     * @param mappingQuality  The phred mapping quality value of the read from
     *                        which the diff originated
     */
    public Difference( int position, char base, boolean isForwardStrand, int count, byte baseQuality, Byte mappingQuality ) {
        super( position, base, isForwardStrand, count, baseQuality, mappingQuality );
    }


    /**
     * Compares the other diff to this diff by position.
     * <p>
     * @param other the diff to compare with this diff
     * <p>
     * @return -1 if this diff has a smaller position than the other, 1 if this
     *         diff has a larger position than the other, and 0 if the position is equal
     */
    @Override
    public int compareTo( Difference other ) {
        if( this.getPosition() < other.getPosition() ) {
            return -1;
        }
        else if( this.getPosition() > other.getPosition() ) {
            return 1;
        }
        else {
            return 0;
        }
    }


}
