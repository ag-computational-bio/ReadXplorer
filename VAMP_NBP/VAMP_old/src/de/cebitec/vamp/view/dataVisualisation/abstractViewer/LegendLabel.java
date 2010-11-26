package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import de.cebitec.vamp.ColorProperties;

/**
 *
 * @author ddoppmeier
 */
public class LegendLabel extends JLabel {

    private static final long serialVersionUID = 2974452;
    private AbstractViewer parent;
    private boolean isShowingLabel;
    private Icon expandIcon;
    private Icon collapseIcon;

    public LegendLabel(AbstractViewer parent){
        super("Legend");
        this.parent = parent;
        isShowingLabel = false;
        expandIcon = new ImageIcon(this.getClass().getClassLoader().getResource("vamp/resources/expandIcon.png"));
        collapseIcon = new ImageIcon(this.getClass().getClassLoader().getResource("vamp/resources/collapseIcon.png"));
        this.setIcon(expandIcon);

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if(isShowingLabel){
                    isShowingLabel = false;
                    LegendLabel.this.setIcon(expandIcon);
                } else {
                    isShowingLabel = true;
                    LegendLabel.this.setIcon(collapseIcon);
                }
                LegendLabel.this.parent.updateLegendVisibility(isShowingLabel);
            }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g){
        g.setColor(ColorProperties.LEGEND_BACKGROUND);
        g.fillRect(0, 0, this.getSize().width-1, this.getSize().height-1);
        super.paintComponent(g);
    }
    
}
