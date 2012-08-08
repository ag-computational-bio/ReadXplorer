package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.api.objects.FeatureType;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotationI;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantSubAnnotation;
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
    private Map<FeatureType, Integer> annotationStats;
    private JAnnotation currentlySelectedAnnotation;
    private int labelMargin;
    private ReferenceConnector refGenConnector;
    private ArrayList<JAnnotation> annotations;
    private ArrayList<JAnnotation> subAnnotations;

    public final static String PROP_ANNOTATION_STATS_CHANGED = "feats changed";
    public final static String PROP_ANNOTATION_SELECTED = "feat selected";
    public static final String PROP_EXCLUDED_ANNOTATION_EVT = "excl feat evt";
    private int trackCount = 0;
    
    /**
     * Creates a new reference viewer. 
     * @param boundsInfoManager the global bounds info manager 
     * @param basePanel the base panel
     * @param refGenome the persistant reference, which is always accessible through the getReference 
     *      method in any abstract viewer.
     */
    public ReferenceViewer(BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistantReference refGenome){
        super(boundsInfoManager, basePanel, refGenome);
        this.annotations = new ArrayList<JAnnotation>();
        this.subAnnotations = new ArrayList<JAnnotation>();
        this.refGenConnector = ProjectConnector.getInstance().getRefGenomeConnector(refGenome.getId());
        this.annotationStats = new EnumMap<FeatureType, Integer>(FeatureType.class);
        this.getExcludedFeatureTypes().add(FeatureType.UNDEFINED);
        this.showSequenceBar(true, true);
        this.labelMargin = 3;
        this.setViewerSize();
    }
           

    public void setSelectedAnnotation(JAnnotation annotation){
        
        firePropertyChange(PROP_ANNOTATION_SELECTED, currentlySelectedAnnotation, annotation);

        // if the currently selected annotation is clicked again, de-select it
        if (currentlySelectedAnnotation == annotation){
            currentlySelectedAnnotation.setSelected(false);
            currentlySelectedAnnotation = null;
        } else {

            // if there was a annotation selected before, de-select it
            if (currentlySelectedAnnotation != null){
                currentlySelectedAnnotation.setSelected(false);
            }

            currentlySelectedAnnotation = annotation;
            currentlySelectedAnnotation.setSelected(true);
        }

        //only recalculate if reading frame was switched
        if (currentlySelectedAnnotation == null || this.getSequenceBar().getFrameCurrAnnotation() != this.determineFrame(currentlySelectedAnnotation.getPersistantAnnotation())){
            this.getSequenceBar().findCodons(); //update codons for current selection
        }
    }
    
    
    @Override
    public void close(){
        super.close();
        refGenConnector = null;
        annotationStats.clear();
        this.annotations.clear();
        this.subAnnotations.clear();
        this.getExcludedFeatureTypes().clear();
    }

    @Override
    public int getMaximalHeight() {
        return height;
    }

    @Override
    public void boundsChangedHook() {
        // TODO compute this outside of EDT if too timeconsuming
        this.createAnnotations();

//        firePropertyChange(PROP_INTERVAL_CHANGED, null, getBoundsInfo());
    }

    private void createAnnotations(){
        this.removeAll();
        this.annotations.clear();
        this.subAnnotations.clear();
        this.annotationStats.clear();
        
        if (this.hasLegend()){
            this.add(this.getLegendLabel());
            this.add(this.getLegendPanel());
        }
        if (this.hasSequenceBar()){
            this.add(this.getSequenceBar());
        }

        List<PersistantAnnotation> annotationList = refGenConnector.getAnnotationsForRegion(
                getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight());
        Map<Integer, PersistantAnnotation> annotationMap = PersistantAnnotation.getAnnotationMap(annotationList);
        List<PersistantSubAnnotation> subAnnotationList = refGenConnector.getSubAnnotationsForRegion(
                getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight());
        
        //at first add sub annotations to their parent annotations
        PersistantAnnotation.addSubAnnotations(annotationMap, subAnnotationList);
        annotationMap.clear();
        
        for (PersistantAnnotation annotation : annotationList){
            this.addAnnotationComponent(annotation);
            this.registerAnnotationInStats(annotation);
        }
        
        for (JAnnotation jSubAnnotation : this.subAnnotations) {
            this.add(jSubAnnotation);
        }
        for (JAnnotation jAnnotation : this.annotations) {
            this.add(jAnnotation);
        }

        firePropertyChange(PROP_ANNOTATION_STATS_CHANGED, null, annotationStats);
    }

    
    private void registerAnnotationInStats(PersistantAnnotationI annotation){
        FeatureType type = annotation.getType();
        if(!this.annotationStats.containsKey(type)){
            this.annotationStats.put(type, 0);
        }
        this.annotationStats.put(type, this.annotationStats.get(type)+1);
    }

    /**
     * Creates a annotation component for a given annotation and adds it to the reference viewer.
     * @param annotation the annotation to add to the viewer.
     */
    private void addAnnotationComponent(PersistantAnnotation annotation){
        int frame = this.determineFrame(annotation);
        int yCoord = this.determineYFromFrame(frame);
        PaintingAreaInfo bounds = getPaintingAreaInfo();
        
        //handle sub annotations of the annotation (e.g. exons)
        boolean subfeatAdded = false;
        for (PersistantSubAnnotation subAnnotation : annotation.getSubAnnotations()) {
            this.registerAnnotationInStats(subAnnotation);

            if (!this.getExcludedFeatureTypes().contains(subAnnotation.getType())) {
                byte border = JAnnotation.BORDER_NONE;
                // get left boundary of the annotation
                double phyStart = this.getPhysBoundariesForLogPos(subAnnotation.getStart()).getLeftPhysBound();
                if (phyStart < bounds.getPhyLeft()) {
                    phyStart = bounds.getPhyLeft();
                    border = JAnnotation.BORDER_LEFT;
                }

                // get right boundary of the annotation
                double phyStop = this.getPhysBoundariesForLogPos(subAnnotation.getStop()).getRightPhysBound();
                if (phyStop > bounds.getPhyRight()) {
                    phyStop = bounds.getPhyRight();
                    border = border == JAnnotation.BORDER_LEFT ? JAnnotation.BORDER_BOTH : JAnnotation.BORDER_RIGHT;
                }

                // set a minimum length to be displayed, otherwise a high zoomlevel could
                // lead to dissapearing annotations
                double length = phyStop - phyStart;
                if (length < 3) {
                    length = 3;
                }

                PersistantAnnotation subAnnotationAnnotation = new PersistantAnnotation(annotation.getId(), annotation.getEcNumber(),
                        annotation.getLocus(), annotation.getProduct(), subAnnotation.getStart(), subAnnotation.getStop(),
                        annotation.isFwdStrand(), subAnnotation.getType(), annotation.getGeneName());
                JAnnotation jSubAnnotation = new JAnnotation(subAnnotationAnnotation, length, this, border);
                int yFrom = yCoord - (jSubAnnotation.getHeight() / 2);
                jSubAnnotation.setBounds((int) phyStart, yFrom, jSubAnnotation.getSize().width, jSubAnnotation.getHeight());

                this.subAnnotations.add(jSubAnnotation);
                subfeatAdded = true;
            }
        }

        if (!this.getExcludedFeatureTypes().contains(annotation.getType()) || subfeatAdded) {
            byte border = JAnnotation.BORDER_NONE;
            // get left boundary of the annotation
            double phyStart = this.getPhysBoundariesForLogPos(annotation.getStart()).getLeftPhysBound();
            if (phyStart < bounds.getPhyLeft()) {
                phyStart = bounds.getPhyLeft();
                border = JAnnotation.BORDER_LEFT;
            }

            // get right boundary of the annotation
            double phyStop = this.getPhysBoundariesForLogPos(annotation.getStop()).getRightPhysBound();
            if (phyStop > bounds.getPhyRight()) {
                phyStop = bounds.getPhyRight();
                border = border == JAnnotation.BORDER_LEFT ? JAnnotation.BORDER_BOTH : JAnnotation.BORDER_RIGHT;
            }

            // set a minimum length to be displayed, otherwise a high zoomlevel could
            // lead to dissapearing annotations
            double length = phyStop - phyStart;
            if (length < 3) {
                length = 3;
            }

            JAnnotation jAnnotation = new JAnnotation(annotation, length, this, border);
            int yFrom = yCoord - (jAnnotation.getHeight() / 2);
            jAnnotation.setBounds((int) phyStart, yFrom, jAnnotation.getSize().width, jAnnotation.getHeight());

            if (currentlySelectedAnnotation != null) {
                if (annotation.getId() == currentlySelectedAnnotation.getPersistantAnnotation().getId()) {
                    setSelectedAnnotation(jAnnotation);
                }
            }

            this.annotations.add(jAnnotation);
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
     * @param annotation annotation whose frame has to be determined
     * @return 1, 2, 3, -1, -2, -3 depending on the reading frame of the annotation
     */
    public int determineFrame(PersistantAnnotation annotation) {
        int frame = 0;

        if (annotation.isFwdStrand()) {
            // forward strand
            frame = (annotation.getStart() - 1) % 3 + 1;
        } else {
            // reverse strand. start <= stop ALWAYS! so use stop for reverse strand
            frame = (annotation.getStop() - 1) % 3 - 3;
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

    public Map<FeatureType, Integer> getAnnotationStats() {
        return this.annotationStats;
    }

    public JAnnotation getCurrentlySelectedAnnotation() {
        return this.currentlySelectedAnnotation;
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
