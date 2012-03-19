package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfo;
import de.cebitec.vamp.view.dataVisualisation.GenomeGapManager;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.HighlightAreaListener;
import de.cebitec.vamp.view.dataVisualisation.HighlightableI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.openide.util.NbPreferences;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class SequenceBar extends JComponent implements HighlightableI {

    private static final long serialVersionUID = 23446398;
    private int height = 50;
    private AbstractViewer parentViewer;
    private PersistantReference refGen;
    private Font font;
    private FontMetrics metrics;
    private boolean printSeq;
    private int baseLineY;
    private int offsetY;
    private Rectangle highlightRect;
    private GenomeGapManager gapManager;
    private List<Region> codonHitsToHighlight;
    private List<Region> patternHitsToHighlight;
    // the width in bases (logical positions), that is used for marking
    // a value of 100 means every 100th base is marked by a large and every 50th
    // base is marked by a small bar
    private int markingWidth;
    private int halfMarkingWidth;
    private int largeBar;
    private int smallBar;
    private StartCodonFilter codonFilter;
    private PatternFilter patternFilter;
    private Preferences pref;
    private HighlightAreaListener highlightListener;
    private int frameCurrAnnotation;

    /**
     * Creates a new sequence bar instance.
     * @param parentViewer the viewer containing the sequence bar
     * @param refGen
     */
    public SequenceBar(AbstractViewer parentViewer, PersistantReference refGen) {
        super();
        this.parentViewer = parentViewer;
        this.setSize(new Dimension(0, this.height));
        this.font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        this.refGen = refGen;
        this.baseLineY = 30;
        this.offsetY = 10;
        this.largeBar = 11;
        this.smallBar = 7;
        this.markingWidth = 10;
        this.halfMarkingWidth = markingWidth / 2;
        this.codonHitsToHighlight = new ArrayList<Region>();
        this.patternHitsToHighlight = new ArrayList<Region>();
        this.codonFilter = new StartCodonFilter(parentViewer.getBoundsInfo().getLogLeft(), parentViewer.getBoundsInfo().getLogRight(), refGen);
        this.patternFilter = new PatternFilter(parentViewer.getBoundsInfo().getLogLeft(), parentViewer.getBoundsInfo().getLogRight(), refGen);
        this.initPrefListener();
        this.initHighlightListener();
    }

    /**
     * Updates the sequence bar according to the genetic code chosen.
     * After changing the genetic code, no start codons are be selected
     * anymore.
     */
    private void initPrefListener() {
        this.pref = NbPreferences.forModule(Object.class);
        this.pref.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (evt.getKey().equals(Properties.SEL_GENETIC_CODE)) {
                    SequenceBar.this.codonFilter.resetStartCodons();
                    SequenceBar.this.findCodons();
                }
            }
        });
    }

    /**
     * Adds a mouse listener to this sequence bar, which allows selecting the
     * sequence, currently displayed on the screen.
     */
    private void initHighlightListener() {
        highlightListener = new HighlightAreaListener(this, this.baseLineY, this.offsetY);
        this.addMouseListener(highlightListener);
        this.addMouseMotionListener(highlightListener);
    }

    public void setGenomeGapManager(GenomeGapManager gapManager) {
        this.gapManager = gapManager;
    }

    /**
     * Should be called, when the bounds have been changed. Updates the content
     * of the sequence bar.
     */
    public void boundsChanged() {
        this.adjustMarkingInterval();
        this.findCodons();
        this.findPattern();
        this.highlightListener.boundsChangedHook();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        BoundsInfo bounds = parentViewer.getBoundsInfo();
        PaintingAreaInfo info = parentViewer.getPaintingAreaInfo();

        g.setColor(ColorProperties.TRACKPANEL_MIDDLE_LINE);
        this.drawSequence(g);
        // draw a line indicating the sequence
        g.draw(new Line2D.Double(info.getPhyLeft(), baseLineY, info.getPhyRight(), baseLineY));

        // draw markings to indicate current parentViewerposition
        int temp = bounds.getLogLeft();
        temp += (halfMarkingWidth - temp % halfMarkingWidth);

        int logright = bounds.getLogRight();
        while (temp <= logright) {
            if (temp % markingWidth == 0) {
                this.drawThickLine(g, temp);
            } else {
                this.drawThinLine(g, temp);
            }
            temp += halfMarkingWidth;
        }

        //paint the hightlight rectangle if there is currently one
        if (this.highlightRect != null) {
            g.setColor(new Color(51, 153, 255));
            g.draw(this.highlightRect);
            g.setColor(new Color(168, 202, 236, 75));
            g.fill(this.highlightRect);
        }
    }

    /**
     * Draw sequence, if current zoom allows it.
     * @param g Graphics2D object to print on
     */
    private void drawSequence(Graphics2D g) {
        // get the font metrics
        g.setFont(font);
        metrics = g.getFontMetrics(font);

        // print sequence if sufficient space
        if (printSeq) {
            BoundsInfo bounds = parentViewer.getBoundsInfo();
            int logleft = bounds.getLogLeft();
            if (logleft < 1) { //might happen for very short reference sequences
                logleft = 1;
            }
            int logright = bounds.getLogRight();
            for (int i = logleft; i <= logright; i++) {
                this.drawChar(g, i);
                this.drawCharReverse(g, i);
            }
        }
    }

    /**
     * Draw base of the sequence.
     * @param g Graphics2D object to paint on
     * @param pos position of the base in the reference genome starting with 1 (not 0!).
     *      To get the correct base 1 is substracted from pos within this method.
     */
    private void drawChar(Graphics2D g, int pos) {
        // pos depents on slider value and cannot be smaller 1
        // since counting in strings starts with 0, we have to substract 1
        int basePosition = pos - 1;

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(pos);
        double physX = bounds.getPhyMiddle();
        if (gapManager != null && gapManager.hasGapAt(pos)) {
            int numOfGaps = gapManager.getNumOfGapsAt(pos);
            for (int i = 0; i < numOfGaps; i++) {
                int tmp = (int) (physX + i * bounds.getPhysWidth());
                String base = "-";
                int offset = metrics.stringWidth(base) / 2;
                g.drawString(base, (float) tmp - offset, (float) baseLineY - offsetY);
            }
            physX += numOfGaps * bounds.getPhysWidth();
        }
        String base = refGen.getSequence().substring(basePosition, basePosition + 1);
        int offset = metrics.stringWidth(base) / 2;
        /*BaseBackground b = new BaseBackground(12,5, base);
        b.setBounds((int)physX-offset,baseLineY-10,b.getSize().width, b.getSize().height);
        this.add(b);*/
        g.drawString(base, (float) physX - offset, (float) baseLineY - offsetY);
    }

    /**
     * draws the a character of the reverse strand of the sequence.
     * @param g the graphics object to paint on
     * @param pos position of the base in the reference genome
     */
    private void drawCharReverse(Graphics2D g, int pos) {
        // logX depents on slider value and cannot be smaller 1
        // since counting in strings starts with 0, we have to substract 1
        int basePosition = pos - 1;

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(pos);
        double physX = bounds.getPhyMiddle();
        if (gapManager != null && gapManager.hasGapAt(pos)) {
            int numOfGaps = gapManager.getNumOfGapsAt(pos);
            for (int i = 0; i < numOfGaps; i++) {
                int tmp = (int) (physX + i * bounds.getPhysWidth());
                String base = "-";
                int offset = metrics.stringWidth(base) / 2;
                g.drawString(base,
                        (float) tmp - offset,
                        (float) baseLineY + offsetY);
            }
            physX += numOfGaps * bounds.getPhysWidth();
        }
        String base = refGen.getSequence().substring(basePosition, basePosition + 1);
        String revBase = reverseBase(base);
        int offset = metrics.stringWidth(revBase) / 2;
        g.drawString(revBase,
                (float) physX - offset,
                (float) baseLineY + offsetY);
    }

    /**
     * draw a thick vertical line with length largeBar
     * @param g Graphics2D object to paint on
     * @param logPos logical position, that should be marked
     */
    private void drawThickLine(Graphics2D g, int logPos) {
        // draw a line and the label (position) in the middle of the space for this base
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(logPos);
        double physX = bounds.getPhyMiddle();
        if (gapManager != null && gapManager.hasGapAt(logPos)) {
            physX += gapManager.getNumOfGapsAt(logPos) * bounds.getPhysWidth();
        }
        g.draw(
                new Line2D.Double(
                physX, baseLineY - largeBar / 2, physX, baseLineY + largeBar / 2));

        String label = getRulerLabel(logPos);

        int offset = metrics.stringWidth(label) / 2;
        g.drawString(label, (float) physX - offset, (float) baseLineY + 2 * offsetY);
    }

    /**
     * Return the label for a marking position
     * @param logPos the position that is intended to be marked
     * @return the label used at that mark. 4000 is abbreviated by 4k, for example.
     */
    private String getRulerLabel(int logPos) {
        String label = null;
        if (logPos >= 1000 && markingWidth >= 1000) {
            if (logPos % 1000 == 0) {
                label = String.valueOf(logPos / 1000);
            } else if (logPos % 500 == 0) {
                label = String.valueOf(logPos / 1000);
                label += ".5";
            }
            label += "K";

        } else {
            label = String.valueOf(logPos);
        }

        return label;
    }

    /**
     * draw a thin vertical line with length smallBar
     * @param g Graphics2D object to paint on
     * @param logPos logical position, that should be marked
     */
    private void drawThinLine(Graphics2D g, int logPos) {
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(logPos);
        double physX = bounds.getPhyMiddle();
        if (gapManager != null && gapManager.hasGapAt(logPos)) {
            physX += gapManager.getNumOfGapsAt(logPos) * bounds.getPhysWidth();
        }

        g.draw(new Line2D.Double(
                physX, baseLineY - smallBar / 2, physX, baseLineY + smallBar / 2));
    }

    /**
     * Adjust the width that is used for marking bases to the current size
     */
    private void adjustMarkingInterval() {
        if (parentViewer.isInMaxZoomLevel()) {
            printSeq = true;
        } else {
            printSeq = false;
        }

        // asume 50 px for label and leave a gap of 30 px to next label
        int labelWidth = 50;
        labelWidth += 30;

        // pixels available per base
        double pxPerBp = (double) parentViewer.getPaintingAreaInfo().getPhyWidth() / parentViewer.getBoundsInfo().getLogWidth();

        if (10 * pxPerBp > labelWidth) {
            markingWidth = 10;
        } else if (20 * pxPerBp > labelWidth) {
            markingWidth = 20;
        } else if (50 * pxPerBp > labelWidth) {
            markingWidth = 50;
        } else if (100 * pxPerBp > labelWidth) {
            markingWidth = 100;
        } else if (250 * pxPerBp > labelWidth) {
            markingWidth = 250;
        } else if (500 * pxPerBp > labelWidth) {
            markingWidth = 500;
        } else if (1000 * pxPerBp > labelWidth) {
            markingWidth = 1000;
        } else if (5000 * pxPerBp > labelWidth) {
            markingWidth = 5000;
        } else if (10000 * pxPerBp > labelWidth) {
            markingWidth = 10000;
        }

        halfMarkingWidth = markingWidth / 2;
    }

    /**
     * Identifies the start codons according to the currently selected codons to show.
     */
    public void findCodons() {
        this.removeAll(JRegion.START_CODON);
        this.codonFilter.setInterval(this.parentViewer.getBoundsInfo().getLogLeft(), this.parentViewer.getBoundsInfo().getLogRight());
        this.determineFrame();

        this.codonFilter.setCurrAnnotationData(frameCurrAnnotation);
        this.codonHitsToHighlight = this.codonFilter.findRegions();
        for (Region r : this.codonHitsToHighlight) {

            BoundsInfo bounds = this.parentViewer.getBoundsInfo();
            int from = this.getStart(bounds, r);
            int to = this.getStop(bounds, r);
            
            int length = to - from + 1;
            // make sure it is visible when using high zoom levels
            if (length < 3) {
                length = 3;
            }
            JRegion jreg = new JRegion(length, 10);


            if (r.isForwardStrand()) {
                jreg.setBounds(from, baseLineY - jreg.getSize().height - 6, jreg.getSize().width, jreg.getSize().height);
            } else {
                jreg.setBounds(from, baseLineY + 4, jreg.getSize().width, jreg.getSize().height);
            }
            this.add(jreg);
        }
        this.repaint();
    }
    
    /**
     * Identifies the currently in this object stored pattern in the genome sequence.
     * @return position of the next occurence of the pattern from the current position on.
     */
    public int findPattern() {
        this.removeAll(JRegion.PATTERN);
        this.patternFilter.setInterval(this.parentViewer.getBoundsInfo().getLogLeft(), this.parentViewer.getBoundsInfo().getLogRight());
        //this.determineFrame();

        //this.patternFilter.setCurrAnnotationData(frameCurrAnnotation);
        this.patternHitsToHighlight = this.patternFilter.findRegions();
        BoundsInfo bounds = this.parentViewer.getBoundsInfo();
        
        for (Region r : this.patternHitsToHighlight) {

            int from = this.getStart(bounds, r);
            int to = this.getStop(bounds, r);

            int length = to - from + 1;
            // make sure it is visible when using high zoom levels
            if (length < 3) {
                length = 3;
            }
            JRegion jreg = new JRegion(length, 10, JRegion.PATTERN, ColorProperties.PATTERN);


            if (r.isForwardStrand()) {
                jreg.setBounds(from, baseLineY - jreg.getSize().height - 6, jreg.getSize().width, jreg.getSize().height);
            } else {
                jreg.setBounds(from, baseLineY + 4, jreg.getSize().width, jreg.getSize().height);
            }
            this.add(jreg);
        }
        this.repaint();
        
        if (this.patternHitsToHighlight.isEmpty()){
            return this.patternFilter.findNextOccurrence();
        } else {
            return -2;
        }
    }
    
    public int findNextPatternOccurrence(){
        return this.patternFilter.findNextOccurrence();
    }

    /**
     * Determines the frame of the currently selected annotation. if there is none it
     * is set to 10.
     */
    private void determineFrame() {
        this.frameCurrAnnotation = StartCodonFilter.INIT;//if it is 10 later, no selected annotation exists yet!
        if (this.parentViewer instanceof ReferenceViewer) {
            ReferenceViewer refViewer = (ReferenceViewer) this.parentViewer;
            if (refViewer.getCurrentlySelectedAnnotation() != null) {
                frameCurrAnnotation = refViewer.determineFrame(refViewer.getCurrentlySelectedAnnotation().getPersistantAnnotation());
            }
        }
    }
    

    private int getStart(BoundsInfo bounds, Region r) {
        int start = r.getStart();
        if (start < bounds.getLogLeft()) {
            start = bounds.getLogLeft();
        }
        return (int) parentViewer.getPhysBoundariesForLogPos(start).getLeftPhysBound();
    }
    
    
    private int getStop(BoundsInfo bounds, Region r){
            int stop = r.getStop();
            if (stop > bounds.getLogRight()) {
                stop = bounds.getLogRight();
            }
            PhysicalBaseBounds stopBounds = parentViewer.getPhysBoundariesForLogPos(stop);
            int to = (int) stopBounds.getRightPhysBound();

            if (gapManager != null && gapManager.hasGapAt(stop)) {
                to = (int) (gapManager.getNumOfGapsAt(stop) * stopBounds.getPhysWidth());
            }
            return to;
    }

    /**
     * Calculates which codons should be highlighted and updates the gui.
     * @param i the index of the codon to update
     * @param isSelected true, if the codon should be selected
     */
    public void showCodons(final int i, final boolean isSelected) {
        this.codonFilter.setCodonSelected(i, isSelected);
        this.findCodons();
    }

    /**
     * Returns if the codon with the index i is currently selected.
     * @param i the index of the codon
     * @return true, if the codon with the index i is currently selected
     */
    public boolean isCodonShown(final int i) {
        return this.codonFilter.isCodonSelected(i);
    }

    /**
     * 
     * @param pattern Pattern to search for
     */
    public int setPattern(String pattern) {
        this.patternFilter.setPattern(pattern);
        return this.findPattern();
    }

    /**
     * Paints the background of each base with a base specific color.
     * Before calling this method make sure to call "removeAll" on this sequence
     * bar! Otherwise the colors accumulate.
     * @param logX
     */
    public void paintBaseBackgroundColor(int logX) {
        int basePosition = 0;
        if (logX > 0) {
            basePosition = logX - 1;
        }
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(logX);
        if (bounds != null) {
            double physX = bounds.getPhyMiddle();
            if (gapManager != null && gapManager.hasGapAt(logX)) {
                int numOfGaps = gapManager.getNumOfGapsAt(logX);
                physX += numOfGaps * bounds.getPhysWidth();
            }

            String base = refGen.getSequence().substring(basePosition, basePosition + 1);

            if (base != null && metrics != null) {
                int offset = metrics.stringWidth(base) / 2;
                BaseBackground b = new BaseBackground(12, 12, base);
                b.setBounds((int) physX - offset, baseLineY - 18, b.getSize().width, b.getSize().height);
                this.add(b);
                BaseBackground brev = new BaseBackground(12, 12, reverseBase(base));
                brev.setBounds((int) physX - offset, baseLineY + 2, b.getSize().width, b.getSize().height);
                this.add(brev);
            }
        }
    }

    public String reverseBase(String base) {
        String revBase;
        if (base.equals("a")) {
            revBase = "t";
        } else if (base.equals("t")) {
            revBase = "a";
        } else if (base.equals("g")) {
            revBase = "c";
        } else if (base.equals("c")) {
            revBase = "g";
        } else if (base.equals("n")) {
            revBase = "n";
        } else if (base.equals("-")) {
            revBase = "-";
        } else {
            revBase = base;
        }
        return revBase;
    }

    /**
     * Sets the rectangle used for highlighting something in this sequence bar.
     * @param rect the rectangle to set
     */
    @Override
    public void setHighlightRectangle(final Rectangle rect) {
        this.highlightRect = rect;
        this.repaint();
    }

    /**
     * Returns the persistant reference used for this sequence bar.
     * @return the persistant reference used for this sequence bar
     */
    public PersistantReference getPersistantReference() {
        return this.refGen;
    }

    /**
     * @return The bounds info of the parent viewer
     */
    public BoundsInfo getViewerBoundsInfo() {
        return this.parentViewer.getBoundsInfo();
    }

    /**
     * @return the base width defined in the parent viewer.
     */
    public double getBaseWidth() {
        return this.parentViewer.getBaseWidth();
    }

    /**
     * @return the horizontal margin of the parent viewer.
     */
    public int getViewerHorizontalMargin() {
        return this.parentViewer.getHorizontalMargin();
    }

    /**
     * This method is to be called, when a mouse listener associated to this component
     * registered a mouse moved event.
     * @param e the mouse event which triggered this call
     */
    public void updateMouseListeners(MouseEvent e) {
        for (MouseMotionListener mml : this.parentViewer.getMouseMotionListeners()) {
            mml.mouseMoved(e);
            this.setToolTipText(this.parentViewer.getToolTipText());
        }
    }

    /**
     * @return The frame of the current annotation
     */
    public int getFrameCurrAnnotation() {
        return this.frameCurrAnnotation;
    }

    /**
     * Removes all JRegions from this component of a given type.
     * Currently JRegion supports 1 = Start codon and 2 = pattern types.
     * @param type type of components to remove
     */
    private void removeAll(int type) {
        for (Component comp : this.getComponents()){
            if (comp instanceof JRegion && ((JRegion) comp).getType() == type){
                this.remove(comp);
            }
        }
    }
}
