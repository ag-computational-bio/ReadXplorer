package de.cebitec.vamp.view.dataVisualisation;

import java.awt.Dimension;

/**
 * This interface defines listeners for changes in the size of the interval to
 * display on the screen (position of zoom level of the reference sequence was changed)
 *
 * @author ddoppmeier
 */
public interface LogicalBoundsListenerI {

    /**
     * Notify the listeners of new bounds
     * @param bounds
     */
    public void updateLogicalBounds(BoundsInfo bounds);

    /**
     *
     * @return the size of the area, that is used for drawing. Logical bounds depend on the available size of each listener.
     */
    public Dimension getPaintingAreaDimension();

}
