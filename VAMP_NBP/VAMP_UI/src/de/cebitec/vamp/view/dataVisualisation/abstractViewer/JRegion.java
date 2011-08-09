package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.util.ColorProperties;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 *
 * @author ddoppmeier
 */
public class JRegion extends JComponent{

    private static final long serialVersionUID = 279564654;
    public static final int START_CODON = 1;
    public static final int PATTERN = 2;
    
    private Color backgroundColor = ColorProperties.START_CODON;
    private int type = JRegion.START_CODON;

    public JRegion(int length, int height){
        super();
        this.setSize(new Dimension(length, height));
    }
    
    public JRegion(int length, int height, int type, Color backgroundColor){
        this(length, height);
        this.backgroundColor = backgroundColor;
        this.type = type;
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
