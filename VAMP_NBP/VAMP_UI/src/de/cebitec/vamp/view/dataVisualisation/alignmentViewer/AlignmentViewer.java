package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import de.cebitec.vamp.databackend.IntervalRequest;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.MappingResultPersistant;
import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;

/**
 * Viewer to show alignments of reads to the reference.
 * 
 * @author ddoppmeier, rhilker
 */
public class AlignmentViewer extends AbstractViewer implements ThreadListener {

    private static final long serialVersionUID = 234765253;
    private static int height = 500;
    private TrackConnector trackConnector;
    private LayoutI layout;
    private int blockHeight;
    private int layerHeight;
    private int maxCountInInterval;
    private int fwdMappingsInInterval;
    private int revMappingsInInterval;
    private int maxCoverageInInterval;
    private float minSaturationAndBrightness;
    private float maxSaturationAndBrightness;
    private float percentSandBPerCovUnit;
    private int oldLogLeft;
    private int oldLogRight;
    boolean needRepaint = false;
    MappingResultPersistant mappingResult;
    HashMap<Integer, Integer> completeCoverage;

    /**
     * Viewer to show alignments of reads to the reference.
     * @param boundsInfoManager the bounds info manager for the viewer
     * @param basePanel the base panel on which the viewer is located
     * @param refGenome the reference genome
     * @param trackConnector connector of the track to show in this viewer
     */
    public AlignmentViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGenome, TrackConnector trackConnector) {
        super(boundsInfoManager, basePanel, refGenome);
        this.trackConnector = trackConnector;
        this.setInDrawingMode(true);
        this.showSequenceBar(true, true);
        blockHeight = 8;
        layerHeight = blockHeight + 2;
        minSaturationAndBrightness = 0.3f;
        maxSaturationAndBrightness = 0.9f;
        mappingResult = new MappingResultPersistant(new ArrayList<PersistantMapping>(), 0, 0);
        completeCoverage = new HashMap<>();
        this.setHorizontalMargin(10);
        this.setActive(false);
        this.setAutomaticCentering(true);
        setupComponents();
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
       
        if (this.isInMaxZoomLevel() && isActive()) {
           //  updatePhysicalBounds();
            setInDrawingMode(true);
        } else {
            setInDrawingMode(false);
        }    

         this.setupComponents();
    }

    private void setupComponents() {


        if (!this.isInMaxZoomLevel()) {
            this.getBoundsInformationManager().zoomLevelUpdated(1);
        }
        // at least sufficient horizontal zoom level to show bases

        if (isInDrawingMode()) { //request the data to show, receiveData method calls draw methods
            this.requestData(super.getBoundsInfo().getLogLeft(), super.getBoundsInfo().getLogRight());
        }
    }

    /*
     * places the excuse panel if zoom level is too high
     */
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
    
    /**
     * Requests new mapping data for the current bounds or shows the mapping 
     * data, if it is already available.
     */
    private void requestData(int from, int to) {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int logLeft = this.getBoundsInfo().getLogLeft();
        int logRight = this.getBoundsInfo().getLogRight();
        if (logLeft != this.oldLogLeft || logRight != this.oldLogRight) {
            
            trackConnector.addMappingRequest(new IntervalRequest(from, to, this, Properties.MAPPINGS_W_DIFFS));
            this.oldLogLeft = logLeft;
            this.oldLogRight = logRight;
        } else {
            showData();
        }
    }
    
    /**
     * Method called, when data is available. If the avialable data is a 
     * MappingResultPersistant, then the viewer is updated with the new
     * mapping data.
     * @param data the new mapping data to show
     */
    @Override
    @SuppressWarnings("unchecked")
    public void receiveData(Object data) {
        if (data.getClass().equals(mappingResult.getClass())) {
            this.mappingResult = ((MappingResultPersistant) data);
            this.needRepaint = true;
            this.showData();
        }
    }
    
    /**
     * Actually takes care of the drawing of all components of the viewer.
     */
    private void showData() {
        
        if (this.needRepaint) {

            this.findMinAndMaxCount(mappingResult.getMappings()); //for currently shown mappingResult
            this.findMaxCoverage(completeCoverage);
            this.setViewerHeight();
            this.layout = new Layout(mappingResult.getLowerBound(), mappingResult.getUpperBound(), mappingResult.getMappings(), getExcludedFeatureTypes());

            this.removeAll();
            this.addBlocks(layout);

            if (this.hasLegend()) {
                this.add(this.getLegendLabel());
                this.add(this.getLegendPanel());
            }
            if (this.hasOptions()) {
                this.add(this.getOptionsLabel());
                this.add(this.getOptionsPanel());
            }
            // if a sequence viewer was set for this panel, add/show it
            if (this.hasSequenceBar()) {
                this.add(this.getSequenceBar());
            }

            getSequenceBar().setGenomeGapManager(layout.getGenomeGapManager());
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            
            this.needRepaint = false;
            this.repaint();
        }
    }

    /**
     * Determines the (min and) max count of mappingResult on a given set of mappingResult.
     * Minimum count is currently disabled as it was not needed.
     * @param mappingResult 
     */
    private void findMinAndMaxCount(Collection<PersistantMapping> mappings) {
//        this.minCountInInterval = Integer.MAX_VALUE; //uncomment all these lines to get min count
        this.maxCountInInterval = Integer.MIN_VALUE;
        this.fwdMappingsInInterval = 0;

        for (PersistantMapping m : mappings) {
            int coverage = m.getNbReplicates();
//            if(coverage < minCountInInterval) {
//                minCountInInterval = coverage;
//            }
            if (coverage > maxCountInInterval) {
                maxCountInInterval = coverage;
            }
            if (m.isFwdStrand()) {
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
     * represent the layout. Thus, it creates all block components. 
     * Each block component depicts one mapping.
     * @param layout the layout containing all information about the mappingResult to paint
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
