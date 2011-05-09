package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.util.ColorProperties;
//import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantDiff;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
//import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

/**
 *
 * @author ddoppmeier
 */
public class BlockComponent extends JComponent implements ActionListener {

    private static final long serialVersionUID = 1324672345;
    private BlockI b;
    private int length;
    private int height;
    private AlignmentViewer parentViewer;
    private GenomeGapManager gapManager;
    private int absLogBlockStart;
    private int absLogBlockStop;
    private int phyLeft;
    private int phyRight;
    private float percentSandBPerCovUnit;
    private float minSaturationAndBrightness;
    private String nameofRead = "";
//    private static String COPY_READNAME = "Copy readname"; //no readnames are stored anymore: RUN domain excluded
    private static String COPY_SEQUENCE = "Copy sequence";
    private static String EXIT_POPUP = "Exit popup menu";
    private JPopupMenu p = new JPopupMenu();
    private Point mousePoint = new Point();

    public BlockComponent(BlockI b, final AlignmentViewer parentViewer, GenomeGapManager gapManager, int height, float minSaturationAndBrightness, float percentSandBPerCovUnit) {
        this.b = b;
        this.height = height;
        this.parentViewer = parentViewer;
        absLogBlockStart = b.getAbsStart();
        absLogBlockStop = b.getAbsStop();
        this.minSaturationAndBrightness = minSaturationAndBrightness;
        this.percentSandBPerCovUnit = percentSandBPerCovUnit;
        this.gapManager = gapManager;

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(absLogBlockStart);
        this.phyLeft = (int) bounds.getLeftPhysBound();
        // if there is a gap at the end of this block, phyRight shows the right bound of the gap (in viewer)
        // thus forgetting about every following matches, diffs, gaps whatever....
        this.phyRight = (int) parentViewer.getPhysBoundariesForLogPos(absLogBlockStop).getRightPhysBound();
        setPopupMenu();
        int numOfGaps = this.gapManager.getNumOfGapsAt(absLogBlockStop);
        int offset = (int) (numOfGaps * bounds.getPhysWidth());
        phyRight += offset;
        this.length = phyRight - phyLeft;

        this.setToolTipText(getText());

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
               mousePoint = e.getLocationOnScreen();
               p.setLocation(mousePoint);
               p.setVisible(true);

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

//    public void setReadname() { //no readnames are stored anymore: RUN domain excluded
//        List<String> names = ProjectConnector.getInstance().getReadNamesForSequenceID(b.getMapping().getSequenceID());
//        JTextField j = new JTextField();
//        for (String name : names) {
//            nameofRead = name;
//        }
//        j.setText(nameofRead);
//        j.selectAll();
//        j.copy();
//    }

    public void setSequence() {
        JTextField j = new JTextField();
        int start = b.getMapping().getStart();
        int stop = b.getMapping().getStop();
        String readSequence = parentViewer.getRefGen().getSequence().substring(start, stop);
        j.setText(readSequence);
        j.selectAll();
        j.copy();
    }

    private String getText() {
        StringBuilder sb = new StringBuilder();
        PersistantMapping mapping = b.getMapping();

        sb.append("<html>");
        sb.append("<table>");

        sb.append(createTableRow("Start", String.valueOf(mapping.getStart())));
        sb.append(createTableRow("Stop", String.valueOf(mapping.getStop())));
        sb.append(createTableRow("Replicates", String.valueOf(mapping.getCoverage())));
//        this.appendReadnames(mapping, sb); //no readnames are stored anymore: RUN domain excluded
        sb.append(createTableRow("Mismatches", String.valueOf(mapping.getErrors())));
        this.appendDiffs(mapping, sb);
        this.appendGaps(mapping, sb);


        sb.append("</table>");
        sb.append("</html>");

        return sb.toString();
    }

    private void setPopupMenu() {
//        JMenuItem copyName = new JMenuItem(); //no readnames are stored anymore: RUN domain excluded
//        copyName.addActionListener(this);
//        copyName.setActionCommand(COPY_READNAME);
//        copyName.setText("Copy readname");

        JMenuItem copySequence = new JMenuItem();
        copySequence.addActionListener(this);
        copySequence.setActionCommand(COPY_SEQUENCE);
        copySequence.setText("Copy sequence");
        copySequence.setToolTipText("Attention! You copy the genome sequence");
        
        JMenuItem exit = new JMenuItem();
        exit.addActionListener(this);
        exit.setActionCommand(EXIT_POPUP);
        exit.setText("Exit");
        
 //       p.add(copyName); //no readnames are stored anymore: RUN domain excluded
        p.add(copySequence);
        p.add(exit);
    }

    //no readnames are stored anymore: RUN domain excluded
//    private void appendReadnames(PersistantMapping mapping, StringBuilder sb) {
//        List<String> names = ProjectConnector.getInstance().getReadNamesForSequenceID(mapping.getSequenceID());
//        boolean printLabel = true;
//        for (String name : names) {
//            String key = "";
//            if (printLabel) {
//                key = "Reads";
//                printLabel = false;
//            }
//            sb.append(createTableRow(key, name));
//        }
//    }

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

        Iterator<Brick> it = b.getBrickIterator();
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

    private Color determineBlockColor() {
        PersistantMapping m = b.getMapping();
        Color tmp;
        if (m.getErrors() == 0) {
            tmp = ColorProperties.BLOCK_MATCH;
        } else if (m.isBestMatch()) {
            tmp = ColorProperties.BLOCK_BEST_MATCH;
        } else {
            tmp = ColorProperties.BLOCK_N_ERROR;
        }

        float[] values = Color.RGBtoHSB(tmp.getRed(), tmp.getGreen(), tmp.getBlue(), null);
        float sAndB = minSaturationAndBrightness + m.getCoverage() * percentSandBPerCovUnit;
        tmp = Color.getHSBColor(values[0], sAndB, sAndB);

        return tmp;
    }

    private String determineBrickLabel(Brick brick) {
        String label = " ";
        int type = brick.getType();
        if (type == Brick.BASE_A) {
            label = "A";
        } else if (type == Brick.BASE_C) {
            label = "C";
        } else if (type == Brick.BASE_G) {
            label = "G";
        } else if (type == Brick.BASE_T) {
            label = "T";
        } else if (type == Brick.BASE_N) {
            label = "N";
        } else if (type == Brick.FOREIGN_GENOMEGAP) {
            label = "";
        } else if (type == Brick.MATCH) {
            label = "";
        } else if (type == Brick.UNDEF) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown brick type {0}", type);
            return "@";
        } else if (type == Brick.GENOME_GAP_A) {
            return "A";
        } else if (type == Brick.GENOME_GAP_C) {
            return "C";
        } else if (type == Brick.GENOME_GAP_G) {
            return "G";
        } else if (type == Brick.GENOME_GAP_T) {
            return "T";
        } else if (type == Brick.GENOME_GAP_N) {
            return "N";
        } else if (type == Brick.READGAP) {
            label = "-";
        }

        return label;
    }

    private Color determineBrickColor(Brick b) {
        Color c = Color.black;
        int type = b.getType();
        if (type == Brick.BASE_A) {
            c = ColorProperties.ALIGNMENT_A;
        } else if (type == Brick.BASE_C) {
            c = ColorProperties.ALIGNMENT_C;
        } else if (type == Brick.BASE_G) {
            c = ColorProperties.ALIGNMENT_G;
        } else if (type == Brick.BASE_T) {
            c = ColorProperties.ALIGNMENT_T;
        } else if (type == Brick.BASE_N) {
            c = ColorProperties.ALIGNMENT_N;
        } else if (type == Brick.FOREIGN_GENOMEGAP) {
            c = ColorProperties.ALIGNMENT_FOREIGN_GENOMEGAP;
        } else if (type == Brick.UNDEF) {
            c = ColorProperties.ALIGNMENT_BASE_UNDEF;
        } else if (type == Brick.GENOME_GAP_A) {
            c = ColorProperties.ALIGNMENT_A;
        } else if (type == Brick.GENOME_GAP_C) {
            c = ColorProperties.ALIGNMENT_C;
        } else if (type == Brick.GENOME_GAP_G) {
            c = ColorProperties.ALIGNMENT_G;
        } else if (type == Brick.GENOME_GAP_T) {
            c = ColorProperties.ALIGNMENT_T;
        } else if (type == Brick.GENOME_GAP_N) {
            c = ColorProperties.ALIGNMENT_N;
        } else if (type == Brick.READGAP) {
            c = ColorProperties.ALIGNMENT_BASE_READGAP;
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Found unknown type of brick {0}", b.getType());
            c = ColorProperties.ALIGNMENT_BASE_UNDEF;
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

    @Override
    public void actionPerformed(ActionEvent e) {
//        if (e.getActionCommand().equals(COPY_READNAME)) {
//            this.setReadname();
//            p.setVisible(false);
//        } //no readnames are stored anymore: RUN domain excluded
        if (e.getActionCommand().equals(COPY_SEQUENCE)) {
            this.setSequence();
            p.setVisible(false);
        }
        if(e.getActionCommand().equals(EXIT_POPUP)){
            p.setVisible(false);
        }
    }
    
}
