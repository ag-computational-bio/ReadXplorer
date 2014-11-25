package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantObject;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public interface BlockI {

    public int getAbsStart();

    public int getAbsStop();
    
    public Iterator<Brick> getBrickIterator();

    public PersistantObject getPersistantObject();

    public int getNumOfBricks();
}
