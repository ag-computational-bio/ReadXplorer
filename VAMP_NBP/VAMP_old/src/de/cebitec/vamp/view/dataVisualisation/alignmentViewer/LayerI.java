package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public interface LayerI {

    public Iterator<BlockI> getBlockIterator();

    public void addBlock(BlockI block);
}
