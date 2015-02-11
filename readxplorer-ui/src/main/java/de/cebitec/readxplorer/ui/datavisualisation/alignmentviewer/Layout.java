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


import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.ui.datavisualisation.GenomeGapManager;
import de.cebitec.readxplorer.utils.classification.Classification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * A Layout holds all information to display for an alignment in different, non
 * overlapping layers. It also know which data is on the exclusion list and
 * should not be displayed.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class Layout implements LayoutI {

    private final int absStart;
    private int absStop;
    private GenomeGapManager gapManager;
    private final ArrayList<LayerI> forwardLayers;
    private final ArrayList<LayerI> reverseLayers;
    private final BlockContainer forwardBlockContainer;
    private final BlockContainer reverseBlockContainer;
    private final List<Classification> exclusionList;


    /**
     * Creates a new layout for read mappings.
     * <p>
     * @param absStart      start of the interval
     * @param absStop       end of the interval
     * @param mappings      all read mappings to add to the layout
     * @param exclusionList list of excluded feature types
     */
    public Layout( int absStart, int absStop, Collection<Mapping> mappings, List<Classification> exclusionList ) {
        this.absStart = absStart;
        this.absStop = absStop;
        this.forwardLayers = new ArrayList<>();
        this.reverseLayers = new ArrayList<>();
        this.forwardBlockContainer = new BlockContainer();
        this.reverseBlockContainer = new BlockContainer();
        this.exclusionList = exclusionList;

        this.storeGaps( mappings );
        this.createBlocks( mappings );
        this.layoutBlocks( forwardLayers, forwardBlockContainer );
        this.layoutBlocks( reverseLayers, reverseBlockContainer );
    }


    /**
     * Handles and stores the genome gaps for all mappings, which are not in the
     * type classes in the exclusion list.
     * <p>
     * @param mappings mappings covering current part of the genome
     */
    private void storeGaps( Collection<Mapping> mappings ) {
        gapManager = new GenomeGapManager( absStart, absStop );
        Iterator<Mapping> it = mappings.iterator();
        while( it.hasNext() ) {
            Mapping mapping = it.next();
            if( !exclusionList.contains( mapping.getMappingClass() ) ) {
                gapManager.addGapsFromMapping( mapping.getGenomeGaps() );
            }
        }

        // gaps do extend the width of this layout
        // so absStop has to be decreased, to fit to old width

        // count the number of gaps occurring in visible area
        int width = absStop - absStart + 1;
        int gapNo = 0; // count the number of gaps
        int widthCount = 0; // count the number of bases
        int i = 0; // count variable till max width
        while( widthCount < width ) {
            int num = gapManager.getNumOfGapsAt( absStart + i ); // get the number of gaps at current position
            ++widthCount; // current position needs 1 base space in visual alignment
            widthCount += num; // if gaps occurred at current position, they need some space, too
            gapNo += num;
            ++i;
        }
        absStop -= gapNo;
    }


    /**
     * Each mapping gets one block, if it is not in a type class in the
     * exclusion list.
     * <p>
     * @param mappings mappings in current interval
     */
    private void createBlocks( Collection<Mapping> mappings ) {
        Iterator<Mapping> mappingIt = mappings.iterator();
        while( mappingIt.hasNext() ) {
            Mapping mapping = mappingIt.next();
            if( !exclusionList.contains( mapping.getMappingClass() ) ) {

                int start = mapping.getStart();
                int stop = mapping.getStop();

                if( mapping.getTrimmedFromLeft() > 0 ) {
                    if( mapping.isFwdStrand() ) {
                        start -= mapping.getTrimmedFromLeft();
                    }
                    else {
                        stop += mapping.getTrimmedFromLeft();
                    }
                }
                if( mapping.getTrimmedFromRight() > 0 ) {
                    if( mapping.isFwdStrand() ) {
                        stop += mapping.getTrimmedFromRight();
                    }
                    else {
                        start -= mapping.getTrimmedFromRight();
                    }
                }

                if( start < this.absStart ) {
                    start = this.absStart;
                }
                if( stop > this.absStop ) {
                    stop = this.absStop;
                }

                BlockI block = new Block( start, stop, mapping, gapManager );
                if( mapping.isFwdStrand() ) {
                    forwardBlockContainer.addBlock( block );
                }
                else {
                    reverseBlockContainer.addBlock( block );
                }
            }

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
            l = new Layer( absStart, absStop, gapManager );
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


    @Override
    public Iterator<LayerI> getForwardIterator() {
        return forwardLayers.iterator();
    }


    @Override
    public Iterator<LayerI> getReverseIterator() {
        return reverseLayers.iterator();
    }


    @Override
    public GenomeGapManager getGenomeGapManager() {
        return gapManager;
    }


}
