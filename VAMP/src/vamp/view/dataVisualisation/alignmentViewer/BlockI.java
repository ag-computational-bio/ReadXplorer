package vamp.view.dataVisualisation.alignmentViewer;

import java.util.Iterator;
import vamp.databackend.dataObjects.PersistantMapping;

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
