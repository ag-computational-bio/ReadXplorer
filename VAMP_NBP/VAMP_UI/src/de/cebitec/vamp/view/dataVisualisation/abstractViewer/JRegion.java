package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.util.Properties;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 * Represents a region, which is highlighted in a certain colour.
 *  
 * @author ddoppmeier
 */
public class JRegion extends JComponent{

    private static final long serialVersionUID = 279564654;
    
    private Color backgroundColor = ColorProperties.START_CODON;
    private int type = Properties.START;

    public JRegion(int length, int height, int type){
        super();
        this.setSize(new Dimension(length, height));
        this.type = type;
        
        if (type == Properties.PATTERN) {
            this.backgroundColor = ColorProperties.PATTERN;
        } else if (type == Properties.STOP) {
            this.backgroundColor = ColorProperties.STOP_CODON;
        } // else { //currently not needed, because start codon color already set.
    }

    @Override
    protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        graphics.setColor(this.backgroundColor);
        graphics.fillRect(0, 0, this.getSize().width-1, this.getSize().height-1);
    }

    /**
     * Sets the background color of this component
     * @param backgroundColor the background color to set
     */
    public void setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    
    public int getType() {
        return this.type;
    }    

}
