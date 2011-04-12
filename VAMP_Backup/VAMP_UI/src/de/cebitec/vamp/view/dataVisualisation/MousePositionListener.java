package de.cebitec.vamp.view.dataVisualisation;

/**
 *
 * @author ddoppmeier
 */
public interface MousePositionListener {

    public void setCurrentMousePosition(int logPos);

    // TODO is this function desired?
    public void setMouseOverPaintingRequested(boolean requested);

}
