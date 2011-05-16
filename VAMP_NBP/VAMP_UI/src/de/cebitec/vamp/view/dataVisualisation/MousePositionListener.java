package de.cebitec.vamp.view.dataVisualisation;

/**
 * Listener for the current mouse position.
 *
 * @author ddoppmeier
 */
public interface MousePositionListener {

    public void setCurrentMousePosition(int logPos);

    public void setMouseOverPaintingRequested(boolean requested);

}
