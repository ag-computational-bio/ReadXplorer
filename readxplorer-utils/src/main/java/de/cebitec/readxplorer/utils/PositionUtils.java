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


import de.cebitec.readxplorer.utils.sequence.GenomicRange;
import java.util.Collections;
import java.util.List;


/**
 * Designed for methods handling any kind of position specific functionality.
 *
 * @author rhilker
 */
public final class PositionUtils {

    /**
     * Utility class. Instantiation is not allowed.
     */
    private PositionUtils() {
    }


    /**
     * Converts a position string to the corresponding integer position.
     * <p>
     * @param posString position as string, which might include a '_'
     * <p>
     * @return corresponding position value as integer
     */
    public static int convertPosition( String posString ) {
        if( posString.contains( "_" ) ) {
            posString = posString.substring( 0, posString.lastIndexOf( '_' ) );
        }
        return Integer.parseInt( posString );
    }


    /**
     * @param genomicRange feature whose frame has to be determined
     * <p>
     * @return 1, 2, 3, -1, -2, -3 depending on the reading frame of the feature
     */
    public static int determineFrame( GenomicRange genomicRange ) {
        int frame;
        if( genomicRange.isFwdStrand() ) {
            frame = PositionUtils.determineFwdFrame( genomicRange.getStart() );
        } else {
            frame = PositionUtils.determineRevFrame( genomicRange.getStop() );
        }
        return frame;
    }


    /**
     * Determines the frame of an element at a given genomic position on the fwd
     * strand.
     * <p>
     * @param position genomic position whose frame needs to be determined
     * <p>
     * @return 1, 2, 3 depending on the reading frame of the feature
     */
    public static int determineFwdFrame( int position ) {
        return (position - 1) % 3 + 1;
    }


    /**
     * Determines the frame of an element at a given genomic position on the rev
     * strand.
     * <p>
     * @param position genomic position whose frame needs to be determined
     * <p>
     * @return -1, -2, -3 depending on the reading frame of the feature
     */
    public static int determineRevFrame( int position ) {
        return (position - 1) % 3 - 3;
    }


    /**
     * Sorts the given <code>GenomicRange</code> implementation list according
     * to the given sort order. For items with <code>isFwdOrder</code> =
     * <code>true</code>, it sorts by the natural order, for items with
     * <code>isFwdOrder</code> = <code>false</code>, it sorts by the reverse
     * natural order.
     * <p>
     * @param isFwdOrder The sort order to use for the list
     * @param genomicRanges The list of genomic ranges to sort
     */
    public static void sortList( boolean isFwdOrder, List<? extends GenomicRange> genomicRanges ) {
        if( isFwdOrder ) {
            Collections.sort( genomicRanges ); //start with first
        } else {
            Collections.sort( genomicRanges, Collections.<GenomicRange>reverseOrder() ); //start with last
        }
    }


    /**
     * Update the last interval of the given list or create a new interval in
     * the given list, if the new boundaries are beyond the boundaries of the
     * last interval in the list. The passed list has to be sorted by position.
     * <p>
     * @param intervals list of current intervals
     * @param start start pos of new interval to add
     * @param stop stop pos of new interval to add
     */
    public static void updateIntervals( List<Pair<Integer, Integer>> intervals, int start, int stop ) {
        int lastIndex = intervals.size() - 1;
        if( intervals.get( lastIndex ).getSecond() < start ) { //add new pair
            intervals.add( new Pair<>( start, stop ) );
        } else { //increase length of first pair (start remains, stop is enlarged)
            intervals.get( lastIndex ).setSecond( stop );
        }
    }


}
