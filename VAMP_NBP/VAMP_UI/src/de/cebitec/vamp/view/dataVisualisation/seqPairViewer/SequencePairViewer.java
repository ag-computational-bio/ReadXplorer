package de.cebitec.vamp.view.dataVisualisation.seqPairViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantSeqPairGroup;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.BlockI;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.LayerI;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.LayoutI;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public class SequencePairViewer extends AbstractViewer {

    private static final long serialVersionUID = 234765253;
    private static int height = 500;
    private TrackConnector trackConnector;
    private int trackID2;
    private LayoutI layout;
    private PersistantReference refGen;
    private int blockHeight;
    private int layerHeight;
//    private int minCountInInterval;
    private int viewerHeight;
//    private int maxCountInInterval;
//    private int fwdMappingsInInterval;
//    private int revMappingsInInterval;
//    private int maxCoverageInInterval;
    private float minSaturationAndBrightness;
//    private float maxSaturationAndBrightness;
//    private float percentSandBPerCovUnit;

    /**
     * Creates a new viewer for displaying sequence pair information between two
     * tracks. Each of them must hold one sequence of the pair.
     * @param boundsInfoManager the bounds info manager
     * @param basePanel base panel on which to display this viewer
     * @param refGen the reference genome
     * @param trackConnector track connector of one of the two sequence pair tracks
     */
    public SequencePairViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGen, TrackConnector trackConnector) {
        super(boundsInfoManager, basePanel, refGen);
//        this.createFocusListener();
        this.refGen = refGen;
        this.trackConnector = trackConnector;
        int id = this.trackConnector.getSeqPairToTrackID();
        this.trackID2 = this.trackConnector.getTrackIdToSeqPairId(id);
        this.showSequenceBar(true, false);
        blockHeight = 5;
        layerHeight = blockHeight + 1;
        minSaturationAndBrightness = 0.9f;
//        maxSaturationAndBrightness = 0.9f;
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

        if (isActive()) {
            setInDrawingMode(true);
            this.setupComponents();
        } else {
            setInDrawingMode(false);
        }

    }

    private void setupComponents() {
        this.removeAll();

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
        }
    }


    /**
     * Creates the complete layout of this viewer for a given interval
     * @param from left (smaller) border of interval
     * @param to right (larger) border of interval
     */
    private void createAndShowNewLayout(int from, int to) {
        Collection<PersistantSeqPairGroup> seqPairs = trackConnector.getSeqPairMappings(from, to, trackID2);
        
//        HashMap<Integer, Integer> coverage = trackConnector.getCoverageInfosOfTrack(from, to);
//        HashMap<Integer, Integer> coverage2 = trackConnector2.getCoverageInfosOfTrack(from, to);
//        this.findMinAndMaxCount(seqPairs); //for currently shown mappings
//        this.findMaxCoverage(coverage); //TODO: update counts here? at the moment not needed
        layout = new LayoutPairs(from, to, seqPairs);
        this.addBlocks(layout);
        this.setViewerHeight();

    }

//    /**
//     * Determines the (min and) max count of mappings on a given set of mappings.
//     * Minimum count is currently disabled as it was not needed.
//     * @param seqPairs 
//     */
//    private void findMinAndMaxCount(Collection<PersistantSeqPairGroup> seqPairs) {
////        this.minCountInInterval = Integer.MAX_VALUE; //uncomment all these lines to get min count
//        this.maxCountInInterval = Integer.MIN_VALUE;
////        this.fwdMappingsInInterval = 0;
//        this.pairCountInInterval = 0;
//
////        for (PersistantSeqPairGroup pair : seqPairs) {
//            ++this.pairCountInInterval;
////            if (pair.getVisibleMapping().isForwardStrand()){
////                ++this.fwdMappingsInInterval;
////            }
//            
//        }
////        this.revMappingsInInterval = seqPairs.size() - this.fwdMappingsInInterval;
//
////        percentSandBPerCovUnit = (maxSaturationAndBrightness - minSaturationAndBrightness) / maxCountInInterval;
//    }

//    /**
//     * Determines maximum coverage in the currently displayed interval.
//     * @param coverage  coverage hashmap of positions for current interval
//     */
//    private void findMaxCoverage(HashMap<Integer, Integer> coverage) {
//        this.maxCoverageInInterval = Integer.MIN_VALUE;
//
//        int coverageAtPos;
//        for (Integer position : coverage.keySet()) {
//
//            coverageAtPos = coverage.get(position);
//            if (coverageAtPos > this.maxCoverageInInterval) {
//                this.maxCoverageInInterval = coverageAtPos;
//            }
//        }
//    }

    private void addBlocks(LayoutI layout) {
        int layerCounter;
        int countingStep;

//        // forward strand
//        layerCounter = 1;
//        countingStep = 1;
//        Iterator<LayerI> it = layout.getForwardIterator();
//        while (it.hasNext()) {
//            LayerI b = it.next();
//            Iterator<BlockI> blockIt = b.getBlockIterator();
//            while (blockIt.hasNext()) {
//                BlockI block = blockIt.next();
//                this.createJBlock(block, layerCounter);
//            }
//
//            layerCounter += countingStep;
//        }
//        this.viewerHeight = layerCounter * this.layerHeight;

        // reverse strand
        layerCounter = -1;
        countingStep = -1;
        Iterator<LayerI> itRev = layout.getReverseIterator();
        while (itRev.hasNext()) {
            LayerI b = itRev.next();
            Iterator<BlockI> blockIt = b.getBlockIterator();
            while (blockIt.hasNext()) {
                BlockPair block = (BlockPair) blockIt.next();
                this.createJBlock(block, layerCounter);
            }

            layerCounter += countingStep;
        }
        this.viewerHeight = Math.abs(layerCounter) * this.layerHeight + 20;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        
        if (isInDrawingMode()) {
            g.setColor(ColorProperties.TRACKPANEL_MIDDLE_LINE);
            drawBaseLines(g);
        }
        g.setColor(Color.black);
    }

    private void createJBlock(BlockPair block, int layerCounter) {
        BlockComponentPair blockComp = new BlockComponentPair(block, this, blockHeight, minSaturationAndBrightness);

        // negative layer counter means reverse strand
        int lower = (layerCounter < 0 ? getPaintingAreaInfo().getReverseLow() : getPaintingAreaInfo().getForwardLow());
        int yPosition = lower - layerCounter * layerHeight;
//        if (layerCounter < 0) {
            // reverse/negative layer
            yPosition -= blockComp.getHeight() / 2;
//        } else {
//            // forward/positive layer
//            yPosition -= jb.getHeight() / 2;
//        }

        blockComp.setBounds(blockComp.getPhyStart(), yPosition, blockComp.getPhyWidth(), blockComp.getHeight());
        this.add(blockComp);
    }

    private void drawBaseLines(Graphics2D graphics) {
        PaintingAreaInfo info = getPaintingAreaInfo();
        graphics.drawLine(info.getPhyLeft(), info.getForwardLow(), info.getPhyRight(), info.getForwardLow());
        graphics.drawLine(info.getPhyLeft(), info.getReverseLow(), info.getPhyRight(), info.getReverseLow());
    }
    

    @Override
    public int getWidthOfMouseOverlay(int position) {
        PhysicalBaseBounds mouseAreaLeft = getPhysBoundariesForLogPos(position);

        int width = (int) mouseAreaLeft.getPhysWidth();
        return width;
    }

    public PersistantReference getRefGen() {
        return refGen;
    }

    /**
     * Adapts the height of the alignment viewer according to the content currently displayed.
     */
    private void setViewerHeight() {

//        int biggestCoverage = this.maxCoverageInInterval / 2;
//        int biggerStrandCoverage = this.pairCountInInterval; //fwdMappingsInInterval > revMappingsInInterval ? fwdMappingsInInterval : revMappingsInInterval;
//        if (biggerStrandCoverage > biggestCoverage) {
//            biggestCoverage = biggerStrandCoverage * 2; //to cover both halves
//        }
        int newHeight = this.viewerHeight;//(int) (this.layerHeight * biggestCoverage * 1.5); //1.5 = factor for possible empty spacings between alignments
        final int spacer = 120;
        this.setPreferredSize(new Dimension(this.getWidth(), newHeight + spacer));
        this.revalidate();
    }
    
    /**
     * returns all information belonging to the given sequence pair id. This
     * comprises all pair replicates and all single mappings associated with this id.
     * @param seqPairId the id to receive all data for
     * @return The PersistantSeqPairGroup holding all information regarding this sequence pair id
     */
    protected PersistantSeqPairGroup getSeqPairInfoFromDB(long seqPairId){
        return this.trackConnector.getMappingsForSeqPairId(seqPairId);
    }
    
    
//    private void createFocusListener() {
//        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//        focusManager.addPropertyChangeListener(new PropertyChangeListener() {
//
//            @Override
//            public void propertyChange(PropertyChangeEvent e) {
//                if (("focusOwner".equals(e.getPropertyName()))) {
//                    Component comp = (Component) e.getNewValue();
//                    if (comp instanceof AbstractViewer && comp != SequencePairViewer.this){
//                        setActive(false);
//                    }
//                }
//            }
//        });
//    }
}
