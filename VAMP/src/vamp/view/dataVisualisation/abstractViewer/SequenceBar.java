package vamp.view.dataVisualisation.abstractViewer;

import vamp.view.dataVisualisation.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import vamp.ColorProperties;
import vamp.databackend.dataObjects.PersistantReference;


/**
 *
 * @author ddoppmeier
 */
public class SequenceBar extends JComponent{

    private static final long serialVersionUID = 23446398;
    private int height = 50;
    private AbstractViewer parentViewer;
    private PersistantReference refGen;
    private Font font;
    private FontMetrics metrics;
    private boolean printSeq;
    private int baseLineY;
    private GenomeGapManager gapManager;
    private List<Region> regionsToHighlight;

    // the width in bases (logical positions), that is used for marking
    // a value of 100 means every 100th base is marked by a large and every 50th
    // base is marked by a small bar
    private int markingWidth;
    private int halfMarkingWidth;
    private int largeBar;
    private int smallBar;

    private StartCodonFilter codonFilter;

    public SequenceBar(AbstractViewer parentViewer, PersistantReference refGen){
        super();
        this.parentViewer = parentViewer;
        this.setSize(new Dimension(0, height));
        font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        this.refGen = refGen;
        baseLineY = 30;
        largeBar = 11;
        smallBar = 7;
        markingWidth = 10;
        halfMarkingWidth = markingWidth /2;
        regionsToHighlight = new ArrayList<Region>();
        codonFilter = new StartCodonFilter(parentViewer.getBoundsInfo().getLogLeft(), parentViewer.getBoundsInfo().getLogRight(), refGen);
        
    }

    public void setGenomeGapManager(GenomeGapManager gapManager){
        this.gapManager = gapManager;
    }

    public void boundsChanged(){
        adjustMarkingIntervall();
        findCodons();
    }

    @Override
    protected void paintComponent(Graphics graphics){
        Graphics2D g = (Graphics2D) graphics;
        
        BoundsInfo bounds = parentViewer.getBoundsInfo();
        PaintingAreaInfo info = parentViewer.getPaintingAreaInfo();

        g.setColor(ColorProperties.TRACKPANEL_MIDDLE_LINE);
        drawRuler(g);
        // draw a line indicating the sequence
        g.draw(new Line2D.Double(info.getPhyLeft(),
                    baseLineY,
                    info.getPhyRight(),
                    baseLineY)
                );

        // draw markings to indicate current parentViewerposition
        int temp = bounds.getLogLeft();
        temp += (halfMarkingWidth - temp % halfMarkingWidth);

        int logright = bounds.getLogRight();
        while(temp <= logright){
            if(temp % markingWidth == 0){
                drawThickLine(g, temp);
            } else{
                drawThinLine(g, temp);
            }
            temp += halfMarkingWidth;
        }
    }

    /**
     * Draw a line in the middle of the area including markings for position and
     * sequence if possible.
     * @param g Graphics2D object to print on
     */
    private void drawRuler(Graphics2D g){
        // get the font metrics
        g.setFont(font);
        metrics = g.getFontMetrics(font);

        // print sequence if sufficient space
        if(printSeq){
            BoundsInfo bounds = parentViewer.getBoundsInfo();
            int logleft = bounds.getLogLeft();
            int logright = bounds.getLogRight();
            for(int i = logleft; i <= logright; i++){
                drawChar(g, i);
            }
        }
}

    /**
     * Draw base of the sequence
     * @param g Graphics2D object to paint on
     * @param logX position of the base in the reference genome
     */
    private void drawChar(Graphics2D g, int logX){
        // logX depents on slider value and cannot be smaller 1
        // since counting in strings starts with 0, we have to substract 1
        int basePosition = logX -1;

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(logX);
        double physX = bounds.getPhyMiddle();
        if(gapManager != null && gapManager.hasGapAt(logX)){
            int numOfGaps = gapManager.getNumOfGapsAt(logX);
            for(int i = 0; i < numOfGaps; i++){
                int tmp = (int) (physX + i * bounds.getPhysWidth());
                String base = "-";
                int offset = metrics.stringWidth(base) / 2;
                g.drawString(base,
                            (float) tmp-offset,
                            (float) baseLineY -10);
            }
            physX += numOfGaps * bounds.getPhysWidth();
        }
        String base = refGen.getSequence().substring(basePosition, basePosition+1);
        int offset = metrics.stringWidth(base) / 2;
        g.drawString(base,
                    (float) physX-offset,
                    (float) baseLineY -10);

    }
    
    /**
     * draw a thick vertical line with length largeBar
     * @param g Graphics2D object to paint on
     * @param logPos logical position, that should be marked
     */
    private void drawThickLine(Graphics2D g, int logPos){
        // draw a line and the label in the middle of the space for this base
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(logPos);
        double physX = bounds.getPhyMiddle();
        if(gapManager != null && gapManager.hasGapAt(logPos)){
            physX += gapManager.getNumOfGapsAt(logPos) * bounds.getPhysWidth();
        }
        g.draw(
            new Line2D.Double(
                physX, baseLineY -largeBar/2, physX, baseLineY +largeBar/2)
                );

        String label = getRulerLabel(logPos);

        int offset = metrics.stringWidth(label) / 2;
        g.drawString(
                label,
                (float) physX-offset,
                (float) baseLineY+20);
    }

    /**
     * Return the label for a marking position
     * @param logPos the position that is intended to be marked
     * @return the label used at that mark. 4000 is abbreviated by 4k, for example.
     */
    private String getRulerLabel(int logPos){
        String label = null;
        if(logPos >= 1000 && markingWidth >= 1000){
            if(logPos % 1000 == 0){
                label = String.valueOf(logPos/1000);
            } else if(logPos % 500 == 0){
                label = String.valueOf(logPos/1000);
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
    private void drawThinLine(Graphics2D g, int logPos){
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos(logPos);
        double physX = bounds.getPhyMiddle();
        if(gapManager != null && gapManager.hasGapAt(logPos)){
            physX += gapManager.getNumOfGapsAt(logPos) * bounds.getPhysWidth();
        }

        g.draw(new Line2D.Double(
                physX, baseLineY-smallBar/2, physX, baseLineY+smallBar/2)
                );
    }


        /**
     * Adjust the with that is used for marking bases to the current size
     */
    private void adjustMarkingIntervall(){

        if(parentViewer.isInMaxZoomLevel()){
            printSeq = true;
        } else {
            printSeq = false;
        }

        // asume 50 px for label and leave a gap of 30 px to next label
        int labelWidth = 50; 
        labelWidth += 30;

        // pixels available per base
        double pxPerBp =   (double) parentViewer.getPaintingAreaInfo().getPhyWidt() / parentViewer.getBoundsInfo().getLogWidth();

        if(10 * pxPerBp > labelWidth){
            markingWidth = 10;
        } else if(20 * pxPerBp > labelWidth){
            markingWidth = 20;
        } else if(50 * pxPerBp > labelWidth){
            markingWidth = 50;
        } else if(100 * pxPerBp > labelWidth){
            markingWidth = 100;
        } else if(250 * pxPerBp > labelWidth){
            markingWidth = 250;
        } else if(500 * pxPerBp > labelWidth){
            markingWidth = 500;
        } else if(1000 * pxPerBp > labelWidth){
            markingWidth = 1000;
        } else if(5000 * pxPerBp > labelWidth){
            markingWidth = 5000;
        } else if(10000 * pxPerBp > labelWidth){
            markingWidth = 10000;
        }

        halfMarkingWidth = markingWidth / 2;
    }

    private void findCodons(){
        this.removeAll();
        codonFilter.setIntervall(parentViewer.getBoundsInfo().getLogLeft(), parentViewer.getBoundsInfo().getLogRight());
        regionsToHighlight = codonFilter.findRegions();
        for(Region r : regionsToHighlight){

            BoundsInfo bounds = parentViewer.getBoundsInfo();
            int start = r.getStart();
            if(start < bounds.getLogLeft()){
                start = bounds.getLogLeft();
            }

            int stop = r.getStop();
            if(stop > bounds.getLogRight()){
                stop = bounds.getLogRight();
            }

            int from = (int) parentViewer.getPhysBoundariesForLogPos(start).getLeftPhysBound();
            PhysicalBaseBounds stopBounds = parentViewer.getPhysBoundariesForLogPos(stop);
            int to = (int) stopBounds.getRightPhysBound();

            if(gapManager != null && gapManager.hasGapAt(stop)){
                to = (int) (gapManager.getNumOfGapsAt(stop) * stopBounds.getPhysWidth());
            }

            int length = to -from +1;
            // make sure it is visible when using high zoom levels
            if(length < 3 ){
                length = 3;
            }
            JRegion jreg = new JRegion(length, 10);
            if(r.isForwardStrand()){
                jreg.setBounds(from , baseLineY-jreg.getSize().height+1 ,jreg.getSize().width, jreg.getSize().height);
            } else {
                jreg.setBounds(from , baseLineY ,jreg.getSize().width, jreg.getSize().height);
            }
            this.add(jreg);
        }
    }

    public void showATGCodon(boolean selected) {
        codonFilter.setAtgSelected(selected);
        this.findCodons();
        this.repaint();
    }

    public void showGTGCodon(boolean selected){
        codonFilter.setGtgSelected(selected);
        this.findCodons();
        this.repaint();
    }

    public void showTTGCodon(boolean selected){
        codonFilter.setTtgSelected(selected);
        this.findCodons();
        this.repaint();
    }


}