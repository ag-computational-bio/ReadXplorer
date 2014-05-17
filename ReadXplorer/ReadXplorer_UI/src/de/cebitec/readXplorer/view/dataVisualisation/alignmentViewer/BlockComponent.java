/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import de.cebitec.readXplorer.databackend.SamBamFileReader;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantDiff;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantMapping;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.view.dataVisualisation.GenomeGapManager;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.readXplorer.view.dialogMenus.MenuItemFactory;
import de.cebitec.readXplorer.view.dialogMenus.RNAFolderI;
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
                    //add copy translated sequence option
                    popUp.add(menuItemFactory.getCopyTranslatedItem(mappingSequence));
                    //add copy position option
                    popUp.add(menuItemFactory.getCopyPositionItem(parentViewer.getCurrentMousePos()));
                    //add center current position option
                    popUp.add(menuItemFactory.getJumpToPosItem(parentViewer.getBoundsInformationManager(), parentViewer.getCurrentMousePos()));
                    //add calculate secondary structure option
                    final RNAFolderI rnaFolderControl = Lookup.getDefault().lookup(RNAFolderI.class);
                    if (rnaFolderControl != null) {
                        popUp.add(menuItemFactory.getRNAFoldItem(rnaFolderControl, mappingSequence, this.getHeader()));
                    }

                    popUp.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            
            /**
             * Creates the header for the highlighted sequence.
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

    /**
     * @return The reference sequence 
     */
    public String getSequence() {
        int start = ((PersistantMapping) block.getPersistantObject()).getStart();
        int stop = ((PersistantMapping) block.getPersistantObject()).getStop();
        //string first pos is zero
        String readSequence = parentViewer.getReference().getActiveChromSequence(start - 1, stop);
        return readSequence;
    }

    private String getText() {
        StringBuilder sb = new StringBuilder(150);
        PersistantMapping mapping = ((PersistantMapping) block.getPersistantObject());

        sb.append("<html>");
        sb.append("<table>");

        sb.append(createTableRow("Start", String.valueOf(mapping.getStart())));
        sb.append(createTableRow("Stop", String.valueOf(mapping.getStop())));
        sb.append(createTableRow("Replicates", String.valueOf(mapping.getNbReplicates())));
//        this.appendReadnames(mapping, sb); //no readnames are stored anymore: RUN domain excluded
        sb.append(createTableRow("Mismatches", String.valueOf(mapping.getDifferences())));
        int mappingQual = mapping.getMappingQuality() == -1 ? SamBamFileReader.DEFAULT_MAP_QUAL : mapping.getMappingQuality();
        sb.append(createTableRow("Mapping quality (Phred)", String.valueOf(mappingQual)));
        sb.append(createTableRow("Base qualities (Phred)", this.generateBaseQualString(mapping.getBaseQualities())));
        this.appendDiffs(mapping, sb);
        this.appendGaps(mapping, sb);
        
        if (mapping.isUnique()) {
            sb.append(createTableRow("Unique", "yes"));
        } else {
            sb.append(createTableRow("Unique", "no"));
        }
        sb.append(createTableRow("Number of mappings for read", mapping.getNumMappingsForRead() + ""));
        if (mapping.getOriginalSequence() != null) {
            sb.append(createTableRow("Original (full) sequence", mapping.getOriginalSequence()));
        }
        if (mapping.getTrimmedFromLeft() > 0) {
            sb.append(createTableRow("Trimmed chars from left", mapping.getTrimmedFromLeft() + ""));
        }
        if (mapping.getTrimmedFromRight() > 0) {
            sb.append(createTableRow("Trimmed chars from right", mapping.getTrimmedFromRight() + ""));
        }

        sb.append("</table>");
        sb.append("</html>");

        return sb.toString();
    }

    /**
     * @param baseQualities The array of phred scaled base qualities to convert
     * @return A String representation of the phred scaled base qualities in 
     * the array.
     */
    private String generateBaseQualString(byte[] baseQualities) {
        String baseQualString = "[";
        int aThird = baseQualities.length / 4;
        int current = aThird;
        for (int i = 0; i < baseQualities.length; i++) {
            baseQualString += baseQualities[i] + ",";
            if (i > current) {
                baseQualString += "<br>";
                current += aThird;
            }
        }
        if (baseQualString.endsWith(",")) {
            baseQualString = baseQualString.substring(0, baseQualString.length() - 1) + "]";
        } else if (baseQualString.endsWith("<br>")) {
            baseQualString = baseQualString.substring(0, baseQualString.length() - 5) + "]";
        }
        return baseQualString;
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
            StringBuilder tmp = new StringBuilder(10);
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
        String label;
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
            case Brick.TRIMMED : label = "⌿"; break;
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
        Color c;
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
            case Brick.TRIMMED : c = ColorProperties.TRIMMED; break;
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
