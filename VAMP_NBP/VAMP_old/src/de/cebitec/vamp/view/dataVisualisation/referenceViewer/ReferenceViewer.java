package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.cebitec.vamp.ColorProperties;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;

/**
 *
 * @author ddoppmeier
 */
public class ReferenceViewer extends AbstractViewer {

    private final static long serialVersionUID = 7964236;
    private static int height = 180;
    private static int FRAMEHEIGHT = 20;
    private PersistantReference refGen;
    private ReferenceViewerInfoPanel infoPanel;
    private Map<Integer, Integer> featureStats;
    private Feature currentlySelectedFeature;
    private int labelMargin;
    private ReferenceConnector refGenC;
    

    public ReferenceViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGen){
        super(boundsInfoManager, basePanel, refGen);
        refGenC = ProjectConnector.getInstance().getRefGenomeConnector(refGen.getId());
        featureStats = new HashMap<Integer, Integer>();
        this.refGen = refGen;
        this.showSequenceBar(true);
        this.labelMargin = 3;
    }
    
    public void setSelectedFeature(Feature feature){
        this.showFeatureDetails(feature.getPersistantFeature());
        // if there was a feature selected before, de-select it
        if(currentlySelectedFeature != null ){
            currentlySelectedFeature.setSelected(false);
        }

        currentlySelectedFeature = feature;
        currentlySelectedFeature.setSelected(true);
    }


    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void boundsChangedHook() {
        createFeatures();
        if(infoPanel != null){
            infoPanel.setIntervall(getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight());
            infoPanel.showFeatureStatisticsForIntervall(featureStats);
        }
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

    private int determineFrame(PersistantFeature f){
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

    public void setGenomeViewerInfoPanel(ReferenceViewerInfoPanel info) {
        infoPanel = info;
    }

    public void showFeatureDetails(PersistantFeature f) {
        infoPanel.showFeatureDetails(f);
    }

    @Override
    public void changeToolTipText(int logPos) {
        if(this.isMouseOverPaintingRequested()){
            this.setToolTipText(String.valueOf(logPos));
        } else {
            this.setToolTipText("");
        }
    }

}
