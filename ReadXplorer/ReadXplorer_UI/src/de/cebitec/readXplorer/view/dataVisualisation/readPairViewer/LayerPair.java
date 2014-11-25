package de.cebitec.readXplorer.view.dataVisualisation.readPairViewer;

import de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer.BlockI;
import de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer.LayerI;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Rolf Hilker
 * 
 * Contains a single layer of blocks belonging to  one sequence pair.
 */
public class LayerPair implements LayerI {
    private ArrayList<BlockI> blocks;

    /**
     * Contains a single layer of blocks belonging to  one sequence pair.
     */
    public LayerPair(){
        blocks = new ArrayList<>();
    }

    @Override
    public void addBlock(BlockI block) {
        blocks.add(block);
    }

    @Override
    public String toString(){
        
        //start und stop hier
        StringBuilder sb = new StringBuilder();
        for (BlockI b : blocks) {
            sb.append(b.toString());
        }

        return sb.toString();
    }

    @Override
    public Iterator<BlockI> getBlockIterator() {
        return blocks.iterator();
    }
}
