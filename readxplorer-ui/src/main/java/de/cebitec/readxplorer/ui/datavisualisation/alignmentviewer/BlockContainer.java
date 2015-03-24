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


import de.cebitec.readxplorer.utils.sequence.GenomicRange;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * A container for blocks. The blocks are added in a sorted fashion.
 * <p>
 * @author ddoppmei, rhilker
 */
public class BlockContainer {

    private final TreeMap<Integer, TreeSet<BlockI>> sortedMappingBlocks;
    private boolean sortFwd;


    /**
     * A container for blocks. The blocks are added in a sorted fashion.
     * <p>
     * @param sortFwd <code>true</code> = blocks shall be ordered in fwd
     *                direction (from smallest to largest genomic position),
     *                <code>false</code> = blocks shall be ordered in rev
     *                direction (from largest to smalles genomic position)
     */
    public BlockContainer( boolean sortFwd ) {
        sortedMappingBlocks = new TreeMap<>();
        this.sortFwd = sortFwd;
    }


    /**
     * Adds a block to the container. The block order is sorted according to
     * start position.
     * <p>
     * @param block block to add
     */
    public void addBlock( BlockI block ) {
        int start = GenomicRange.Utils.getStartOnStrand( block );
        if( !sortedMappingBlocks.containsKey( start ) ) {
            sortedMappingBlocks.put( start, new TreeSet<>( new BlockComparator() ) );
        }
        sortedMappingBlocks.get( start ).add( block );
    }


    /**
     * @param pos The genomic position of interest
     * <p>
     * @return The next mapping block in the order passed to the BlockContainer
     *         upon creation.
     */
    public BlockI getNextByPositionAndRemove( int pos ) {
        Integer key;
        if( sortFwd ) {
            key = sortedMappingBlocks.ceilingKey( pos );
        } else {
            key = sortedMappingBlocks.lowerKey( pos );
        }
        if( key != null ) {
            TreeSet<BlockI> set = sortedMappingBlocks.get( key );
            BlockI b = set.pollFirst();
            if( set.isEmpty() ) {
                sortedMappingBlocks.remove( key );
            }
            return b;
        } else {
            return null;
        }
    }


    /**
     * @return <code>true</code> if the map of mapping blocks is empty,
     *         <code>false</code> = otherwise
     */
    public boolean isEmpty() {
        return sortedMappingBlocks.isEmpty();
    }


    /**
     * @return <code>true</code> = blocks shall be ordered in fwd direction
     *         (from smallest to largest genomic position), <code>false</code> =
     *         blocks shall be ordered in rev direction (from largest to smalles
     *         genomic position)
     */
    public boolean isSortFwd() {
        return sortFwd;
    }


    /**
     * Comparator for blocks.
     */
    private class BlockComparator implements Comparator<BlockI> {

        @Override
        public int compare( BlockI o1, BlockI o2 ) {
            // order by start of block
            if( o1.getStart() < o2.getStart() ) {
                return -1;
            } else if( o1.getStart() > o2.getStart() ) {
                return 1;
            } else {
                // if blocks start at identical position use stop position
                if( o1.getStop() < o2.getStop() ) {
                    return -1;
                } else if( o1.getStop() > o2.getStop() ) {
                    return 1;
                } else {
                    // stop position are identical, too
                    // use mapping id to distinguish and order
                    if( o1.getObjectWithId().getId() < o2.getObjectWithId().getId() ) {
                        return -1;
                    } else if( o1.getObjectWithId().getId() > o2.getObjectWithId().getId() ) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }


    }

}
