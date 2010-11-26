package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public interface BlockI {

    public Iterator<Brick> getBrickIterator();

    public PersistantMapping getMapping();

    public int getNumOfBricks();

    public int getAbsStart();

    public int getAbsStop();
}
