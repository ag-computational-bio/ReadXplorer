package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.ColorProperties;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.parser.reference.Filter.FeatureType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 *
 * @author ddoppmei
 */
public class Feature extends JComponent{

    private static final long serialVersionUID = 347348234;

    private PersistantFeature f;
    private Dimension size;
    public static final int height = 12;
    private Font font;
    private Color color;

    public Feature(final PersistantFeature f, double length, final ReferenceViewer genomeViewer){
        super();
        this.f = f;
        size = new Dimension((int) length, height);
        this.setSize(size);
        font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        color = determineColor(f);

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                genomeViewer.setSelectedFeature(Feature.this);
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
        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                genomeViewer.forwardChildrensMousePosition(e.getX(), Feature.this);
            }
        });
        this.setToolTipText(createToolTipText());
    }

    public PersistantFeature getPersistantFeature(){
        return f;
    }

    private String createToolTipText(){
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<table>");
        
        sb.append(createTableRow("Locus", f.getLocus()));
        sb.append(createTableRow("Type", FeatureType.getTypeString(f.getType())));
        sb.append(createTableRow("Strand", (f.getStrand() == 1 ? "forward" : "reverse")));
        sb.append(createTableRow("Start", String.valueOf(f.getStart())));
        sb.append(createTableRow("Stop", String.valueOf(f.getStop())));
        if(f.getProduct() != null && !f.getProduct().isEmpty()){
            sb.append(createTableRow("Product", f.getProduct()));
        }
        if(f.getEcNumber() != null && !f.getEcNumber().isEmpty()){
            sb.append(createTableRow("EC no.", f.getEcNumber()));
        }
        
        sb.append("</table>");
        sb.append("</html>");
        return sb.toString();
    }
     
    private String createTableRow(String label, String value){
        return "<tr><td align=\"right\"><b>"+label+":</b></td><td align=\"left\">"+value+"</td></tr>";
    }

    public void setSelected(boolean b){
        if(b){
            color = ColorProperties.SELECTED_FEATURE;
        } else {
            color = determineColor(f);
        }
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics graphics){
        Graphics2D g = (Graphics2D) graphics;

        // draw the rectangle
        g.setColor(color);
        g.fillRect(0, 0, this.getSize().width, height);

        // draw the locus of the feature inside the rectangle
        g.setColor(ColorProperties.FEATURE_LABEL);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int fontY = this.getHeight() / 2 + fm.getMaxAscent() / 2;
        String label = determineLabel(f.getLocus(), fm);
        g.drawString(label, 5 ,fontY);
        
    }

    private String determineLabel(String text, FontMetrics fm){
        // cut down the string if it extends the width of this component
        if(fm.stringWidth(text) > this.getWidth()-10){
            while(fm.stringWidth(text+"...") > this.getWidth()-10 && text.length() > 0){
                text = text.substring(0, text.length()-1);
            }
            text += "...";
        }
        return text;
    }

    /**
     * Set the color a feature is displayed with. Depends on the feature's type,
     * @param f the feature
     * @return the color for this feature
     */
    private Color determineColor(PersistantFeature f){
        Color c;

        if(f.getType() == FeatureType.CDS){
            c = ColorProperties.CDS;
        } else if(f.getType() == FeatureType.M_RNA){
            c = ColorProperties.MRNA;
        } else if(f.getType() == FeatureType.MISC_RNA){
            c = ColorProperties.MISC_RNA;
        } else if(f.getType() == FeatureType.REPEAT_UNIT){
            c = ColorProperties.REPEAT_UNIT;
        } else if(f.getType() == FeatureType.R_RNA){
            c = ColorProperties.RRNA;
        } else if(f.getType() == FeatureType.SOURCE){
            c = ColorProperties.SOURCE;
        } else if(f.getType() == FeatureType.T_RNA){
            c = ColorProperties.TRNA;
        } else if(f.getType() == FeatureType.GENE){
            c = ColorProperties.GENE;
        }  else if(f.getType() == FeatureType.MI_RNA){
            c = ColorProperties.MI_RNA;
        } else if(f.getType() == FeatureType.UNDEFINED){
            c = ColorProperties.UNDEF_FEATURE;
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown type for feature {0}", f.getType());
            c = ColorProperties.UNDEF_FEATURE;
        }

        return c;
    }

}
