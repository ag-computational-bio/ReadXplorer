package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.util.ColorProperties;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 *
 * @author jstraube
 */
public class BaseBackground extends JComponent{

    private static final long serialVersionUID = 27956465;
    private String   bases = null;

    public BaseBackground(int length, int height, String base){
        super();
        this.setSize(new Dimension(length, height));
       //bases = base.charAt(0);
         bases = base;
    }

    @Override
    @SuppressWarnings("fallthrough")
    protected void paintComponent(Graphics graphics) {
      
        super.paintComponent(graphics);
//        switch (bases) {
//            case 'a':  graphics.setColor(ColorProperties.BACKGROUND_A);
//            case 'c': graphics.setColor(ColorProperties.BACKGROUND_C);
//            case 'g': graphics.setColor(ColorProperties.BACKGROUND_G);
//            case 't': graphics.setColor(ColorProperties.BACKGROUND_T);
//            case '-': graphics.setColor(ColorProperties.BACKGROUND_READGAP);
//            case 'n':  graphics.setColor(ColorProperties.BACKGROUND_N);
//            default: graphics.setColor(ColorProperties.BACKGROUND_BASE_UNDEF);
//        }
        if (bases.equals("A")) {
            graphics.setColor(ColorProperties.BACKGROUND_A);
        } else if (bases.equals("C")) {
            graphics.setColor(ColorProperties.BACKGROUND_C);
        } else if (bases.equals("G")) {
            graphics.setColor(ColorProperties.BACKGROUND_G);
        } else if (bases.equals("T")) {
            graphics.setColor(ColorProperties.BACKGROUND_T);
        } else if (bases.equals("-")) {
            graphics.setColor(ColorProperties.BACKGROUND_READGAP);
        }else if (bases.equals("N")) {
            graphics.setColor(ColorProperties.BACKGROUND_N);
        }else {
            graphics.setColor(ColorProperties.BACKGROUND_BASE_UNDEF);
        }

        graphics.fillRect(0, 0, this.getSize().width-1, this.getSize().height-1);
    }

}
