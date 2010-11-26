package de.cebitec.vamp.view.dataVisualisation.basePanel;

import de.cebitec.vamp.view.dataVisualisation.MousePositionListener;
import javax.swing.JPanel;

/**
 *
 * @author ddoppmeier
 */
public abstract class AbstractInfoPanel extends JPanel implements MousePositionListener{
    
    private static final long serialVersionUID = 1L;
    
    public abstract void close();


}
