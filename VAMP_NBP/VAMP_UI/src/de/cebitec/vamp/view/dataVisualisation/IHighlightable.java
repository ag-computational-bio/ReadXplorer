/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.view.dataVisualisation;

import java.awt.Rectangle;

/**
 * By implementing this interface the class gains a method for setting the
 * rectangle to highlight. How this information is used can still be decided by
 * the class.
 *
 * @author Rolf Hilker
 */
public interface IHighlightable {

    public void setHighlightRectangle(Rectangle rect);
}
