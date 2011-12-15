package de.cebitec.vamp.view.dataVisualisation.seqPairViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantSeqPairGroup;
import de.cebitec.vamp.databackend.dataObjects.PersistantSequencePair;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.BlockContainer;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.BlockI;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.LayerI;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.LayoutI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author rhilker
 * 
 * A LayoutPairs holds all information to display for sequence pair alignments 
 * in different, non-overlapping layers.
 */
public class LayoutPairs implements LayoutI {

    private int absStart;
    private int absStop;
    private ArrayList<LayerI> reverseLayers;
    private BlockContainer reverseBlockContainer;

    public LayoutPairs(int absStart, int absStop, Collection<PersistantSeqPairGroup> seqPairs) {
        this.absStart = absStart;
        this.absStop = absStop;
        this.reverseLayers = new ArrayList<LayerI>();
        this.reverseBlockContainer = new BlockContainer();

        this.createBlocks(seqPairs);
        this.layoutBlocks(this.reverseLayers, this.reverseBlockContainer);
    }

    /**
     * Each seq pair group gets one block.
     * @param seqPair mappings in current interval
     */
    private void createBlocks(Collection<PersistantSeqPairGroup> seqPair) {
        Iterator<PersistantSeqPairGroup> groupIt = seqPair.iterator();
        while (groupIt.hasNext()) {
            PersistantSeqPairGroup group = groupIt.next();
            List<PersistantSequencePair> seqPairs = group.getSequencePairs();
            List<PersistantMapping> singleMappings = group.getSingleMappings();
            Iterator<PersistantSequencePair> pairIt = seqPairs.iterator();
            Iterator<PersistantMapping> singleIt = singleMappings.iterator();
            long start = Long.MAX_VALUE;
            long stop = Long.MIN_VALUE;

            //handle pairs
            while (pairIt.hasNext()) {
                PersistantSequencePair pair = pairIt.next();

                // get start position
                if (pair.getStart() > this.absStart && pair.getStart() < start) {
                    start = pair.getStart();
                }

                // get stop position
                if (pair.getStop() < this.absStop && pair.getStop() > stop) {
                    stop = pair.getStop();
                }
            }

            //handle single mappings
            while (singleIt.hasNext()) {
                PersistantMapping mapping = singleIt.next();

                //update start position, if necessary
                if (mapping.getStart() > this.absStart && mapping.getStart() < start) {
                    start = mapping.getStart();
                }

                //update start position, if necessary
                if (mapping.getStop() < this.absStop && mapping.getStop() > stop) {
                    stop = mapping.getStop();
                }
            }

            start = start == Long.MAX_VALUE ? this.absStart : start;
            stop = stop == Long.MIN_VALUE ? this.absStop : stop;

            BlockI block = new BlockPair((int) start, (int) stop, seqPairs, singleMappings, group.getSeqPairId());
            this.reverseBlockContainer.addBlock(block);

        }
    }

    /**
     * Fills each single layer until all blocks were added from the block container
     * to the layer list
     * @param layers list of layers to add the blocks to
     * @param blocks block container to add to layers
     */
    private void layoutBlocks(ArrayList<LayerI> layers, BlockContainer blocks) {
        LayerI l;
        while (!blocks.isEmpty()) {
            l = new LayerPair();
            this.fillLayer(l, blocks);
            layers.add(l);
        }
    }

    /**
     * Fills a single layer with as many blocks as possible, while obeying to the
     * rule, that the blocks in one layer are not allowed to overlap.
     * @param l single layer to fill with blocks
     * @param blocks block container
     */
    private void fillLayer(LayerI l, BlockContainer blocks) {
        BlockI block = blocks.getNextByPositionAndRemove(0);
        int counter = 0;
        while (block != null) {
            counter++;
            l.addBlock(block);
            block = blocks.getNextByPositionAndRemove(block.getAbsStop() + 1);
        }
    }

    /**
     * @return Since all mappings are shown on the "reverse strand" aka below the
     * sequence bar, it only returns null!
     */
    @Override
    public Iterator<LayerI> getForwardIterator() {
//        return forwardLayers.iterator();
        return null;
    }

    @Override
    public Iterator<LayerI> getReverseIterator() {
        return reverseLayers.iterator();
    }

    /**
     * @return null, because this implementation of Layout does not need a GenomeGapManager
     */
    @Override
    public GenomeGapManager getGenomeGapManager() {
        return null;
    }
}
