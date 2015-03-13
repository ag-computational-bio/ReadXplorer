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

package de.cebitec.readxplorer.utils.sequence;


/**
 * A region marked by a start and stop position and if it should be read in fwd
 * or reverse direction. Furthermore, it holds the type of the region.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class Region implements GenomicRange {

    private final int start;
    private final int stop;
    private final boolean isForwardStrand;
    private final int type;


    /**
     * A region marked by a start and stop position and if it should be read in
     * fwd or reverse direction. Furthermore, it holds the type of the region.
     * <p>
     * @param start the start of the region as base position, always smaller
     * than stop
     * @param stop the stop of the region as base position, always larger than
     * start
     * @param isForwardStrand true, if it is on the fwd strand, false otherwise
     * @param type type of the region. Use Properties.CDS, Properties.START,
     * Properties.STOP, Properties.PATTERN or Properties.ALL
     */
    public Region( int start, int stop, boolean isForwardStrand, int type ) {
        this.start = start;
        this.stop = stop;
        this.isForwardStrand = isForwardStrand;
        this.type = type;
    }


    /**
     * @return the start of this region = the starting position in the genome.
     * Always smaller than stop.
     */
    @Override
    public int getStart() {
        return this.start;
    }


    /**
     * @return The stop of this region. = the ending position in the genome.
     * Always larger than start.
     */
    @Override
    public int getStop() {
        return this.stop;
    }


    /**
     * @return The length of the feature in base pairs.
     */
    public int getLength() {
        return GenomicRange.Utils.getLength( this );
    }


    /**
     * @return The start position on the feature strand = smaller position for
     * features on the fwd and larger position for features on the rev strand.
     */
    public int getStartOnStrand() {
        return GenomicRange.Utils.getStartOnStrand( this );
    }


    /**
     * @return The stop position on the feature strand = smaller position for
     * features on the rev and larger position for features on the fwd strand.
     */
    public int getStopOnStrand() {
        return GenomicRange.Utils.getStopOnStrand( this );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFwdStrand() {
        return this.isForwardStrand;
    }


    /**
     * @return The type of the region. Either Properties.CDS, Properties.START,
     * Properties.STOP, Properties.PATTERN or Properties.ALL
     */
    public int getType() {
        return this.type;
    }


    /**
     * Compares the start positions of both Regions.
     *
     * @param other Region to compare to this one
     * @return The value 0 if this position is equal to the argument position; a
     * value less than 0 if this position is numerically less than the argument
     * position; and a value greater than 0 if this position is numerically
     * greater than the argument position (signed comparison).
     */
    @Override
    public int compareTo( GenomicRange other ) {
        return ((Integer) this.start).compareTo( other.getStart() );
    }


    @Override
    public boolean equals( Object other ) {
        if( other instanceof Region ) {
            Region otherRegion = (Region) other;
            if( otherRegion.isFwdStrand() == this.isFwdStrand()
                    && otherRegion.getType() == this.getType()
                    && otherRegion.getStart() == this.getStart()
                    && otherRegion.getStop() == this.getStop() ) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.start;
        hash = 23 * hash + this.stop;
        hash = 23 * hash + (this.isForwardStrand ? 1 : 0);
        hash = 23 * hash + this.type;
        return hash;
    }


}
