package de.cebitec.vamp.view.dataVisualisation.histogramViewer;

import de.cebitec.vamp.util.ColorProperties;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 *
 * @author ddoppmeier
 */
public class BarComponent extends JComponent {

    private final static long serialVersionUID = 38461064;

    private int height;
    private int width;
    private Color color;

    public BarComponent(int height, int width, Color color){
        super();
        this.height = height;
        this.width = width;
        this.color = color;
    }

    @Override
    public void paintComponent(Graphics graphics){
        graphics.setColor(color);
        graphics.fillRect(0, 0, width-1, height-1);

        graphics.setColor(ColorProperties.BLOCK_BORDER);
        graphics.drawRect(0, 0, width-1, height-1);
    }

}
