package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import de.cebitec.readXplorer.view.dataVisualisation.GenomeGapManager;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public interface LayoutI {

    /**
     * @return An iterator for the forward layer.
     */
    public Iterator<LayerI> getForwardIterator();

    /**
     * @return An iterator for the reverse layer.
     */
    public Iterator<LayerI> getReverseIterator();

    /**
     * @return The genome gap manager for this layout.
     */
    public GenomeGapManager getGenomeGapManager();

}
