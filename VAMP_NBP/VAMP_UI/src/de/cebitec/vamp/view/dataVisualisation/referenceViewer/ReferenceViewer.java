package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeatureI;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantSubfeature;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.PaintingAreaInfo;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.EnumMap;
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
    private Map<FeatureType, Integer> featureStats;
    private JFeature currentlySelectedFeature;
    private int labelMargin;
    private ReferenceConnector refGenC;
    private ArrayList<JFeature> features;
    private ArrayList<JFeature> subfeatures;

    public final static String PROP_FEATURE_STATISTICS_CHANGED = "feats changed";
    public final static String PROP_FEATURE_SELECTED = "feat selected";
    public static final String PROP_EXCLUDED_FEATURE_EVT = "excl feat evt";
    private int trackCount = 0;
    
    public ReferenceViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGen){
        super(boundsInfoManager, basePanel, refGen);
        this.features = new ArrayList<JFeature>();
        this.subfeatures = new ArrayList<JFeature>();
        this.refGenC = ProjectConnector.getInstance().getRefGenomeConnector(refGen.getId());
        this.featureStats = new EnumMap<FeatureType, Integer>(FeatureType.class);
        this.getExcludedFeatureTypes().add(FeatureType.UNDEFINED);
        this.refGen = refGen;
        this.showSequenceBar(true, true);
        this.labelMargin = 3;
        this.setViewerSize();
    }
           

    public void setSelectedFeature(JFeature feature){
        
        firePropertyChange(PROP_FEATURE_SELECTED, currentlySelectedFeature, feature);

        // if the currently selected feature is clicked again, de-select it
        if (currentlySelectedFeature == feature){
            currentlySelectedFeature.setSelected(false);
            currentlySelectedFeature = null;
        } else {

            // if there was a feature selected before, de-select it
            if (currentlySelectedFeature != null){
                currentlySelectedFeature.setSelected(false);
            }

            currentlySelectedFeature = feature;
            currentlySelectedFeature.setSelected(true);
        }

        //only recalculate if reading frame was switched
        if (currentlySelectedFeature == null || this.getSequenceBar().getFrameCurrFeature() != this.determineFrame(currentlySelectedFeature.getPersistantFeature())){
            this.getSequenceBar().findCodons(); //update codons for current selection
        }
    }
    
    
    @Override
    public void close(){
        super.close();
        refGenC = null;
        refGen = null;
        featureStats.clear();
        this.features.clear();
        this.subfeatures.clear();
        this.getExcludedFeatureTypes().clear();
    }

    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void boundsChangedHook() {
        // TODO compute this outside of EDT if too timeconsuming
        this.createFeatures();

//        firePropertyChange(PROP_INTERVAL_CHANGED, null, getBoundsInfo());
    }

    private void createFeatures(){
        this.removeAll();
        this.features.clear();
        this.subfeatures.clear();
        this.featureStats.clear();
        
        if (this.hasLegend()){
            this.add(this.getLegendLabel());
            this.add(this.getLegendPanel());
        }
        if (this.hasSequenceBar()){
            this.add(this.getSequenceBar());
        }

        List<PersistantFeature> featureList = refGenC.getFeaturesForRegion(
                getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight());
        Map<Integer, PersistantFeature> featureMap = PersistantFeature.getFeatureMap(featureList);
        List<PersistantSubfeature> subfeatureList = refGenC.getSubfeaturesForRegion(
                getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight());
        
        //at first add subfeatures to their parent features
        PersistantFeature.addSubfeatures(featureMap, subfeatureList);
        featureMap.clear();
        
        for (PersistantFeature feature : featureList){
            this.addFeatureComponent(feature);
            this.registerFeatureInStats(feature);
        }
        
        for (JFeature jsubfeature : this.subfeatures) {
            this.add(jsubfeature);
        }
        for (JFeature jfeature : this.features) {
            this.add(jfeature);
        }

        firePropertyChange(PROP_FEATURE_STATISTICS_CHANGED, null, featureStats);
    }

    
    private void registerFeatureInStats(PersistantFeatureI feature){
        FeatureType type = feature.getType();
        if(!this.featureStats.containsKey(type)){
            this.featureStats.put(type, 0);
        }
        this.featureStats.put(type, this.featureStats.get(type)+1);
    }

    /**
     * Creates a feature component for a given feature and adds it to the reference viewer.
     * @param feature the feature to add to the viewer.
     */
    private void addFeatureComponent(PersistantFeature feature){
        int frame = this.determineFrame(feature);
        int yCoord = this.determineYFromFrame(frame);
        PaintingAreaInfo bounds = getPaintingAreaInfo();
        
        //handle subfeatures of the feature (e.g. exons)
        boolean subfeatAdded = false;
        for (PersistantSubfeature subfeature : feature.getSubfeatures()) {
            this.registerFeatureInStats(subfeature);

            if (!this.getExcludedFeatureTypes().contains(subfeature.getType())) {
                byte border = JFeature.BORDER_NONE;
                // get left boundary of the feature
                double phyStart = this.getPhysBoundariesForLogPos(subfeature.getStart()).getLeftPhysBound();
                if (phyStart < bounds.getPhyLeft()) {
                    phyStart = bounds.getPhyLeft();
                    border = JFeature.BORDER_LEFT;
                }

                // get right boundary of the feature
                double phyStop = this.getPhysBoundariesForLogPos(subfeature.getStop()).getRightPhysBound();
                if (phyStop > bounds.getPhyRight()) {
                    phyStop = bounds.getPhyRight();
                    border = border == JFeature.BORDER_LEFT ? JFeature.BORDER_BOTH : JFeature.BORDER_RIGHT;
                }

                // set a minimum length to be displayed, otherwise a high zoomlevel could
                // lead to dissapearing features
                double length = phyStop - phyStart;
                if (length < 3) {
                    length = 3;
                }

                PersistantFeature subfeatureFeature = new PersistantFeature(feature.getId(), feature.getEcNumber(),
                        feature.getLocus(), feature.getProduct(), subfeature.getStart(), subfeature.getStop(),
                        feature.getStrand(), subfeature.getType(), feature.getGeneName());
                JFeature jSubfeature = new JFeature(subfeatureFeature, length, this, border);
                int yFrom = yCoord - (jSubfeature.getHeight() / 2);
                jSubfeature.setBounds((int) phyStart, yFrom, jSubfeature.getSize().width, jSubfeature.getHeight());

                this.subfeatures.add(jSubfeature);
                subfeatAdded = true;
            }
        }

        if (!this.getExcludedFeatureTypes().contains(feature.getType()) || subfeatAdded) {
            byte border = JFeature.BORDER_NONE;
            // get left boundary of the feature
            double phyStart = this.getPhysBoundariesForLogPos(feature.getStart()).getLeftPhysBound();
            if (phyStart < bounds.getPhyLeft()) {
                phyStart = bounds.getPhyLeft();
                border = JFeature.BORDER_LEFT;
            }

            // get right boundary of the feature
            double phyStop = this.getPhysBoundariesForLogPos(feature.getStop()).getRightPhysBound();
            if (phyStop > bounds.getPhyRight()) {
                phyStop = bounds.getPhyRight();
                border = border == JFeature.BORDER_LEFT ? JFeature.BORDER_BOTH : JFeature.BORDER_RIGHT;
            }

            // set a minimum length to be displayed, otherwise a high zoomlevel could
            // lead to dissapearing features
            double length = phyStop - phyStart;
            if (length < 3) {
                length = 3;
            }

            JFeature jFeature = new JFeature(feature, length, this, border);
            int yFrom = yCoord - (jFeature.getHeight() / 2);
            jFeature.setBounds((int) phyStart, yFrom, jFeature.getSize().width, jFeature.getHeight());

            if (currentlySelectedFeature != null) {
                if (feature.getId() == currentlySelectedFeature.getPersistantFeature().getId()) {
                    setSelectedFeature(jFeature);
                }
            }

            this.features.add(jFeature);
        }
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
        this.drawScales(g);
    }

    /**
     * Draws the lines as orientation for each frame.
     * @param g the graphics object to paint in.
     */
    private void drawScales(Graphics2D g){
        this.drawSingleScaleLine(g, this.determineYFromFrame(1), "+1");
        this.drawSingleScaleLine(g, this.determineYFromFrame(2), "+2");
        this.drawSingleScaleLine(g, this.determineYFromFrame(3), "+3");
        this.drawSingleScaleLine(g, this.determineYFromFrame(-1), "-1");
        this.drawSingleScaleLine(g, this.determineYFromFrame(-2), "-2");
        this.drawSingleScaleLine(g, this.determineYFromFrame(-3), "-3");
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

    public Map<FeatureType, Integer> getFeatureStats() {
        return this.featureStats;
    }

    public JFeature getCurrentlySelectedFeature() {
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
