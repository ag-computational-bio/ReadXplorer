package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import de.cebitec.vamp.ColorProperties;

/**
 *
 * @author ddoppmeier
 */
public class JRegion extends JComponent{

    private static final long serialVersionUID = 279564654;


    public JRegion(int length, int height){
        super();
        this.setSize(new Dimension(length, height));
    }

    @Override
    protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        graphics.setColor(ColorProperties.START_CODON);
        graphics.fillRect(0, 0, this.getSize().width-1, this.getSize().height-1);
    }



}
