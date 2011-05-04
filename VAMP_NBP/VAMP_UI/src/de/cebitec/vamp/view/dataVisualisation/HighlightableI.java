package de.cebitec.vamp.view.dataVisualisation;

import java.awt.Rectangle;

/**
 * By implementing this interface the class gains a method for setting the
 * rectangle to highlight. How this information is used can still be decided by
 * the class.
 *
 * @author Rolf Hilker
 */
public interface HighlightableI {

    public void setHighlightRectangle(Rectangle rect);
}
