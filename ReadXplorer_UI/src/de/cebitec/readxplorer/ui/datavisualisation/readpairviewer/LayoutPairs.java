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

package de.cebitec.readxplorer.ui.datavisualisation.readpairviewer;


import de.cebitec.readxplorer.databackend.dataObjects.Mapping;
import de.cebitec.readxplorer.databackend.dataObjects.ReadPair;
import de.cebitec.readxplorer.databackend.dataObjects.ReadPairGroup;
import de.cebitec.readxplorer.utils.classification.Classification;
import de.cebitec.readxplorer.ui.datavisualisation.GenomeGapManager;
import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.BlockContainer;
import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.BlockI;
import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.LayerI;
import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.LayoutI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * A LayoutPairs holds all information to display for read pair alignments
 * in different, non-overlapping layers.
 * <p>
 * @author rhilker
 */
public class LayoutPairs implements LayoutI {

    private final int absStart;
    private final int absStop;
    private final ArrayList<LayerI> reverseLayers;
    private final BlockContainer reverseBlockContainer;
    private final List<Classification> exclusionList;


    /**
     * Creates a new layout for read pairs.
     * <p>
     * @param absStart      start of the interval
     * @param absStop       end of the interval
     * @param readPairs     all read pairs to add to the layout
     * @param exclusionList list of excluded feature types
     */
    public LayoutPairs( int absStart, int absStop, Collection<ReadPairGroup> readPairs, List<Classification> exclusionList ) {
        this.absStart = absStart;
        this.absStop = absStop;
        this.reverseLayers = new ArrayList<>();
        this.reverseBlockContainer = new BlockContainer();
        this.exclusionList = exclusionList;

        this.createBlocks( readPairs );
        this.layoutBlocks( this.reverseLayers, this.reverseBlockContainer );
    }


    /**
     * Each read pair group gets one block.
     * <p>
     * @param readPairList read pairs in current interval
     */
    private void createBlocks( Collection<ReadPairGroup> readPairList ) {
        Iterator<ReadPairGroup> groupIt = readPairList.iterator();
        while( groupIt.hasNext() ) {
            ReadPairGroup group = groupIt.next();
            List<ReadPair> readPairs = group.getReadPairs();
            List<Mapping> singleMappings = group.getSingleMappings();
            Iterator<ReadPair> pairIt = readPairs.iterator();
            Iterator<Mapping> singleIt = singleMappings.iterator();
            long start = Long.MAX_VALUE;
            long stop = Long.MIN_VALUE;
            boolean containsVisibleMapping = false;
            //handle pairs
            while( pairIt.hasNext() ) {
                ReadPair pair = pairIt.next();
                containsVisibleMapping = !exclusionList.contains( pair.getVisibleMapping().getMappingClass() )
                                         || !exclusionList.contains( pair.getVisibleMapping2().getMappingClass() );

                if( containsVisibleMapping ) {
                    // get start position
                    if( pair.getStart() > this.absStart && pair.getStart() < start ) {
                        start = pair.getStart();
                    }

                    // get stop position
                    if( pair.getStop() < this.absStop && pair.getStop() > stop ) {
                        stop = pair.getStop();
                    }
                }
            }

            //handle single mappings
            while( singleIt.hasNext() ) {
                Mapping mapping = singleIt.next();
                containsVisibleMapping = containsVisibleMapping ? containsVisibleMapping : !exclusionList.contains( mapping.getMappingClass() );

                //update start position, if necessary
                if( mapping.getStart() > this.absStart && mapping.getStart() < start ) {
                    start = mapping.getStart();
                }

                //update start position, if necessary
                if( mapping.getStop() < this.absStop && mapping.getStop() > stop ) {
                    stop = mapping.getStop();
                }
            }

            start = start == Long.MAX_VALUE ? this.absStart : start;
            stop = stop == Long.MIN_VALUE ? this.absStop : stop;

            BlockI block = new BlockPair( (int) start, (int) stop, group );
            this.reverseBlockContainer.addBlock( block );

        }
    }


    /**
     * Fills each single layer until all blocks were added from the block
     * container
     * to the layer list
     * <p>
     * @param layers list of layers to add the blocks to
     * @param blocks block container to add to layers
     */
    private void layoutBlocks( ArrayList<LayerI> layers, BlockContainer blocks ) {
        LayerI l;
        while( !blocks.isEmpty() ) {
            l = new LayerPair();
            this.fillLayer( l, blocks );
            layers.add( l );
        }
    }


    /**
     * Fills a single layer with as many blocks as possible, while obeying to
     * the
     * rule, that the blocks in one layer are not allowed to overlap.
     * <p>
     * @param l      single layer to fill with blocks
     * @param blocks block container
     */
    private void fillLayer( LayerI l, BlockContainer blocks ) {
        BlockI block = blocks.getNextByPositionAndRemove( 0 );
        int counter = 0;
        while( block != null ) {
            counter++;
            l.addBlock( block );
            block = blocks.getNextByPositionAndRemove( block.getAbsStop() + 1 );
        }
    }


    /**
     * @return Since all mappings are shown on the "reverse strand" aka below
     *         the
     *         sequence bar, it only returns null!
     */
    @Override
    public Iterator<LayerI> getForwardIterator() {
//        return forwardLayers.iterator();
        return null;
    }


    /**
     * @return The iterator for the reverse layer
     */
    @Override
    public Iterator<LayerI> getReverseIterator() {
        return reverseLayers.iterator();
    }


    /**
     * @return null, because this implementation of Layout does not need a
     *         GenomeGapManager
     */
    @Override
    public GenomeGapManager getGenomeGapManager() {
        return null;
    }


}
