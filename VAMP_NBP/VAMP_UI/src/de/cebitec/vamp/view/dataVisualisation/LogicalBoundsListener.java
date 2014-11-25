package de.cebitec.vamp.view.dataVisualisation;

import java.awt.Dimension;

/**
 *This interface defines listeners for changes in the size of the intervall of
 * the reference sequence, that should be displayed
 * @author ddoppmeier
 */
public interface LogicalBoundsListener {

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

        /**
     *
     * @return if the PaintingArea has coords to calculate bounds
     */
    public boolean isPaintingAreaAviable();
}
