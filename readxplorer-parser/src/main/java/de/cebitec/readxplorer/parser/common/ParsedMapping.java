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

package de.cebitec.readxplorer.parser.common;


import java.util.Collections;
import java.util.List;


/**
 * Container for a parsed mapping. It contains all data a mapping should have.
 * ID, start, stop (start is always the smaller value), direction (1 for fwd and
 * -1 for rev), errors, diffs, gaps, bestmapping and number of replicates. Also
 * the read sequence can be stored here, but should be removed when not needed
 * anymore.
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedMapping {

    private long id;
    private final int start;
    private final int stop;
    private final byte direction;
    private final int errors;
    private final List<ParsedDiff> diffs;
    private final List<ParsedReferenceGap> gaps;
    private boolean bestMapping;
    private int numOfReplicates;


    /**
     * Standard constructor for a parsed mapping.
     * <p>
     * @param start start of the mapping
     * @param stop end of the mapping
     * @param direction direction of the mapping: 1 for fwd and -1 for rev
     * @param diffs the list of diffs between the reference and the mapping
     * @param gaps list of gaps between the reference and the mapping
     * @param errors number of errors
     */
    public ParsedMapping( int start, int stop, byte direction, List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps, int errors ) {
        this.start = start;
        this.stop = stop;
        this.direction = direction;
        this.diffs = diffs;
        this.gaps = gaps;
        this.errors = errors;
        this.bestMapping = false;
        this.numOfReplicates = 1;
    }


    /**
     * Sets if this is the best mapping for the given read in the complete
     * reference.
     * <p>
     * @param isBestMapping <code>true</code> if this is the best mapping for the
     * read, <code>false</code> otherwise
     */
    public void setIsBestMapping( boolean isBestMapping ) {
        this.bestMapping = isBestMapping;
    }


    /**
     * @return <code>true</code> if this is the best mapping for the read,
     * <code>false</code> otherwise
     */
    public boolean isBestMapping() {
        return this.bestMapping;
    }


    /**
     * @return The number of replicates of this mapping.
     */
    public int getNumReplicates() {
        return numOfReplicates;
    }


    /**
     * Increase the number of replicates of this mapping.
     */
    public void increaseCounter() {
        ++numOfReplicates;
    }


    /**
     * @return Start position of this mapping. Always the smaller value among
     * start and stop.
     */
    public int getStart() {
        return start;
    }


    /**
     * @return Stop position of this mapping. Always the larger value among
     * start and stop.
     */
    public int getStop() {
        return stop;
    }


    /**
     * @return direction of the mapping: 1 for fwd and -1 for rev
     */
    public byte getDirection() {
        return direction;
    }


    /**
     * @return The list of differences to the reference of this mapping.
     */
    public List<ParsedDiff> getDiffs() {
        return Collections.unmodifiableList( diffs );
    }


    /**
     * @return <code>true</code> if this mapping has differences to the
     *         reference, <code>false</code> otherwise
     */
    public boolean hasDiffs() {
        return !diffs.isEmpty();
    }


    /**
     * @return The list of genome gaps induced by this mapping.
     */
    public List<ParsedReferenceGap> getGenomeGaps() {
        return Collections.unmodifiableList( gaps );
    }


    /**
     * @return <code>true</code> if this mapping has genome gaps,
     *         <code>false</code> otherwise
     */
    public boolean hasGenomeGaps() {
        return !gaps.isEmpty();
    }


    /**
     * @return The number of mismatches to the reference of this mapping.
     */
    public int getErrors() {
        return errors;
    }


    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final ParsedMapping other = (ParsedMapping) obj;
        if( this.start != other.getStart() ) {
            return false;
        }
        if( this.stop != other.getStop() ) {
            return false;
        }
        if( this.direction != other.getDirection() ) {
            return false;
        }
        if( this.errors != other.getErrors() ) {
            return false;
        }
        if( this.diffs != other.getDiffs() && (this.diffs == null || !this.diffs.equals( other.getDiffs() )) ) {
            return false;
        }
        if( this.gaps != other.getGenomeGaps() && (this.gaps == null || !this.gaps.equals( other.getGenomeGaps() )) ) {
            return false;
        }
        if( this.bestMapping != other.isBestMapping() ) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.start;
        hash = 79 * hash + this.stop;
        hash = 79 * hash + this.direction;
        hash = 79 * hash + this.errors;
        hash = 79 * hash + (this.diffs != null ? this.diffs.hashCode() : 0);
        hash = 79 * hash + (this.gaps != null ? this.gaps.hashCode() : 0);
        hash = 79 * hash + (this.bestMapping ? 1 : 0);
        return hash;
    }


    /**
     * A unique mapping id.
     * @param mappingID the id
     */
    public void setID( long mappingID ) {
        this.id = mappingID;
    }


    /**
     * @return A unique mapping id.
     */
    public long getID() {
        return id;
    }


    /**
     * Set the number of replicates of this mapping to a certain value.
     * @param count The number of replicates to set
     */
    public void setCount( int count ) {
        this.numOfReplicates = count;
    }


}
