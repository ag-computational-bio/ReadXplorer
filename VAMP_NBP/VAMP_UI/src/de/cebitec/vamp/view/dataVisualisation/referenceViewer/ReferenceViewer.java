package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class ReferenceViewer extends AbstractViewer {

    private final static long serialVersionUID = 7964236;
    private static int height = 250;
    private static int FRAMEHEIGHT = 20;
    private PersistantReference refGen;
    private Map<Integer, Integer> featureStats;
    private Feature currentlySelectedFeature;
    private int labelMargin;
    private ReferenceConnector refGenC;

//    public final static String PROP_INTERVALL_CHANGED = "intervall changed";
    public final static String PROP_FEATURE_STATISTICS_CHANGED = "feats changed";
    public final static String PROP_FEATURE_SELECTED = "feat selected";
    private int zoom;
    private int trackCount = 0;

    public ReferenceViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGen){
        super(boundsInfoManager, basePanel, refGen);
        refGenC = ProjectConnector.getInstance().getRefGenomeConnector(refGen.getId());
        featureStats = new HashMap<Integer, Integer>();
        this.refGen = refGen;
        this.showSequenceBar(true, true);
        this.labelMargin = 3;
        this.setViewerSize();
    }
           

    public void setSelectedFeature(Feature feature){
//        this.showFeatureDetails(feature.getPersistantFeature());
        firePropertyChange(PROP_FEATURE_SELECTED, currentlySelectedFeature, feature);

        // if the currently selected feature is clicked again, de-select it
        if (currentlySelectedFeature == feature){
            currentlySelectedFeature.setSelected(false);
            currentlySelectedFeature = null;
        } else {

            // if there was a feature selected before, de-select it
            if(currentlySelectedFeature != null){
                currentlySelectedFeature.setSelected(false);
            }

            currentlySelectedFeature = feature;
            currentlySelectedFeature.setSelected(true);
        }

        //only recalculate if reading frame was switched
        if (currentlySelectedFeature == null || this.getSequenceBar().getFrameCurrFeature() != this.determineFrame(currentlySelectedFeature.getPersistantFeature())){
            this.getSequenceBar().findCodons(); //update codons for current selection
        }
        
        //this.repaint();
    }

    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void boundsChangedHook() {
        // TODO compute this outside of EDT if too timeconsuming
        createFeatures();

//        firePropertyChange(PROP_INTERVALL_CHANGED, null, getBoundsInfo());
    }

    private void createFeatures(){
        this.removeAll();
        if(this.hasLegend()){
            this.add(this.getLegendLabel());
            this.add(this.getLegendPanel());
        }
        if(this.hasSequenceBar()){
            this.add(this.getSequenceBar());
        }

        featureStats.clear();

        List<PersistantFeature> features = refGenC.getFeaturesForRegion(getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight());
        for(PersistantFeature f : features){
            addFeatureComponent(f);
            registerFeatureInStats(f);
        }

        firePropertyChange(PROP_FEATURE_STATISTICS_CHANGED, null, featureStats);
    }

    private void registerFeatureInStats(PersistantFeature f){
        int type = f.getType();
        if(!featureStats.containsKey(type)){
            featureStats.put(type, 0);
        }
        featureStats.put(type, featureStats.get(type)+1);
    }

    private void addFeatureComponent(PersistantFeature f){
        int frame = determineFrame(f);
        int yCoord = determineYFromFrame(frame);
        PaintingAreaInfo bounds = getPaintingAreaInfo();

        // get left boundary of the feature
        double phyStart = getPhysBoundariesForLogPos(f.getStart()).getLeftPhysBound();
        if(phyStart < bounds.getPhyLeft()){
            phyStart = bounds.getPhyLeft();
        }

        // get right boundary of the feature
        double phyStop = getPhysBoundariesForLogPos(f.getStop()).getRightPhysBound();
        if(phyStop > bounds.getPhyRight()){
            phyStop = bounds.getPhyRight();
        }

        // set a minimum length to be displayed, otherwise a high zoomlevel could
        // lead to dissapearing features
        double length = phyStop - phyStart;
        if(length < 3){
            length = 3;
        }

        Feature jf = new Feature(f, length, this);
        int yFrom = yCoord - (Feature.height/2);
        jf.setBounds((int) phyStart,yFrom, jf.getSize().width, jf.getSize().height);

        if(currentlySelectedFeature != null){
            if(f.getId() == currentlySelectedFeature.getPersistantFeature().getId()){
                setSelectedFeature(jf);
            }
        }

        this.add(jf);
    }

    private int determineYFromFrame(int frame){
        int result = 0;
        int offset = Math.abs(frame) * FRAMEHEIGHT;

        if(frame < 0){
            result = this.getPaintingAreaInfo().getReverseLow();
            result += offset;
        } else {
            result = this.getPaintingAreaInfo().getForwardLow();
            result -= offset;
        }
        return result;
    }

    /**
     * @param f feature whose frame has to be determined
     * @return 1, 2, 3, -1, -2, -3 depending on the reading frame of the feature
     */
    public int determineFrame(PersistantFeature f){
        int frame = 0;
        int direction = f.getStrand();

        if(direction == 1){
            // forward strand
            frame = f.getStart() % 3 +1;
        } else if(direction == -1){
            // reverse strand
            // "start" at end of genome and use stop of feature, because  start <= stop ALWAYS!
            frame = ((refGen.getSequence().length() - f.getStop() +1) % 3 +1 )* -1;
        }
        return frame;
    }

    @Override
    protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        // draw lines for frames
        g.setColor(ColorProperties.TRACKPANEL_SCALE_LINES);
        drawScales(g);
    }

    private void drawScales(Graphics2D g){
        drawSingleScaleLine(g, determineYFromFrame(1), "+1");
        drawSingleScaleLine(g, determineYFromFrame(2), "+2");
        drawSingleScaleLine(g, determineYFromFrame(3), "+3");
        drawSingleScaleLine(g, determineYFromFrame(-1), "-1");
        drawSingleScaleLine(g, determineYFromFrame(-2), "-2");
        drawSingleScaleLine(g, determineYFromFrame(-3), "-3");
    }

    private void drawSingleScaleLine(Graphics2D g, int yCord, String label){
        int labelHeight = g.getFontMetrics().getMaxAscent();
        int labelWidth = g.getFontMetrics().stringWidth(label);

        int maxLeft = getPaintingAreaInfo().getPhyLeft();
        int maxRight = getPaintingAreaInfo().getPhyRight();

        // draw left label
        g.drawString(label, maxLeft-labelMargin-labelWidth, yCord+ labelHeight/2);
        // draw right label
        g.drawString(label, maxRight+labelMargin, yCord+ labelHeight/2);

        // assign space for label and some extra space
        int x1 = maxLeft;
        int x2 = maxRight;

        int linewidth = 15;
        int i = x1;
        while(i<=x2-linewidth){
            g.drawLine(i, yCord, i+linewidth, yCord);
            i += 2*linewidth;
        }
        if(i<=x2){
            g.drawLine(i, yCord, x2, yCord);
        }
    }

    @Override
    public void changeToolTipText(int logPos) {
        if(this.isMouseOverPaintingRequested()){
            this.setToolTipText(String.valueOf(logPos));
        } else {
            this.setToolTipText("");
        }
    }

    public Map<Integer, Integer> getFeatureStats() {
        return this.featureStats;
    }

    public Feature getCurrentlySelectedFeature() {
        return this.currentlySelectedFeature;
    }
    
    /**
     * Sets the initial size of the reference viewer.
     */
    private void setViewerSize() {
        
        this.setPreferredSize(new Dimension(1, 300));
        this.revalidate();
    }

    /**
     * Increases count of corresponding tracks.
     * If more information is needed implement listener model
     * with possibility to get track viewers.
     */
    public void increaseTrackCount() {
        ++this.trackCount;
    }
    
    /**
     * Decreases count of corresponding tracks.
     * If more information is needed implement listener model
     * with possibility to get track viewers.
     */
    public void decreaseTrackCount(){
        if (this.trackCount > 0){
            --this.trackCount;
        } //nothing to do if it is already 0
    }
    
    /**
     * @return Number of corresponding tracks.
     */
    public int getTrackCount(){
        return this.trackCount;
    }

}
