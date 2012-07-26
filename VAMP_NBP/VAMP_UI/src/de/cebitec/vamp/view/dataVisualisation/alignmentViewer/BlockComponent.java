package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.vamp.view.dialogMenus.MenuItemFactory;
import de.cebitec.vamp.view.dialogMenus.RNAFolderI;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;

/**
 *
 * @author ddoppmeier
 */
public class BlockComponent extends JComponent {

    private static final long serialVersionUID = 1324672345;
    private BlockI block;
    private int length;
    private int height;
    private AbstractViewer parentViewer;
    private GenomeGapManager gapManager;
    private int absLogBlockStart;
    private int absLogBlockStop;
    private int phyLeft;
    private int phyRight;
    private float percentSandBPerCovUnit;
    private float minSaturationAndBrightness;

    public BlockComponent(BlockI block, final AbstractViewer parentViewer, GenomeGapManager gapManager, int height, float minSaturationAndBrightness, float percentSandBPerCovUnit) {
        this.block = block;
        this.height = height;
        this.parentViewer = parentViewer;
        this.absLogBlockStart = block.getAbsStart();
        this.absLogBlockStop = block.getAbsStop();
        this.minSaturationAndBrightness = minSaturationAndBrightness;
        this.percentSandBPerCovUnit = percentSandBPerCovUnit;
        this.gapManager = gapManager;

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(absLogBlockStart);
        this.phyLeft = (int) bounds.getLeftPhysBound();
        // if there is a gap at the end of this block, phyRight shows the right bound of the gap (in viewer)
        // thus forgetting about every following matches, diffs, gaps whatever....
        this.phyRight = (int) parentViewer.getPhysBoundariesForLogPos(absLogBlockStop).getRightPhysBound();
        int numOfGaps = this.gapManager.getNumOfGapsAt(absLogBlockStop);
        int offset = (int) (numOfGaps * bounds.getPhysWidth());
        phyRight += offset;
        this.length = phyRight - phyLeft;

        this.setToolTipText(getText());

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu popUp = new JPopupMenu();
                    MenuItemFactory menuItemFactory = new MenuItemFactory();

                    final String mappingSequence = getSequence();
                    //add copy option
                    popUp.add(menuItemFactory.getCopyItem(mappingSequence));
                    //add copy position option
                    popUp.add(menuItemFactory.getCopyPositionItem(parentViewer.getCurrentMousePos()));
                    //add calculate secondary structure option
                    final RNAFolderI rnaFolderControl = Lookup.getDefault().lookup(RNAFolderI.class);
                    if (rnaFolderControl != null) {
                        popUp.add(menuItemFactory.getRNAFoldItem(rnaFolderControl, mappingSequence, this.getHeader()));
                    }

                    popUp.show((JComponent) e.getComponent(), e.getX(), e.getY());
                }
            }
            
            /**
             * Creates the header for the highlighted sequence.
             *
             * @return the header for the sequence
             */
            private String getHeader() {
                PersistantMapping mapping = (PersistantMapping) BlockComponent.this.block.getPersistantObject();
                final String strand = mapping.isFwdStrand() ? ">>" : "<<";
                HashMap<Integer, String> trackNames = ProjectConnector.getInstance().getOpenedTrackNames();
                String name = "Reference seq from ";
                if (trackNames.containsKey(mapping.getTrackId())) {
                    name += trackNames.get(mapping.getTrackId());
                }
                return name + " " + strand + " from " + absLogBlockStart + "-" + absLogBlockStop;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //not in use
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //not in use
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                parentViewer.forwardChildrensMousePosition(e.getX(), BlockComponent.this);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //not in use
            }
        });

    }

    public String getSequence() {
        int start = ((PersistantMapping) block.getPersistantObject()).getStart();
        int stop = ((PersistantMapping) block.getPersistantObject()).getStop();
        //string first pos is zero
        String readSequence = parentViewer.getReference().getSequence().substring(start-1, stop);
        return readSequence;
    }

    private String getText() {
        StringBuilder sb = new StringBuilder();
        PersistantMapping mapping = ((PersistantMapping) block.getPersistantObject());

        sb.append("<html>");
        sb.append("<table>");

        sb.append(createTableRow("Start", String.valueOf(mapping.getStart())));
        sb.append(createTableRow("Stop", String.valueOf(mapping.getStop())));
        sb.append(createTableRow("Replicates", String.valueOf(mapping.getNbReplicates())));
//        this.appendReadnames(mapping, sb); //no readnames are stored anymore: RUN domain excluded
        sb.append(createTableRow("Mismatches", String.valueOf(mapping.getDifferences())));
        this.appendDiffs(mapping, sb);
        this.appendGaps(mapping, sb);


        sb.append("</table>");
        sb.append("</html>");

        return sb.toString();
    }

    private void appendDiffs(PersistantMapping mapping, StringBuilder sb) {
        boolean printLabel = true;
        for (PersistantDiff d : mapping.getDiffs().values()) {
            String key = "";
            if (printLabel) {
                key = "Differences to reference";
                printLabel = false;
            }
            sb.append(createTableRow(key, d.getBase() + " at " + d.getPosition()));
        }
    }

    private void appendGaps(PersistantMapping mapping, StringBuilder sb) {
        boolean printLabel = true;
        for (Integer pos : mapping.getGenomeGaps().keySet()) {
            String key = "";
            if (printLabel) {
                key = "Reference insertions";
                printLabel = false;
            }
            StringBuilder tmp = new StringBuilder();
            for (PersistantReferenceGap g : mapping.getGenomeGapsAtPosition(pos)) {
                tmp.append(g.getBase()).append(", ");
            }
            tmp.deleteCharAt(tmp.toString().lastIndexOf(','));
            tmp.append(" at ").append(pos);
            String value = tmp.toString();
            sb.append(createTableRow(key, value));
        }

    }

    private String createTableRow(String key, String value) {
        if (!key.isEmpty()) {
            key += ":";
        }
        return "<tr><td align=\"right\">" + key + "</td><td align=\"left\">" + value + "</td>";
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));

        // paint this block's background
        graphics.setColor(determineBlockColor());
        graphics.fillRect(0, 0, length, height);

        Iterator<Brick> it = block.getBrickIterator();
        // only count Bricks, that are no genome gaps.
        //Used for determining location of brick in viewer
        int brickCount = 0;
        boolean gapPreceeding = false;
        while (it.hasNext()) {
            Brick brick = it.next();

            // only paint brick if mismatch
            int type = brick.getType();
            if (type != Brick.MATCH) {

                // get start of brick
                int logBrickStart = absLogBlockStart + brickCount;
                PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(logBrickStart);
                int x1 = (int) bounds.getLeftPhysBound() - phyLeft;
                String label = determineBrickLabel(brick);
                int labelWidth = graphics.getFontMetrics().stringWidth(label);
                int labelX = ((int) bounds.getPhyMiddle() - phyLeft) - labelWidth / 2;

                // if Brick before was a gap, this brick has the same position
                // in the genome as the gap. This forces the viewer to map to
                // the same position, which would lead to this Brick being painted
                // at the same location as the gap. So increase values manually
                if (gapPreceeding) {
                    x1 += bounds.getPhysWidth();
                    labelX += bounds.getPhysWidth();
                }

                graphics.setColor(determineBrickColor(brick));
                graphics.fillRect(x1, 0, (int) bounds.getPhysWidth(), height);

                graphics.setColor(ColorProperties.BRICK_LABEL);
                graphics.drawString(label, labelX, height);

                if (type == Brick.FOREIGN_GENOMEGAP
                        || type == Brick.GENOME_GAP_A
                        || type == Brick.GENOME_GAP_C
                        || type == Brick.GENOME_GAP_G
                        || type == Brick.GENOME_GAP_T
                        || type == Brick.GENOME_GAP_N) {
                    brickCount--;
                    gapPreceeding = true;
                } else {
                    gapPreceeding = false;
                }

            } else {
                gapPreceeding = false;
            }

            brickCount++;
        }
    }

    /**
     * Determines the color, brithness and saturation of a block.
     * @return 
     */
    private Color determineBlockColor() {
        PersistantMapping m = ((PersistantMapping) block.getPersistantObject());
        Color tmp;
        if (m.getDifferences() == 0) {
            tmp = ColorProperties.BLOCK_MATCH;
        } else if (m.isBestMatch()) {
            tmp = ColorProperties.BLOCK_BEST_MATCH;
        } else {
            tmp = ColorProperties.BLOCK_N_ERROR;
        }

        float[] values = Color.RGBtoHSB(tmp.getRed(), tmp.getGreen(), tmp.getBlue(), null);
        float sAndB = minSaturationAndBrightness + m.getNbReplicates() * percentSandBPerCovUnit;
        tmp = Color.getHSBColor(values[0], sAndB, sAndB);

        return tmp;
    }

    /**
     * Determines the label of a brick. This means the character representing
     * the base, the given brick stands for.
     * @param brick the brick whose label is needed
     * @return the character string representing the base of this brick
     */
    private String determineBrickLabel(Brick brick) {
        String label = " ";
        int type = brick.getType();
        switch (type) {
            case Brick.MATCH : label = ""; break;
            case Brick.BASE_A : label = "A"; break;
            case Brick.BASE_C : label = "C"; break;
            case Brick.BASE_G : label = "G"; break;
            case Brick.BASE_T : label = "T"; break;
            case Brick.BASE_N : label = "N"; break;
            case Brick.FOREIGN_GENOMEGAP : label = ""; break;
            case Brick.READGAP : label = "-"; break;
            case Brick.GENOME_GAP_A : label = "A"; break;
            case Brick.GENOME_GAP_C : label = "C"; break;
            case Brick.GENOME_GAP_G : label = "G"; break;
            case Brick.GENOME_GAP_T : label = "T"; break;
            case Brick.GENOME_GAP_N : label = "N"; break;
            case Brick.SKIPPED : label = "."; break;
            case Brick.UNDEF : label = "@"; 
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", type);
                break;
            default:
                label = "@";
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", type);
        }

        return label;
    }

    /**
     * Determines the color of a brick, if it deviates from the reference.
     * Matches are not taken into account in this method.
     * @param block the non-matching block whose color is needed
     * @return the color of the non-matching block
     */
    private Color determineBrickColor(Brick block) {
        Color c = Color.black;
        int type = block.getType();
        switch (type) {
            case Brick.BASE_A : c = ColorProperties.ALIGNMENT_A; break;
            case Brick.BASE_C : c = ColorProperties.ALIGNMENT_G; break;
            case Brick.BASE_G : c = ColorProperties.ALIGNMENT_C; break;
            case Brick.BASE_T : c = ColorProperties.ALIGNMENT_T; break;
            case Brick.BASE_N : c = ColorProperties.ALIGNMENT_N; break;
            case Brick.FOREIGN_GENOMEGAP : c = ColorProperties.ALIGNMENT_FOREIGN_GENOMEGAP; break;
            case Brick.READGAP : c = ColorProperties.ALIGNMENT_BASE_READGAP; break;
            case Brick.GENOME_GAP_A : c = ColorProperties.ALIGNMENT_A; break;
            case Brick.GENOME_GAP_C : c = ColorProperties.ALIGNMENT_C; break;
            case Brick.GENOME_GAP_G : c = ColorProperties.ALIGNMENT_G; break;
            case Brick.GENOME_GAP_T : c = ColorProperties.ALIGNMENT_T; break;
            case Brick.GENOME_GAP_N : c = ColorProperties.ALIGNMENT_N; break;
            case Brick.SKIPPED : c = ColorProperties.SKIPPED; break;
            case Brick.UNDEF : c = ColorProperties.ALIGNMENT_BASE_UNDEF;
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", type);
                break;
            default:
                c = ColorProperties.ALIGNMENT_BASE_UNDEF;
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", type);
        }
        
        return c;
    }

    public int getPhyStart() {
        return phyLeft;
    }

    public int getPhyWidth() {
        return length;
    }

    @Override
    public int getHeight() {
        return height;
    }
    
}
