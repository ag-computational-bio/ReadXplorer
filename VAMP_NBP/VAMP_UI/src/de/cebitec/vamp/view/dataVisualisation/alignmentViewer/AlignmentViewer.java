package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.ZoomLevelExcusePanel;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;

/**
 *
 * @author ddoppmeier
 */
public class AlignmentViewer extends AbstractViewer {

    private static final long serialVersionUID = 234765253;
    private static int height = 500;
    private TrackConnector trackConnector;
    private LayoutI layout;
    private PersistantReference refGen;
    private int blockHeight;
    private int layerHeight;
    private ZoomLevelExcusePanel zoomExcuse;
//    private int minCountInInterval;
    private int maxCountInInterval;
    private int fwdMappingsInInterval;
    private int revMappingsInInterval;
    private int maxCoverageInInterval;
    private float minSaturationAndBrightness;
    private float maxSaturationAndBrightness;
    private float percentSandBPerCovUnit;

    public AlignmentViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGen, TrackConnector trackConnector) {
        super(boundsInfoManager, basePanel, refGen);
        this.refGen = refGen;
        this.trackConnector = trackConnector;
        this.setInDrawingMode(false);
        this.showSequenceBar(true, true);
        blockHeight = 8;
        layerHeight = blockHeight + 2;
        zoomExcuse = new ZoomLevelExcusePanel();
        minSaturationAndBrightness = 0.3f;
        maxSaturationAndBrightness = 0.9f;
        this.setHorizontalMargin(10);
        this.setActive(false);
    }

    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void changeToolTipText(int logPos) {
    }

    @Override
    public void boundsChangedHook() {

        if (isInMaxZoomLevel() && isActive()) {
            setInDrawingMode(true);
        } else {
            setInDrawingMode(false);
        }

        this.setupComponents();
    }

    private void setupComponents() {
        this.removeAll();

        if (isInMaxZoomLevel()) {
            // at least sufficient horizontal zoom level to show bases

            if (isInDrawingMode()) {
                if (this.hasLegend()) {
                    this.add(this.getLegendLabel());
                    this.add(this.getLegendPanel());
                }
                // if a sequence viewer was set for this panel, add/show it
                if (this.hasSequenceBar()) {
                    this.add(this.getSequenceBar());
                }

                // setup the layout of mappings
                this.createAndShowNewLayout(getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight());
                this.getSequenceBar().setGenomeGapManager(layout.getGenomeGapManager());
            }

        } else {
            this.placeExcusePanel(zoomExcuse);
        }
    }

    private void placeExcusePanel(JPanel p) {
        // has to be checked for null because, this method may be called upon
        // initialization of this object (depending on behaviour of AbstractViewer)
        // BEFORE the panels are initialized!
        if (p != null) {
            int tmpWidth = p.getPreferredSize().width;
            int x = this.getSize().width / 2 - tmpWidth / 2;
            if (x < 0) {
                x = 0;
            }

            int tmpHeight = p.getPreferredSize().height;
            int y = this.getSize().height / 2 - tmpHeight / 2;
            if (y < 0) {
                y = 0;
            }
            p.setBounds(x, y, tmpWidth, tmpHeight);
            this.add(p);
            this.updateUI();
        }
    }

    private void createAndShowNewLayout(int from, int to) {
        Collection<PersistantMapping> mappings = trackConnector.getMappings(from, to);
        HashMap<Integer, Integer> coverage = trackConnector.getCoverageInfosOfTrack(from, to);
        this.findMinAndMaxCount(mappings); //for currently shown mappings
        this.findMaxCoverage(coverage);
        this.setViewerHeight();
        layout = new Layout(from, to, mappings);
        this.addBlocks(layout);

    }

    /**
     * Determines the (min and) max count of mappings on a given set of mappings.
     * Minimum count is currently disabled as it was not needed.
     * @param mappings 
     */
    private void findMinAndMaxCount(Collection<PersistantMapping> mappings) {
//        this.minCountInInterval = Integer.MAX_VALUE; //uncomment all these lines to get min count
        this.maxCountInInterval = Integer.MIN_VALUE;
        this.fwdMappingsInInterval = 0;

        for (PersistantMapping m : mappings) {
            int coverage = m.getCoverage();
//            if(coverage < minCountInInterval){
//                minCountInInterval = coverage;
//            }
            if (coverage > maxCountInInterval) {
                maxCountInInterval = coverage;
            }
            if (m.isForwardStrand()) {
                ++this.fwdMappingsInInterval;
            }
        }
        this.revMappingsInInterval = mappings.size() - this.fwdMappingsInInterval;

        percentSandBPerCovUnit = (maxSaturationAndBrightness - minSaturationAndBrightness) / maxCountInInterval;
    }

    /**
     * Determines maximum coverage in the currently displayed interval.
     * @param coverage  coverage hashmap of positions for current interval
     */
    private void findMaxCoverage(HashMap<Integer, Integer> coverage) {
        this.maxCoverageInInterval = Integer.MIN_VALUE;

        int coverageAtPos;
        for (Integer position : coverage.keySet()) {

            coverageAtPos = coverage.get(position);
            if (coverageAtPos > this.maxCoverageInInterval) {
                this.maxCoverageInInterval = coverageAtPos;
            }
        }
    }

    /**
     * After creating a layout this method creates all visual components which
     * represent the layout. Thus it creates all block components. 
     * Each block component depicts one mapping.
     * @param layout the layout containing all information about the mappings to paint
     */
    private void addBlocks(LayoutI layout) {
        int layerCounter;
        int countingStep;

        // forward strand
        layerCounter = 1;
        countingStep = 1;
        Iterator<LayerI> it = layout.getForwardIterator();
        while (it.hasNext()) {
            LayerI b = it.next();
            Iterator<BlockI> blockIt = b.getBlockIterator();
            while (blockIt.hasNext()) {
                BlockI block = blockIt.next();
                this.createJBlock(block, layerCounter);
            }

            layerCounter += countingStep;
        }


        // reverse strand
        layerCounter = -1;
        countingStep = -1;
        Iterator<LayerI> itRev = layout.getReverseIterator();
        while (itRev.hasNext()) {
            LayerI b = itRev.next();
            Iterator<BlockI> blockIt = b.getBlockIterator();
            while (blockIt.hasNext()) {
                BlockI block = blockIt.next();
                this.createJBlock(block, layerCounter);
            }

            layerCounter += countingStep;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        if (isInDrawingMode()) {
            g.setColor(ColorProperties.TRACKPANEL_MIDDLE_LINE);
            drawBaseLines(g);
        }
    }

    /**
     * Creates a new block component vertically in the current layer and horizontally
     * covering it's aligned genome positions.
     * @param block the block to create a jblock (block component) for
     * @param layerCounter determines in which layer the block should be painted
     */
    private void createJBlock(BlockI block, int layerCounter) {
        BlockComponent jb = new BlockComponent(block, this, layout.getGenomeGapManager(), blockHeight, minSaturationAndBrightness, percentSandBPerCovUnit);

        // negative layer counter means reverse strand
        int lower = (layerCounter < 0 ? getPaintingAreaInfo().getReverseLow() : getPaintingAreaInfo().getForwardLow());
        int yPosition = lower - layerCounter * layerHeight;
        if (layerCounter < 0) {
            // reverse/negative layer
            yPosition -= jb.getHeight() / 2;
        } else {
            // forward/positive layer
            yPosition -= jb.getHeight() / 2;
        }

        jb.setBounds(jb.getPhyStart(), yPosition, jb.getPhyWidth(), jb.getHeight());
        this.add(jb);
    }

    private void drawBaseLines(Graphics2D graphics) {
        PaintingAreaInfo info = getPaintingAreaInfo();
        graphics.drawLine(info.getPhyLeft(), info.getForwardLow(), info.getPhyRight(), info.getForwardLow());
        graphics.drawLine(info.getPhyLeft(), info.getReverseLow(), info.getPhyRight(), info.getReverseLow());
    }

    @Override
    public int transformToLogicalCoord(int physPos) {
        int logPos = super.transformToLogicalCoord(physPos);
        if (isInDrawingMode()) {
            int gapsSmaller = layout.getGenomeGapManager().getAccumulatedGapsSmallerThan(logPos);
            logPos -= gapsSmaller;
        }
        return logPos;

    }

    @Override
    public double transformToPhysicalCoord(int logPos) {

        // if this viewer is operating in detail view mode, adjust logPos
        if (layout != null && isInDrawingMode()) {
            int gapsSmaller = layout.getGenomeGapManager().getNumOfGapsSmaller(logPos);
            logPos += gapsSmaller;
        }
        return super.transformToPhysicalCoord(logPos);
    }

    @Override
    public int getWidthOfMouseOverlay(int position) {
        PhysicalBaseBounds mouseAreaLeft = getPhysBoundariesForLogPos(position);

        int width = (int) mouseAreaLeft.getPhysWidth();
        // if currentPosition is a gap, the following bases to the right marks the same position!
        // situation may occur, that on startup no layout is computed but this methode is called, although
        if (layout != null && layout.getGenomeGapManager().hasGapAt(position)) {
            width *= (layout.getGenomeGapManager().getNumOfGapsAt(position) + 1);
        }
        return width;
    }

    public PersistantReference getRefGen() {
        return refGen;
    }

    /**
     * Adapts the height of the alignment viewer according to the content currently displayed.
     */
    private void setViewerHeight() {

        int biggestCoverage = this.maxCoverageInInterval / 2;
        int biggerStrandCoverage = fwdMappingsInInterval > revMappingsInInterval ? fwdMappingsInInterval : revMappingsInInterval;
        if (biggerStrandCoverage > biggestCoverage) {
            biggestCoverage = biggerStrandCoverage * 2; //to cover both halves
        }
        int newHeight = (int) (this.layerHeight * biggestCoverage * 1.5); //1.5 = factor for possible empty spacings between alignments
        final int spacer = 120;
        this.setPreferredSize(new Dimension(this.getWidth(), newHeight + spacer));
        this.revalidate();
    }
}
