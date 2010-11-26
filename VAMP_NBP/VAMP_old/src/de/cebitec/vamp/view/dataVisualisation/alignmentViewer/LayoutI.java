package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public interface LayoutI {

    public Iterator<LayerI> getForwardIterator();

    public Iterator<LayerI> getReverseIterator();

    public GenomeGapManager getGenomeGapManager();

}
