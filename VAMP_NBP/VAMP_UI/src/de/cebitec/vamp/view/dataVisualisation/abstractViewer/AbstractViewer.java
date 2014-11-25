package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.view.dataVisualisation.*;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.vamp.view.dialogMenus.MenuItemFactory;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.openide.util.Exceptions;

/**
 * AbstractViewer ist a superclass for displaying genome related information.
 * It provides methods to compute the physical position (meaning pixel) for any
 * logical position (base position in genome) and otherwise. Depending on it's
 * own size and settings in the ViewerController AbstractViewer knows, which
 * interval from the genome should currently be diplayed and provides getter
 * methods for these values. Tooltips in this viewer are initially shown for 
 * 20 seconds.
 * 
 * @author ddoppmeier, rhilker
 */
public abstract class AbstractViewer extends JPanel implements LogicalBoundsListener, MousePositionListener {

    private static final long serialVersionUID = 1L;
    // logical coordinates for genome interval
    private BoundsInfo bounds;
    private boolean isPanning = false;
    private boolean canPan = true;
    // correlation factor to compute physical position from logical position
    private double correlationFactor;
    // gap at the sides of panel
    private int horizontalMargin;
    private int verticalMargin;
    private int zoom = 1;
    private boolean canZoom = true;
    private double basewidth;
    private BoundsInfoManager boundsManager;
    private int oldLogMousePos;
    private int currentLogMousePos; //the position of the genome, where to mouse is currently hovering
    private int lastPhysPos = 0;
    private boolean printMouseOver;
    private BasePanel basePanel;
    private SequenceBar seqBar;
    private boolean centerSeqBar;
    private PaintingAreaInfo paintingAreaInfo;
    private PersistantReference reference;
    private boolean isInMaxZoomLevel;
    private boolean inDrawingMode;
    private boolean isActive;
    private MenuLabel legendLabel;
    private JPanel legend;    
    private boolean hasLegend;
    private MenuLabel optionsLabel;
    private JPanel options;
    private boolean hasOptions;
    private List<FeatureType> excludedFeatureTypes;
    private boolean pAInfoIsAvailable = false;
    public static final String PROP_MOUSEPOSITION_CHANGED = "mousePos changed";
    public static final String PROP_MOUSEOVER_REQUESTED = "mouseOver requested";
    public static final Color backgroundColor = new Color(240, 240, 240); //to prevent wrong color on mac
    private JScrollBar scrollBar; /* Scrollbar, which should adapt, when component is repainted. */
    private boolean centerScrollBar = false;
    private BufferedImage loadingIndicator;

    public AbstractViewer(BoundsInfoManager boundsManager, BasePanel basePanel, PersistantReference reference) {
        super();
        
        //read loadingIndicator icon from package resources
        try {
            InputStream stream = AbstractViewer.class.getResourceAsStream("loading.png");
            this.loadingIndicator = ImageIO.read(stream);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        this.excludedFeatureTypes = new ArrayList<>();
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
        //       this.adjustPaintingAreaInfo();

        printMouseOver = false;
        // setup all components
        this.initComponents();
        bounds = new BoundsInfo(0, 0, 0, 0);

        this.calcBaseWidth();
        this.recalcCorrelationFactor();

    }

    private void setSizes() {
        setMinimumSize(new Dimension(Integer.MIN_VALUE, getMaximalHeight()));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getMaximalHeight()));
        setPreferredSize(new Dimension(getPreferredSize().width, getMaximalHeight()));
    }

    public void close() {
        boundsManager.removeBoundListener(this);
    }

    public void setupLegend(MenuLabel label, JPanel legend) {
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
    
    /**
     * Setup an option panel in the right top corner of the viewer.
     * @param label 
     * @param options 
     */
    public void setupOptions(MenuLabel label, JPanel options) {
        this.hasOptions = true;

        int labelX = 70; // this.getWidth() - 72;
        int labelY = 0;

        this.optionsLabel = label;
        this.optionsLabel.setSize(new Dimension(70, 20));
        this.optionsLabel.setBounds(labelX, labelY, this.optionsLabel.getSize().width, this.optionsLabel.getSize().height);

        this.options = options;
        int legendY = labelY + optionsLabel.getSize().height + 2;

        this.options.setBounds(labelX, legendY, options.getPreferredSize().width, options.getPreferredSize().height);
        this.options.setVisible(false);
    }

    public void showSequenceBar(boolean showSeqBar, boolean centerSeqBar) {
        if (showSeqBar) {
            this.seqBar = new SequenceBar(this, reference);
            this.centerSeqBar = centerSeqBar;
        } else {
            seqBar = null;
        }
        // this.updatePhysicalBounds();
    }
    /*
     * check this error occures!
     */

    private void adjustPaintingAreaInfo() {
        if (this.getHeight() > 0 && this.getWidth() > 0) {
            pAInfoIsAvailable = true;
            paintingAreaInfo.setForwardHigh(verticalMargin);
            paintingAreaInfo.setReverseHigh(this.getHeight() - 1 - verticalMargin);
            paintingAreaInfo.setPhyLeft(horizontalMargin);
            paintingAreaInfo.setPhyRight(this.getWidth() - 1 - horizontalMargin);

            // if existent, leave space for sequence bar
            if (this.seqBar != null) {
                if (centerSeqBar) {
                    int y1 = this.getSize().height / 2 - seqBar.getSize().height / 2;
                    int y2 = this.getSize().height / 2 + seqBar.getSize().height / 2;
                    seqBar.setBounds(0, y1, paintingAreaInfo.getPhyRight(), seqBar.getSize().height);
                    paintingAreaInfo.setForwardLow(y1 - 1);
                    paintingAreaInfo.setReverseLow(y2 + 1);
                } else {
                    seqBar.setBounds(0, 20, this.getSize().width, seqBar.getSize().height);
                    paintingAreaInfo.setForwardLow(20 - 1);
                    paintingAreaInfo.setReverseLow(seqBar.getSize().height + 21);
                }

            } else {
                paintingAreaInfo.setForwardLow(this.getSize().height / 2 - 1);
                paintingAreaInfo.setReverseLow(this.getSize().height / 2 + 1);
            }
        } else {
            pAInfoIsAvailable = false;
        }
    }

    /**
     * @return true, if this viewer has a sequence bar, false otherwise
     */
    public boolean hasSequenceBar() {
        return this.seqBar != null;
    }

    /**
     * @return returns the sequence bar of this viewer
     */
    public SequenceBar getSequenceBar() {
        return this.seqBar;
    }

    protected abstract int getMaximalHeight();

    private void initComponents() {
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updatePhysicalBounds();
            }
        });


        this.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                if (canZoom && ((zoom <= 500 && zoom > 0 && e.getUnitsToScroll() > 0) 
                            || (zoom <= 500 && zoom > 0 && e.getUnitsToScroll() < 0))) {
                    int oldZoom = zoom;
                    zoom += e.getUnitsToScroll();
                    if (zoom > 500) {
                        zoom = 500;
                    }
                    if (zoom < 1) {
                        zoom = 1;
                    }
                    if (zoom < oldZoom) {
                        boundsManager.navigatorBarUpdated(currentLogMousePos);
                    }
                    boundsManager.zoomLevelUpdated(zoom);
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                setPanPosition(e.getX());
            }

            @Override
            public void mouseMoved(MouseEvent e) {

                Point p = e.getPoint();
                // only report mouse position when moved over viewing area and panel is requested to draw
                int tmpPos = transformToLogicalCoord(p.x);
                if (tmpPos >= getBoundsInfo().getLogLeft() && tmpPos <= getBoundsInfo().getLogRight() && isInDrawingMode()) {
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
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (canPan) {
                        isPanning = true;
                        AbstractViewer.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popUp = new JPopupMenu();
                    MenuItemFactory menuItemFactory = new MenuItemFactory();

                    //add copy mouse position option
                    popUp.add(menuItemFactory.getCopyPositionItem(currentLogMousePos));
                    //add center current position option
                    popUp.add(menuItemFactory.getJumpToPosItem(boundsManager, getCurrentMousePos()));
                    popUp.show((JComponent) e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPanning = false;
                AbstractViewer.this.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                basePanel.reportMouseOverPaintingStatus(false);
            }
        });
        
        //ensure the tooltips are shown for 20 seconds to be able to read the data
        ToolTipManager.sharedInstance().setDismissDelay(20000);

    }

    /**
     * Sets the current mouse position as the navigator bar center position, if
     * panning is allowed and panning is currently active
     * @param position 
     */
    private void setPanPosition(int position) {
        if (isPanning && canPan) {
            int logi = transformToLogicalCoordForPannig(position);
            //       Logger.getLogger(this.getClass().getName()).log(Level.INFO, "pos "+position+" logi "+logi);
            boundsManager.navigatorBarUpdated(logi);
        }
    }
    
    /**
     * @return true, if panning is allowed, false otherwise
     */
    public boolean isPanModeOn() {
        return canPan;
    }

    /**
     * @param canPan true, if panning is allowed, false otherwise
     */
    public void setIsPanModeOn(boolean canPan) {
        this.canPan = canPan;
    }

    /**
     * @return <cc>true</cc> if this viewer is allowed to zoom via the mouse
     * wheel, <cc>false</cc> otherwise.
     */
    public boolean isCanZoom() {
        return canZoom;
    }

    /**
     * @param canZoom <cc>true</cc> if this viewer is allowed to zoom via the 
     * mouse wheel, <cc>false</cc> otherwise.
     */
    public void setCanZoom(boolean canZoom) {
        this.canZoom = canZoom;
    }

    public abstract void changeToolTipText(int logPos);

    /**
     * Compute the space that is currently assigned for one base of the genome
     */
    private void calcBaseWidth() {
        if (pAInfoIsAvailable) {
            basewidth = (double) paintingAreaInfo.getPhyWidth() / bounds.getLogWidth();
        }
    }

    /**
     * @param logPos position in the reference genome
     * @return the physical boundaries (left, right) of a single base of the sequence
     * in the viewer.
     */
    public PhysicalBaseBounds getPhysBoundariesForLogPos(int logPos) {
        double left = this.transformToPhysicalCoord(logPos);
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
    protected double transformToPhysicalCoord(int logPos) {
        double tmp = (logPos - bounds.getLogLeft()) * correlationFactor + horizontalMargin;
        return tmp;
    }
    
    /**
     * Converts the physical pixel position into the logical sequence position
     * for a given phyPos.
     * @param phyPos physical position (pixel) in the reference genome
     * @return the logical position of a single base in the sequence.
     */
    public int getLogicalPosForPixel(int phyPos) {
        return this.transformToLogicalCoord(phyPos);
    }

    /**
     * Compute the logical position for any given physical position (pixel).
     * @param physPos horizontal position of a pixel
     * @return logical position corresponding to the pixel
     */
    protected int transformToLogicalCoord(int physPos) {
        //       Logger.getLogger(this.getClass().getName()).log(Level.INFO, "boundsLeft "+ bounds.getLogLeft()+"right"+ bounds.getLogRight());
        return (int) (((double) physPos - horizontalMargin) / correlationFactor + bounds.getLogLeft());

    }

    /**
     * Compute the logical position for any given physical position
     * @param physPos horizontal position of a pixel
     * @return logical position corresponding to the pixel
     */
    protected int transformToLogicalCoordForPannig(int physPos) {

        int pos;
        int currentLog = bounds.getCurrentLogPos();
        int leftbound = bounds.getLogLeft();
        int rightBound = bounds.getLogRight();
        pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + bounds.getLogLeft());
        int lb = leftbound - ((rightBound - leftbound) / 2);
        //we want to go to smaller positions
        if (lastPhysPos > physPos) {
            lastPhysPos = physPos;
            //mouse on the right side of currentLog
            pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + lb);
        } else {
            lastPhysPos = physPos;
            //mouse on the right side of currentLog
            if (currentLog < pos) {
                pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + leftbound);
                // Logger.getLogger(this.getClass().getName()).log(Level.INFO, "rightside plus "+pos);
            } else {
                pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + rightBound);
                //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "leftside plus "+pos);
            }

        }
        if (pos <= 0) {
            pos = 1;
        }
        return pos;

    }

    /**
     * Logical position are mapped to physical position by multiplying with a
     * correlation factor, which is updated by this method, depending on the
     * current width of this panel
     */
    private void recalcCorrelationFactor() {
        if (pAInfoIsAvailable) {
            correlationFactor = (double) paintingAreaInfo.getPhyWidth() / bounds.getLogWidth();
        }
    }

    /**
     * Update the physical coordinates of this panel, available width for painting.
     * Method is called automatically, when this panel resizes
     */
    public void updatePhysicalBounds() {
        this.setSizes();
        this.adjustPaintingAreaInfo();
        this.boundsManager.getUpdatedBoundsInfo((LogicalBoundsListener) this);
    }

    /**
     * Assign new logical bounds to this panel, meaning the range from the genome
     * that should be displayed. In case the abstract viewer was handed over a scrollbar
     * the value of the scrollbar is adjusted to the middle.
     * @param bounds Information about the interval that should be displayed
     * and the current position
     */
    @Override
    public void updateLogicalBounds(BoundsInfo bounds) {
//        if (!this.bounds.equals(bounds)) {
            this.bounds = bounds;
            this.calcBaseWidth();
            this.recalcCorrelationFactor();

            if (this.basewidth > 7) {
                this.setIsInMaxZoomLevel(true);
            } else {
                this.setIsInMaxZoomLevel(false);
            }
            if (this.seqBar != null) {
                this.seqBar.boundsChanged();
            }
            if (this.isActive()) {
                this.boundsChangedHook();
                this.repaint();
            }
//        }

        if (this.scrollBar != null && this.centerScrollBar) {
            this.scrollBar.setValue(this.scrollBar.getMaximum() / 2 - this.getParent().getHeight() / 2);
        }
    }

    @Override
    public void setCurrentMousePosition(int newPos) {
        oldLogMousePos = currentLogMousePos;
        currentLogMousePos = newPos;
        if (oldLogMousePos != currentLogMousePos) {
            this.repaintMousePosition(oldLogMousePos, currentLogMousePos);
            this.firePropertyChange(PROP_MOUSEPOSITION_CHANGED, oldLogMousePos, currentLogMousePos);
        }

        if (newPos >= this.getBoundsInfo().getLogLeft() && newPos <= this.getBoundsInfo().getLogRight()) {
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
    private void repaintMousePosition(int oldPos, int newPos) {
        if (isInDrawingMode()) {
            PhysicalBaseBounds mouseAreaOld = getPhysBoundariesForLogPos(oldPos);
            PhysicalBaseBounds mouseAreaNew = getPhysBoundariesForLogPos(newPos);

            int min;
            int max;
            if (oldPos >= newPos) {
                min = (int) mouseAreaNew.getLeftPhysBound();
                max = (int) mouseAreaOld.getLeftPhysBound() + getWidthOfMouseOverlay(oldPos);
            } else {
                min = (int) mouseAreaOld.getLeftPhysBound();
                max = (int) mouseAreaNew.getLeftPhysBound() + getWidthOfMouseOverlay(newPos);
            }
            min--;
            max++;
            int width = max - min + 1;

            repaint(min, 0, width, this.getHeight() - 1);
        }
    }

    /**
     * Paints the rectangle marking the mouse position. It covers the whole height
     * of the viewer.
     * @param g the grapics object to paint in
     */
    private void drawMouseCursor(Graphics g) {
        int currentLogPos = getCurrentMousePos();
        if (getBoundsInfo().getLogLeft() <= currentLogPos && currentLogPos <= getBoundsInfo().getLogRight()) {
            PhysicalBaseBounds mouseArea = this.getPhysBoundariesForLogPos(currentLogPos);
            int width = getWidthOfMouseOverlay(currentLogPos);
            PaintingAreaInfo info = this.getPaintingAreaInfo();
            g.drawRect((int) mouseArea.getLeftPhysBound(), info.getForwardHigh(), width - 1, info.getCompleteHeight() - 1);
        }
    }

    private void paintCurrentCenterPosition(Graphics g) {
        PhysicalBaseBounds coords = getPhysBoundariesForLogPos(getBoundsInfo().getCurrentLogPos());
        PaintingAreaInfo info = this.getPaintingAreaInfo();
        g.setColor(ColorProperties.CURRENT_POSITION);
        int width = (int) (coords.getPhysWidth() >= 1 ? coords.getPhysWidth() : 1);
        g.fillRect((int) coords.getLeftPhysBound(), info.getForwardHigh(), width, info.getCompleteHeight());
    }

    protected int getWidthOfMouseOverlay(int position) {
        PhysicalBaseBounds mouseArea = getPhysBoundariesForLogPos(position);
        return (int) (mouseArea.getPhysWidth() >= 3 ? mouseArea.getPhysWidth() : 3);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (isInDrawingMode()) {
            graphics.setColor(ColorProperties.MOUSEOVER);
            if (printMouseOver) {
                drawMouseCursor(graphics);
            }
            paintCurrentCenterPosition(graphics);
        }
    }

    @Override
    public void setMouseOverPaintingRequested(boolean requested) {
        // repaint whole viewer if mouse curser was painted before, but none is not wanted
        if (printMouseOver && !requested) {
            repaint();
        }
        printMouseOver = requested;
        if (!printMouseOver) {
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
    public BoundsInfo getBoundsInfo() {
        return this.bounds;

    }

    /**
     * @return The size of the area, that is used for drawing. Logical bounds
     * depend on the available size of each listener.
     */
    @Override
    public Dimension getPaintingAreaDimension() {
        return pAInfoIsAvailable ? new Dimension(paintingAreaInfo.getPhyWidth(), paintingAreaInfo.getCompleteHeight()) : null;

    }

    /**
     * @return true, if the PaintingArea has coordinates to calculate bounds,
     * false otherwise.
     */
    @Override
    public boolean isPaintingAreaAvailable() {
        return pAInfoIsAvailable;
    }

    public PaintingAreaInfo getPaintingAreaInfo() {
        return paintingAreaInfo;
    }

    public int getCurrentMousePos() {
        return currentLogMousePos;
    }

    public void forwardChildrensMousePosition(int relPhyPos, JComponent child) {
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
        return this.reference;
    }

    /**
     * @return true, if this viewer is currently active (in the foreground)
     * and false, if it is inactive
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Set true, if this viewer should be active (in the foreground or it needs
     * to update its data) and false, if it should be inactive.
     * @param isActive true, if this viewer should be active and false, if not
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
        if (isActive) {
            this.setSizes();
            this.updatePhysicalBounds();
        }
    }

    public MenuLabel getLegendLabel() {
        return this.legendLabel;
    }

    public boolean hasLegend() {
        return this.hasLegend;
    }

    public JPanel getLegendPanel() {
        return this.legend;
    }

    public boolean isLegendVisisble() {
        return this.legend.isVisible();
    }
    
    public MenuLabel getOptionsLabel() {
        return this.optionsLabel;
    }
    
    public boolean hasOptions() {
        return this.hasOptions;
    }
    
    public JPanel getOptionsPanel() {
        return this.options;
    }
    
    public boolean isOptionsVisible() {
        return this.options.isVisible();
    }

    public List<FeatureType> getExcludedFeatureTypes() {
        return this.excludedFeatureTypes;
    }

    public boolean isMouseOverPaintingRequested() {
        return printMouseOver;
    }

    public void setHorizontalMargin(int horizontalMargin) {
        this.horizontalMargin = horizontalMargin;
        this.adjustPaintingAreaInfo();
    }

    public int getHorizontalMargin() {
        return this.horizontalMargin;
    }

    public void setVerticalMargin(int verticalMargin) {
        this.verticalMargin = verticalMargin;
        this.adjustPaintingAreaInfo();
    }

    public BoundsInfoManager getBoundsInformationManager() {
        return this.boundsManager;
    }

    /**
     * @return the current width of a single base of the sequence
     */
    public double getBaseWidth() {
        this.calcBaseWidth();
        return this.basewidth;
    }

    public Dimension getBasePanelSize() {
        return this.basePanel.getSize();
    }

    /**
     * A scrollbar should be handed over, in case the scrollbar should adapt its
     * value to the middle position, whenever the genome position was updated.
     * @param scrollBar the scrollbar which should adapt
     */
    public void setScrollBar(JScrollBar scrollBar) {
        this.scrollBar = scrollBar;
    }
    
    /**
     * Sets the property for centering the scrollbar around the center (sequence bar) (true)
     * or not (false).
     * @param centerScrollBar true, if the scrollbar should center around the sequence bar,
     *              false otherwise
     */
    public void setAutomaticCentering(boolean centerScrollBar) {
        this.centerScrollBar = centerScrollBar;
    }

    /**
     * @return The image to display, if the viewer waits for something.
     */
    public BufferedImage getLoadingIndicator() {
        return loadingIndicator;
    }
    
}
