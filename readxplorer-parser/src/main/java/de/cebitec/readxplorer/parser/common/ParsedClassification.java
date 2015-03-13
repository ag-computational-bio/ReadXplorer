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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileHeader;


/**
 * Class for the classification data of a read.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParsedClassification {

    private final SAMFileHeader.SortOrder sortOrder;
    private int minMismatches;
    private final List<Integer> readStarts;
    private final Map<Integer, Integer> mismatchCountMap = new HashMap<>();

    //    private List<String> refNames; //TODO use this when multiple import is enabled

    /**
     * Class for the classification data of a read.
     * <p>
     * @param sortOrder Sort order
     */
    public ParsedClassification( SAMFileHeader.SortOrder sortOrder ) {
        this.sortOrder = sortOrder;
        this.minMismatches = Integer.MAX_VALUE;
        this.readStarts = new ArrayList<>();
    }


    /**
     * @return The smallest number of mismatches for the associated read in the
     * current data set.
     */
    public int getMinMismatches() {
        return minMismatches;
    }


    /**
     * Updates the smallest number of mismatches for this read with the given
     * value. The value is only set, if it is smaller than the value already
     * stored here.
     * <p>
     * @param mismatches The number of mismatches for the associated read at the
     * current mapping position.
     */
    public void updateMinMismatches( int mismatches ) {
        if( mismatches < minMismatches ) {
            minMismatches = mismatches;
        }
    }


    /**
     * @return The array of read start positions for this read.
     */
    public List<Integer> getReadStarts() {
        return Collections.unmodifiableList( readStarts );
    }


    /**
     * Calculates the next mapping start for the given mapping start. If it is
     * the largest mapping start, then the smallest mapping start is returned to
     * be able to reach all mappings belonging to a read. If only one position
     * is stored in the array, 0 is returned.
     * <p>
     * @param start the start of the current read
     * <p>
     * @return The start position of the next mapping or the smallest mapping
     * position, if this is the largest mapping position in the array. If only
     * one position is stored in the array, 0 is returned.
     */
    public int getNextMappingStart( int start ) {
        if( readStarts.size() <= 1 ) {
            return 0;
        } else if( sortOrder == SAMFileHeader.SortOrder.coordinate ) {
            return getSortedMappingStart( start );
        } else {
            return calcNextMappingStart( start );
        }
    }


    /**
     * @param start The start to which the next start should be returned
     * <p>
     * @return The next larger start position of the same read or the smallest
     * mapping position, if this is the largest mapping position for the read
     */
    private int getSortedMappingStart( int start ) {
        int index = readStarts.indexOf( start ) + 1;
        if( index < readStarts.size() ) {
            return readStarts.get( index );
        } else {
            return readStarts.get( 0 );
        }
    }


    /**
     * Calculates the next mapping start for the given mapping start. If it is
     * the largest mapping start, then the smallest mapping start is returned to
     * be able to reach all mappings belonging to a read. If no fitting position
     * is found, 0 is returned.
     * <p>
     * @param start the start of the current read
     * <p>
     * @return The start position of the next mapping or the smallest mapping
     * position, if this is the largest mapping position in the array. If no
     * fitting position is found, 0 is returned.
     */
    private int calcNextMappingStart( int start ) {

        int nextStart = Integer.MAX_VALUE;
        int smallestStart = start;
        for( Integer mapStart : readStarts ) {
            if( mapStart > start && mapStart < nextStart ) {
                nextStart = mapStart;

            } else if( mapStart < smallestStart ) {
                smallestStart = mapStart;
            }
        }
        nextStart = nextStart == Integer.MAX_VALUE && smallestStart < start ? smallestStart : nextStart;

        return nextStart == Integer.MAX_VALUE ? 0 : nextStart;
    }


    /**
     * Adds a mapping start position to the start positions of this read in the
     * current data set.
     * <p>
     * @param mappingStart The mapping start position of this read to add to the
     * list
     */
    public void addReadStart( int mappingStart ) {
        readStarts.add( mappingStart );
    }


    /**
     * @return The number of occurrences of this read in the data set.
     */
    public int getNumberOccurrences() {
        return readStarts.size();
    }


    /**
     * @return The map of a number of mismatches to their count = how often has
     * this number of mismatches been observed in total.
     */
    public Map<Integer, Integer> getMismatchCountMap() {
        return Collections.unmodifiableMap( mismatchCountMap );
    }


    /**
     * Increases the entry of the given noMismatches by one.
     * <p>
     * @param noMismatches The number of mismatches entry to increase by one
     */
    public void updateMismatchCountMap( int noMismatches ) {
        if( !mismatchCountMap.containsKey( noMismatches ) ) {
            mismatchCountMap.put( noMismatches, 0 );
        }
        mismatchCountMap.put( noMismatches, mismatchCountMap.get( noMismatches ) + 1 );
    }


}
