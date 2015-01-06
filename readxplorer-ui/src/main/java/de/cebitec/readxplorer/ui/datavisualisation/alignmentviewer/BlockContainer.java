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

package de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer;


import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * @author ddoppmei, rhilker
 * <p>
 * A container for blocks. The blocks are added in a sorted fashion.
 */
public class BlockContainer {

    private final TreeMap<Integer, TreeSet<BlockI>> sortedMappings;


    public BlockContainer() {
        sortedMappings = new TreeMap<>();
    }


    /**
     * Adds a block to the container. The block order is sorted according to
     * start position.
     *
     * @param block block to add
     */
    public void addBlock( BlockI block ) {
        int start = block.getAbsStart();
        if( !sortedMappings.containsKey( start ) ) {
            sortedMappings.put( start, new TreeSet<>( new BlockComparator() ) );
        }
        sortedMappings.get( start ).add( block );
    }


    public BlockI getNextByPositionAndRemove( int pos ) {
        Integer key = sortedMappings.ceilingKey( pos );
        if( key != null ) {
            TreeSet<BlockI> set = sortedMappings.get( key );
            BlockI b = set.pollFirst();
            if( set.isEmpty() ) {
                sortedMappings.remove( key );
            }
            return b;
        }
        else {
            return null;
        }
    }


    public boolean isEmpty() {
        return sortedMappings.isEmpty();
    }


    private class BlockComparator implements Comparator<BlockI> {

        @Override
        public int compare( BlockI o1, BlockI o2 ) {
            // order by start of block
            if( o1.getAbsStart() < o2.getAbsStart() ) {
                return -1;
            }
            else if( o1.getAbsStart() > o2.getAbsStart() ) {
                return 1;
            }
            else {
                // if blocks start at identical position use stop position
                if( o1.getAbsStop() < o2.getAbsStop() ) {
                    return -1;
                }
                else if( o1.getAbsStop() > o2.getAbsStop() ) {
                    return 1;
                }
                else {
                    // stop position are identical, too
                    // use mapping id to distinguish and order
                    if( o1.getObjectWithId().getId() < o2.getObjectWithId().getId() ) {
                        return -1;
                    }
                    else if( o1.getObjectWithId().getId() > o2.getObjectWithId().getId() ) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                }
            }
        }


    }

}
