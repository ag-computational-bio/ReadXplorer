package de.cebitec.vamp.view.dataVisualisation;

import java.awt.Dimension;

/**
 * This interface defines listeners for changes in the size of the interval of
 * the reference sequence, that should be displayed.
 * 
 * @author ddoppmeier
 */
public interface LogicalBoundsListener {

    /**
     * Notify the listeners of new bounds.
     * @param bounds the new boudns to set
     */
    public void updateLogicalBounds(BoundsInfo bounds);

    /**
     * @return The size of the area, that is used for drawing. Logical bounds 
     * depend on the available size of each listener.
     */
    public Dimension getPaintingAreaDimension();

    /**
     * @return true, if the PaintingArea has coordinates to calculate bounds,
     * false otherwise.
     */
    public boolean isPaintingAreaAvailable();
}
