package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author ddoppmei
 * 
 * A Layout holds all information to display for an alignment in different, non
 * overlapping layers.
 */
public class Layout implements LayoutI {

    private int absStart;
    private int absStop;
    private GenomeGapManager gapManager;
    private ArrayList<LayerI> forwardLayers;
    private ArrayList<LayerI> reverseLayers;
    private BlockContainer forwardBlockContainer;
    private BlockContainer reverseBlockContainer;

    public Layout(int absStart, int absStop, Collection<PersistantMapping> mappings){
        this.absStart = absStart;
        this.absStop = absStop;
        forwardLayers = new ArrayList<LayerI>();
        reverseLayers = new ArrayList<LayerI>();
        forwardBlockContainer = new BlockContainer();
        reverseBlockContainer = new BlockContainer();
        
        this.storeGaps(mappings);
        this.createBlocks(mappings);
        this.layoutBlocks(forwardLayers, forwardBlockContainer);
        this.layoutBlocks(reverseLayers, reverseBlockContainer);
    }

    /**
     * Handles and stores the genome gaps.
     * @param mappings mappings covering current part of the genome
     */
    private void storeGaps(Collection<PersistantMapping> mappings){
        gapManager = new GenomeGapManager(absStart, absStop);
        Iterator<PersistantMapping> it = mappings.iterator();
        while(it.hasNext()){
            PersistantMapping m = it.next();
            gapManager.addGapsFromMapping(m.getGenomeGaps());
        }

        // gaps do extend the width of this layout
        // so absStop has to be decreased, to fit to old width

        // count the number of gaps occuring in visible area
        int width = absStop - absStart +1;
        int gapNo = 0; // count the number of gaps
        int widthCount = 0; // count the number of bases
        int i = 0; // count variable till max width
        while(widthCount < width){
            int num = gapManager.getNumOfGapsAt(absStart+i); // get the number of gaps at current position
            widthCount++; // current position needs 1 base space in visual alignment
            widthCount += num; // if gaps occured at current position, they need some space, too
            gapNo += num;
            i++;
        }
        absStop -= gapNo;
    }

    /**
     * Each mapping gets one block.
     * @param mappings mappings in current interval
     */
    private void createBlocks(Collection<PersistantMapping> mappings){
        Iterator<PersistantMapping> it = mappings.iterator();
        while(it.hasNext()){
            PersistantMapping m = it.next();
            // get start position
            int start = m.getStart();
            if(start < this.absStart){
                start = this.absStart;
            }

            // get stop position
            int stop = m.getStop();
            if(stop > this.absStop){
                stop = this.absStop;
            }

            BlockI block = new Block(start, stop, m, gapManager);
            if(m.isForwardStrand()){
                forwardBlockContainer.addBlock(block);
            } else {
                reverseBlockContainer.addBlock(block);
            }
        }
    }    

    /**
     * Fills each single layer until all blocks were added from the block container
     * to the layer list
     * @param layers list of layers to add the blocks to
     * @param blocks block container to add to layers
     */
    private void layoutBlocks(ArrayList<LayerI> layers, BlockContainer blocks){
        LayerI l;
        while(!blocks.isEmpty()){
            l = new Layer(absStart, absStop, gapManager);
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
    private void fillLayer(LayerI l, BlockContainer blocks ){
        BlockI block = blocks.getNextByPositionAndRemove(0);
        int counter = 0;
        while(block != null){
            counter++;
            l.addBlock(block);
            block = blocks.getNextByPositionAndRemove(block.getAbsStop()+1);
        }
    }

    @Override
    public Iterator<LayerI> getForwardIterator(){
        return forwardLayers.iterator();
    }

    @Override
    public Iterator<LayerI> getReverseIterator(){
        return reverseLayers.iterator();
    }

    @Override
    public GenomeGapManager getGenomeGapManager() {
        return gapManager;
    }

}
