package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfo;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.LogicalBoundsListener;
import de.cebitec.vamp.view.dataVisualisation.MousePositionListener;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * AbstractViewer ist a superclass for displaying genome related information.
 * It provides methods to compute the physical position (meaning pixel) for any
 * logical position (base position in genome) and otherwise. Depending on it's
 * own size and settings in the ViewerController AbstractViewer knows, which
 * intervall from the genome should be diplayed currently and provides getter
 * methods for these values
 * @author ddoppmeier
 */
public abstract class AbstractViewer extends JPanel implements LogicalBoundsListener, MousePositionListener{

    private static final long serialVersionUID = 1L;

    // logical coordinates for genome interval
    private BoundsInfo bounds;

    // correlation factor to compute physical position from logical position
    private double correlationFactor;
    
    // gap at the sides of panel
    private int horizontalMargin;
    private int verticalMargin;

    private double basewidth;
    private BoundsInfoManager boundsManager;
    private int oldLogMousePos;
    private int currentLogMousePos;

    private boolean printMouseOver;
    private BasePanel basePanel;

    private SequenceBar seqBar;
    private PaintingAreaInfo paintingAreaInfo;

    private PersistantReference reference;

    private boolean isInMaxZoomLevel;
    private boolean inDrawingMode;

    private boolean isActive;

    private LegendLabel legendLabel;
    private JPanel legend;
    private boolean hasLegend;

    public static final String PROP_MOUSEPOSITION_CHANGED = "mousePos changed";
    public static final String PROP_MOUSEOVER_REQUESTED = "mouseOver requested";
    public static final Color backgroundColor = new Color(240, 240, 240); //to prevent wrong color on mac

    public AbstractViewer(BoundsInfoManager boundsManager, BasePanel basePanel, PersistantReference reference){
        super();
        this.setLayout(null);
        this.setBackground(AbstractViewer.backgroundColor);
        this.boundsManager = boundsManager;
        this.basePanel = basePanel;
        this.reference = reference;

        // per default, show every available detail
        isInMaxZoomLevel = true;
        inDrawingMode = true;
        isActive = true;

        // sets min, max and preferred size
        this.setSizes();

        // init physical bounds
        horizontalMargin = 40;
        verticalMargin = 10;


        paintingAreaInfo = new PaintingAreaInfo();
        this.adjustPaintingAreaInfo();

        printMouseOver = false;
        // setup all components
        this.initComponents();
        bounds = new BoundsInfo(0, 0, 0, 0);

        this.calcBaseWidth();
        this.recalcCorrelatioFactor();

    }

    private void setSizes(){
        setMinimumSize(new Dimension(Integer.MIN_VALUE, getMaximalHeight()));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getMaximalHeight()));
        setPreferredSize(new Dimension(getPreferredSize().width, getMaximalHeight()));
    }

    public void close() {
        boundsManager.removeBoundListener(this);      
    }

    public void setupLegend(LegendLabel label, JPanel legend){
        this.hasLegend = true;

        int labelX = 2;
        int labelY = 0;

        this.legendLabel = label;
        this.legendLabel.setSize(new Dimension(70, 20));
        this.legendLabel.setBounds(labelX, labelY, this.legendLabel.getSize().width, this.legendLabel.getSize().height);

        this.legend = legend;
        int legendY = labelY + legendLabel.getSize().height + 2;

        this.legend.setBounds(labelX, legendY, legend.getPreferredSize().width, legend.getPreferredSize().height);
        this.legend.setVisible(false);
    }

    public void showSequenceBar(boolean showSeqBar){
        if(showSeqBar){
            this.seqBar = new SequenceBar(this, reference);
        } else {
            seqBar = null;
        }
        this.updatePhysicalBounds();
    }

    private void adjustPaintingAreaInfo(){
        paintingAreaInfo.setForwardHigh(verticalMargin);
        paintingAreaInfo.setReverseHigh(this.getSize().height-1 -verticalMargin);
        paintingAreaInfo.setPhyLeft(horizontalMargin);
        paintingAreaInfo.setPhyRight(this.getSize().width-1 - horizontalMargin);

        // if existant, leave space for sequence viewer
        if(this.seqBar != null){
            int y1 = this.getSize().height / 2 - seqBar.getSize().height / 2;
            int y2 = this.getSize().height / 2 + seqBar.getSize().height / 2;
            seqBar.setBounds(0, y1, this.getSize().width , seqBar.getSize().height);
            paintingAreaInfo.setForwardLow(y1 -1);
            paintingAreaInfo.setReverseLow(y2 +1);

        } else {
            paintingAreaInfo.setForwardLow(this.getSize().height / 2 -1);
            paintingAreaInfo.setReverseLow(this.getSize().height / 2 +1);
        }
    }

    public boolean hasSequenceBar(){
        if(seqBar != null){
            return true;
        } else {
            return false;
        }
    }

    public SequenceBar getSequenceBar(){
        return seqBar;
    }

    protected abstract int getMaximalHeight();

    private void initComponents(){
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updatePhysicalBounds();
            }
        });
        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {}

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                // only report mouse position when moved over viewing area and panel is requested to draw
                int tmpPos = transformToLogicalCoord(p.x);
                if(tmpPos >= getBoundsInfo().getLogLeft() && tmpPos <= getBoundsInfo().getLogRight() && isInDrawingMode()){
                    basePanel.reportMouseOverPaintingStatus(true);
                    basePanel.reportCurrentMousePos(tmpPos);
                } else {
                    basePanel.reportMouseOverPaintingStatus(false);
                    AbstractViewer.this.repaintMousePosition(AbstractViewer.this.getCurrentMousePos(), AbstractViewer.this.getCurrentMousePos());
                }
            }
        });
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {
                basePanel.reportMouseOverPaintingStatus(false);
            }
        });
    }

    public abstract void changeToolTipText(int logPos);

    /**
     * Compute the space that is currently assigned for one base of the genome
     */
    private void calcBaseWidth(){
        basewidth = (double) paintingAreaInfo.getPhyWidt() / bounds.getLogWidth();
    }

    /**
     * Returns the physical boundaries (left, right) of a single base of the sequence.
     * @param logPos
     * @return
     */
    public PhysicalBaseBounds getPhysBoundariesForLogPos(int logPos){
        double left =  transformToPhysicalCoord(logPos);
        double right = left + basewidth - 1;
        return new PhysicalBaseBounds(left, right);
    }

    /**
     * Compute the horizontal position (pixel) for a logical position (base in genome).
     * If a base has more space than one pixel, this method returns the leftmost pixel,
     * meaning the beginning of the available interval for displaying this base
     * @param logPos a position in the genome
     * @return horizontal position for requested base position
     */
    protected double transformToPhysicalCoord(int logPos){
        double tmp = (logPos - bounds.getLogLeft())*correlationFactor + horizontalMargin;
        return tmp;
    }

    /**
     * Compute the logical position for any given physical position
     * @param physPos horizontal position of a pixel
     * @return logical position corresponding to the pixel
     */
    protected int transformToLogicalCoord(int physPos){
        return (int) (((double) physPos - horizontalMargin) / correlationFactor + bounds.getLogLeft());
    }

    /**
     * Logical position are mapped to physical position by multiplying with a
     * correlationfactor, which is updated by this method, depending on the
     * current width of this panel
     */
    private void recalcCorrelatioFactor(){
        correlationFactor =  (double) paintingAreaInfo.getPhyWidt() / bounds.getLogWidth();
    }

    /**
     * Update the physical coordinates of this panel, available width for painting.
     * Method is called automatically, when this panel resizes
     */
    private void updatePhysicalBounds(){

        this.adjustPaintingAreaInfo();
        this.boundsManager.getUpdatedBoundsInfo((LogicalBoundsListener) this);
    }

    /**
     * Assign new logical bounds to this panel, meaning the range from the genome
     * that should be displayed
     * @param bounds Information about the intervall that should be displayed
     * and the current position
     */
    @Override
    public void updateLogicalBounds(BoundsInfo bounds){
        this.bounds = bounds;
        calcBaseWidth();
        recalcCorrelatioFactor();

        if(basewidth > 7){
            this.setIsInMaxZoomLevel(true);
        } else {
            this.setIsInMaxZoomLevel(false);
        }
        if(seqBar != null){
            seqBar.boundsChanged();
        }
        if(isActive()){
            boundsChangedHook();
            repaint();
        }
    }

    @Override
    public void setCurrentMousePosition(int newPos){
        oldLogMousePos = currentLogMousePos;
        currentLogMousePos = newPos;
        if(oldLogMousePos != currentLogMousePos){
            this.repaintMousePosition(oldLogMousePos, currentLogMousePos);
            this.firePropertyChange(PROP_MOUSEPOSITION_CHANGED, oldLogMousePos, currentLogMousePos);
        }

        if(newPos >= this.getBoundsInfo().getLogLeft() && newPos <= this.getBoundsInfo().getLogRight()){
            this.changeToolTipText(newPos);
        } else {
            this.setToolTipText(null);
        }
    }

    /**
     * Repaints the component.
     * @param oldPos the old mouse position
     * @param newPos the new mouse position
     */
    private void repaintMousePosition(int oldPos, int newPos){
        if(isInDrawingMode()){
            PhysicalBaseBounds mouseAreaOld = getPhysBoundariesForLogPos(oldPos);
            PhysicalBaseBounds mouseAreaNew = getPhysBoundariesForLogPos(newPos);

            int min;
            int max;
            if(oldPos >= newPos){
                min = (int) mouseAreaNew.getLeftPhysBound();
                max = (int) mouseAreaOld.getLeftPhysBound()+getWidthOfMouseOverlay(oldPos);
            } else {
                min = (int) mouseAreaOld.getLeftPhysBound();
                max = (int) mouseAreaNew.getLeftPhysBound()+getWidthOfMouseOverlay(newPos);
            }
            min--;
            max++;
            int width = max - min +1;

            repaint(min, 0, width, this.getHeight()-1);
        }
    }

    /**
     * Paints the rectangle marking the mouse position. It covers the whole height
     * of the viewer.
     * @param g the grapics object to paint in
     */
    private void drawMouseCursor(Graphics g){
        int currentLogPos = getCurrentMousePos();
        if(getBoundsInfo().getLogLeft() <= currentLogPos && currentLogPos <= getBoundsInfo().getLogRight()){
            PhysicalBaseBounds mouseArea = this.getPhysBoundariesForLogPos(currentLogPos);
            int width = getWidthOfMouseOverlay(currentLogPos);
            PaintingAreaInfo info = this.getPaintingAreaInfo();
            g.drawRect((int)mouseArea.getLeftPhysBound(), info.getForwardHigh(), width-1, info.getCompleteHeight()-1);
        }
    }

    private void paintCurrentCenterPosition(Graphics g){
        PhysicalBaseBounds coords = getPhysBoundariesForLogPos(getBoundsInfo().getCurrentLogPos());
        PaintingAreaInfo info = this.getPaintingAreaInfo();
        g.setColor(ColorProperties.CURRENT_POSITION);
        int width = (int) (coords.getPhysWidth()>= 1 ? coords.getPhysWidth() : 1);
        g.fillRect((int)coords.getLeftPhysBound(), info.getForwardHigh(), width, info.getCompleteHeight());
    }

    protected int getWidthOfMouseOverlay(int position){
        PhysicalBaseBounds mouseArea = getPhysBoundariesForLogPos(position);
        return (int) (mouseArea.getPhysWidth() >= 3 ? mouseArea.getPhysWidth() : 3);
    }

    @Override
    protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);

        if(isInDrawingMode()){
            graphics.setColor(ColorProperties.MOUSEOVER);
            if(printMouseOver){
                drawMouseCursor(graphics);
            }
            paintCurrentCenterPosition(graphics);
        }
    }
    
    @Override
    public void setMouseOverPaintingRequested(boolean requested){
        // repaint whole viewer if mouse curser was painted before, but none is not wanted
        if(printMouseOver && !requested){
            repaint();
        }
        printMouseOver = requested;
        if(!printMouseOver){
            currentLogMousePos = 0;
        }
        firePropertyChange(PROP_MOUSEOVER_REQUESTED, null, requested);
    }

    /**
     * This method defines a hook, that is called every time when the logical
     * positions change. Content of this method is executed after updating the
     * logical position of AbstractViewer. Can be used for retrieving new data
     * from a database for example.
     */
    public abstract void boundsChangedHook();

    /**
     * Returns the current bounds of the visible area of this component.
     * @return the current bounds values
     */
    public BoundsInfo getBoundsInfo(){
        return this.bounds;
    }

    /**
     * @return the current dimension of this panel
     */
    @Override
    public Dimension getPaintingAreaDimension(){
        return new Dimension(paintingAreaInfo.getPhyWidt(), paintingAreaInfo.getCompleteHeight());
    }

    public PaintingAreaInfo getPaintingAreaInfo(){
        return paintingAreaInfo;
    }

    private int getCurrentMousePos(){
        return currentLogMousePos;
    }

    public void forwardChildrensMousePosition(int relPhyPos, JComponent child){
        int phyPos = child.getX() + relPhyPos;
        int logPos = transformToLogicalCoord(phyPos);

        basePanel.reportMouseOverPaintingStatus(true);
        basePanel.reportCurrentMousePos(logPos);
    }

    public boolean isInMaxZoomLevel() {
        return isInMaxZoomLevel;
    }

    public boolean isInDrawingMode() {
        return inDrawingMode;
    }

    private void setIsInMaxZoomLevel(boolean isInMaxZoomLevel) {
        this.isInMaxZoomLevel = isInMaxZoomLevel;

    }

    public void setInDrawingMode(boolean inDrawingMode) {
        this.inDrawingMode = inDrawingMode;
    }

    public PersistantReference getReference() {
        return reference;
    }

    public boolean isActive(){
        return isActive;
    }

    public void setActive(boolean isActive){
        this.isActive = isActive;
        if(isActive){
            updatePhysicalBounds();
        }
    }

    public void updateLegendVisibility(boolean isShowingLegend){
        legend.setVisible(isShowingLegend);
    }

    public LegendLabel getLegendLabel(){
        return legendLabel;
    }

    public boolean hasLegend(){
        return hasLegend;
    }

    public JPanel getLegendPanel(){
        return legend;
    }

    public boolean isLegendVisisble(){
        return legend.isVisible();
    }

    public boolean isMouseOverPaintingRequested() {
        return printMouseOver;
    }

    public void setHorizontalMargin(int horizontalMargin) {
        this.horizontalMargin = horizontalMargin;
        this.adjustPaintingAreaInfo();
    }

    public int getHorizontalMargin(){
        return this.horizontalMargin;
    }

    public void setVerticalMargin(int verticalMargin) {
        this.verticalMargin = verticalMargin;
        this.adjustPaintingAreaInfo();
    }

    public BoundsInfoManager getBoundsInformationManager(){
        return this.boundsManager;
    }

    /**
     * Returns the current width of a single base of the sequence.
     * @return the current width of a single base of the sequence
     */
    public double getBaseWidth(){
        this.calcBaseWidth();
        return this.basewidth;
    }

  }
