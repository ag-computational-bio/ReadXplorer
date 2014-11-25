package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;

import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.Properties;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 * Represents a region, which is highlighted in a certain colour.
 *  
 * @author ddoppmeier
 */
public class JRegion extends JComponent {

    private static final long serialVersionUID = 279564654;
    
    private Color backgroundColor = ColorProperties.START_CODON;
    private int type = Properties.START;
    private final int genomeStart;
    private final int genomeStop;

    public JRegion(int length, int height, int type, int genomeStart, int genomeStop){
        super();
        this.setSize(new Dimension(length, height));
        this.type = type;
        this.genomeStart = genomeStart;
        this.genomeStop = genomeStop;
        
        if (type == Properties.PATTERN) {
            this.backgroundColor = ColorProperties.PATTERN;
        } else if (type == Properties.STOP) {
            this.backgroundColor = ColorProperties.STOP_CODON;
        } else if (type == Properties.CDS) {
            this.backgroundColor = ColorProperties.HIGHLIGHT_FILL;
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

    /**
     * @return The start position of this region in the genome.
     */
    public int getStart() {
        return genomeStart;
    }

    /**
     * @return The stop position of this region in the genome.
     */
    public int getStop() {
        return genomeStop;
    }

}
