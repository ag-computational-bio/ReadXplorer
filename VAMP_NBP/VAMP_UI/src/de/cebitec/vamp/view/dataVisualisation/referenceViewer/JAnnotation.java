package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.view.dialogMenus.MenuItemFactory;
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
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Contains the content of an annotation and takes care of the painting process.
 * Also contains its popup menu.
 *
 * @author ddoppmeier, rhilker
 */
public class JAnnotation extends JComponent {

    private static final long serialVersionUID = 347348234;
    private PersistantAnnotation annotation;
    private Dimension size;
    public static final int NORMAL_HEIGHT = 12;
    public static final int PARENT_ANNOTATION_HEIGHT = 8;
    public static final byte BORDER_NONE = 0;
    public static final byte BORDER_LEFT = -1;
    public static final byte BORDER_RIGHT = 1;
    public static final byte BORDER_BOTH = 2;
    private int height;
    private Font font;
    private Color color;
    private short border;

    /**
     * A component for displaying an annotation.
     * @param annotation the annotation to display
     * @param length length of the annotation on the screen
     * @param refViewer the reference viewer on which the annotation is displayed
     * @param border value among JAnnotation.BORDER_NONE, JAnnotation.BORDER_LEFT, JAnnotation.BORDER_RIGHT, JAnnotation.BORDER_BOTH
     */
    public JAnnotation(final PersistantAnnotation annotation, double length, final ReferenceViewer refViewer, short border) {
        super();
        this.annotation = annotation;
        this.height = NORMAL_HEIGHT;
        this.size = new Dimension((int) length, height);
        this.setSize(size);
        this.font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        this.color = this.determineColor(annotation);
        this.border = border;

        this.addListeners(refViewer);
        this.setToolTipText(createToolTipText());
    }

    public PersistantAnnotation getPersistantAnnotation() {
        return annotation;
    }

    private String createToolTipText() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<table>");

        sb.append(createTableRow("Locus", annotation.getLocus()));
        sb.append(createTableRow("Type", annotation.getType().getTypeString()));
        sb.append(createTableRow("Strand", (annotation.getStrand() == 1 ? "forward" : "reverse")));
        sb.append(createTableRow("Start", String.valueOf(annotation.getStart())));
        sb.append(createTableRow("Stop", String.valueOf(annotation.getStop())));
        if (annotation.getProduct() != null && !annotation.getProduct().isEmpty()) {
            sb.append(createTableRow("Product", annotation.getProduct()));
        }
        if (annotation.getEcNumber() != null && !annotation.getEcNumber().isEmpty()) {
            sb.append(createTableRow("EC no.", annotation.getEcNumber()));
        }

        sb.append("</table>");
        sb.append("</html>");
        return sb.toString();
    }

    private String createTableRow(String label, String value) {
        return "<tr><td align=\"right\"><b>" + label + ":</b></td><td align=\"left\">" + value + "</td></tr>";
    }

    public void setSelected(boolean selected) {
        if (selected) {
            color = ColorProperties.SELECTED_ANNOTATION;
        } else {
            color = this.determineColor(annotation);
        }
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        // draw the rectangle
        g.setColor(color);
        if (annotation.getSubAnnotations().isEmpty()){
            g.fillRect(0, 0, this.getSize().width, this.height);
            g.setColor(ColorProperties.EXON_BORDER);
            g.drawRect(0, 0, this.getSize().width-1, this.height-1);
            //paint border in annotation color, if annotation is larger than screen at that border
            g.setColor(color);
            this.overpaintBorder(g, 0, this.height-1);
        } else { //annotations with sub annotations have a smaller height
            g.fillRect(0, (NORMAL_HEIGHT-PARENT_ANNOTATION_HEIGHT)/2, this.getSize().width, PARENT_ANNOTATION_HEIGHT);
            g.setColor(ColorProperties.EXON_BORDER);
            g.drawRect(0, (NORMAL_HEIGHT-PARENT_ANNOTATION_HEIGHT)/2, this.getSize().width-1, PARENT_ANNOTATION_HEIGHT-1);
            g.setColor(color);
            this.overpaintBorder(g, (NORMAL_HEIGHT-PARENT_ANNOTATION_HEIGHT)/2 + 1, PARENT_ANNOTATION_HEIGHT);
        }

        // draw the locus of the annotation inside the rectangle
        g.setColor(ColorProperties.ANNOTATION_LABEL);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int fontY = this.getHeight() / 2 - 2 + fm.getMaxAscent() / 2;
        if (annotation.hasLocus()){
            String label = this.determineLabel(annotation.getLocus(), fm);
            g.drawString(label, 5, fontY);
        }

    }
    
    /**
     * Overpaints the border of the annotation again with a line, if it is larger 
     * than the screen and continues at the border.
     * @param g graphics object to paint on
     * @param y1 first y value of the line to draw
     * @param y2 second y value of the line to draw
     */
    private void overpaintBorder(Graphics2D g, int y1, int y2) {
        switch (this.border) {
            case JAnnotation.BORDER_BOTH:
                g.drawLine(0, y1, 0, y2);
                g.drawLine(this.getSize().width-1, y1, this.getSize().width-1, y2);
                break;
            case JAnnotation.BORDER_LEFT:
                g.drawLine(0, y1, 0, y2);
                break;
            case JAnnotation.BORDER_RIGHT:
                g.drawLine(this.getSize().width-1, y1, this.getSize().width-1, y2);
                break;
            default:
                break;
        }
    }

    private String determineLabel(String text, FontMetrics fm) {
        // cut down the string if it extends the width of this component
        if (fm.stringWidth(text) > this.getWidth() - 10) {
            while (fm.stringWidth(text + "...") > this.getWidth() - 10 && text.length() > 0) {
                text = text.substring(0, text.length() - 1);
            }
            text += "...";
        }
        return text;
    }

    /**
     * Set the color an annotation is displayed with. Depends on the annotation type,
     * @param annotation the annotation
     * @return the color for this annotation
     */
    private Color determineColor(PersistantAnnotation annotation) {
        Color c;
        
        if (annotation.getType() == FeatureType.CDS) {
            c = ColorProperties.CDS;
        } else if (annotation.getType() == FeatureType.MRNA) {
            c = ColorProperties.MRNA;
        } else if (annotation.getType() == FeatureType.MISC_RNA) {
            c = ColorProperties.MISC_RNA;
        } else if (annotation.getType() == FeatureType.REPEAT_UNIT) {
            c = ColorProperties.REPEAT_UNIT;
        } else if (annotation.getType() == FeatureType.RRNA) {
            c = ColorProperties.RRNA;
        } else if (annotation.getType() == FeatureType.SOURCE) {
            c = ColorProperties.SOURCE;
        } else if (annotation.getType() == FeatureType.TRNA) {
            c = ColorProperties.TRNA;
        } else if (annotation.getType() == FeatureType.GENE) {
            c = ColorProperties.GENE;
        } else if (annotation.getType() == FeatureType.MIRNA) {
            c = ColorProperties.MI_RNA;
        } else if (annotation.getType() == FeatureType.EXON) {
            c = ColorProperties.EXON;
        } else if (annotation.getType() == FeatureType.UNDEFINED) {
            c = ColorProperties.UNDEF_ANNOTATION;
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown type for annotation {0}", annotation.getType());
            c = ColorProperties.UNDEF_ANNOTATION;
        }

        return c;
    }

    private void addListeners(final ReferenceViewer refViewer) {
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1){
                    refViewer.setSelectedAnnotation(JAnnotation.this);
                }
                showPopUp(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopUp(e);
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

            private void showPopUp(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger())) {
                    final Lookup.Result<ReferenceViewer> resultReferenceView = Utilities.actionsGlobalContext().lookupResult(ReferenceViewer.class);
                    final ReferenceViewer viewer = (ReferenceViewer) resultReferenceView.allInstances().iterator().next();

                    JPopupMenu popUp = new JPopupMenu();

                    //add thumbnail view options
                    final IThumbnailView thumb = Lookup.getDefault().lookup(IThumbnailView.class);
                    if (thumb != null) {
                        thumb.showPopUp(annotation, refViewer, e, popUp);
                    }

                    MenuItemFactory menuItemFactory = new MenuItemFactory();

                    //add copy option
                    String selAnnotationSequence = viewer.getReference().getSequence().substring(annotation.getStart() - 1, annotation.getStop());
                    popUp.add(menuItemFactory.getCopyItem(selAnnotationSequence));

                    //add store as fasta file option
                    popUp.add(menuItemFactory.getStoreFastaItem(selAnnotationSequence, annotation));

                    popUp.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                refViewer.forwardChildrensMousePosition(e.getX(), JAnnotation.this);
            }
        });
    }
    
}
