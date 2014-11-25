package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;

import de.cebitec.readXplorer.util.ColorProperties;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 * Creates a colored rectangle as background for a DNA base in a base specific
 * color.
 *
 * @author jstraube, Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class BaseBackground extends JComponent{

    private static final long serialVersionUID = 27956465;
    private String base = null;

    /**
     * Creates a colored rectangle as background for a DNA base in a base
     * specific color.
     * @param width Width of a single base in pixels.
     * @param height Height of a single base in pixels
     * @param base Base for which the background shall be created
     */
    public BaseBackground(int width, int height, String base){
        super();
        this.setSize(new Dimension(width, height));
        this.base = base;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      
        super.paintComponent(graphics);
        switch (base) {
            case "A": graphics.setColor(ColorProperties.BACKGROUND_A); break;
            case "C": graphics.setColor(ColorProperties.BACKGROUND_C); break;
            case "G": graphics.setColor(ColorProperties.BACKGROUND_G); break;
            case "T": graphics.setColor(ColorProperties.BACKGROUND_T); break;
            case "-": graphics.setColor(ColorProperties.BACKGROUND_READGAP); break;
            case "N": graphics.setColor(ColorProperties.BACKGROUND_N); break;
            default:  graphics.setColor(ColorProperties.BACKGROUND_BASE_UNDEF);
        }

        graphics.fillRect(0, 0, this.getSize().width - 1, this.getSize().height - 1);
    }

}
