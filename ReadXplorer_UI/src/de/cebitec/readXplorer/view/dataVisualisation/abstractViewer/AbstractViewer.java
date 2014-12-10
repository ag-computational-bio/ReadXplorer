/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;


import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfo;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.LogicalBoundsListener;
import de.cebitec.readXplorer.view.dataVisualisation.MousePositionListener;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import de.cebitec.readXplorer.view.dialogMenus.MenuItemFactory;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.openide.util.Exceptions;


/**
 * AbstractViewer ist a superclass for displaying genome related information.
 * It provides methods to compute the physical position (meaning pixel) for any
 * logical position (base position in genome) and otherwise. Depending on it's
 * own size and settings in the ViewerController AbstractViewer knows, which
 * interval from the genome should currently be displayed and provides getter
 * methods for these values. Tooltips in this viewer are initially shown for
 * 20 seconds.
 * <p>
 * @author ddoppmeier, rhilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public abstract class AbstractViewer extends JPanel implements
        LogicalBoundsListener, MousePositionListener {

    private static final long serialVersionUID = 1L;
    /**
     * x position of a legend label or menu.
     */
    private static final int LEGEND_X = 2;
    /**
     * x position of an options label or menu.
     */
    private static final int OPTIONS_X = 70;

    /**
     * logical coordinates for genome interval.
     */
    private BoundsInfo bounds;
    private boolean isPanning = false;
    private boolean canPan = true;
    /**
     * Correlation factor to convert logical position into physical position
     */
    private double correlationFactor;
    /**
     * Gap at the edges of the panel.
     */
    private int horizontalMargin;
    private int verticalMargin;
    private int zoom = 1;
    private boolean canZoom = true;
    private double basewidth;
    private final BoundsInfoManager boundsManager;
    private int oldLogMousePos;
    /**
     * the position of the genome, where to mouse is currently hovering.
     */
    private int currentLogMousePos;
    private int lastPhysPos = 0;
    private boolean printMouseOver;
    private final BasePanel basePanel;
    private SequenceBar seqBar;
    private boolean centerSeqBar;
    private final PaintingAreaInfo paintingAreaInfo;
    private final PersistentReference reference;
    private boolean isInMaxZoomLevel;
    private boolean inDrawingMode;
    private boolean isActive;
    private MenuLabel legendLabel;
    private JPanel legend;
    private boolean hasLegend;
    private MenuLabel optionsLabel;
    private JPanel options;
    private boolean hasOptions;
    private JPanel chromSelectionPanel;
    private boolean hasChromSelection;
    private final List<Classification> excludedClassifications;
    private byte minMappingQuality = 0;
    private boolean pAInfoIsAvailable = false;
    public static final String PROP_MOUSEPOSITION_CHANGED = "mousePos changed";
    public static final String PROP_MOUSEOVER_REQUESTED = "mouseOver requested";
    public static final Color backgroundColor = new Color( 240, 240, 240 ); //to prevent wrong color on mac
    /**
     * Scrollpane, which should adapt, when component is repainted.
     */
    private JScrollPane scrollPane;
    private boolean centerScrollBar = false;
    private BufferedImage loadingIndicator;
    private boolean newDataRequestNeeded = false;


    /**
     * AbstractViewer ist a superclass for displaying genome related
     * information. It provides methods to compute the physical position
     * (meaning pixel) for any logical position (base position in genome) and
     * otherwise. Depending on it's own size and settings in the
     * ViewerController AbstractViewer knows, which interval from the genome
     * should currently be displayed and provides getter methods for these
     * values. Tooltips in this viewer are initially shown for 20 seconds.
     * <p>
     * @param boundsManager The reference bounds manager of this viewer
     * @param basePanel     the base panel to paint data on
     * @param reference     the associated reference genome
     */
    public AbstractViewer( BoundsInfoManager boundsManager, BasePanel basePanel, PersistentReference reference ) {
        super();

        //read loadingIndicator icon from package resources
        try {
            InputStream stream = AbstractViewer.class.getResourceAsStream( "loading.png" );
            this.loadingIndicator = ImageIO.read( stream );
        }
        catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }

        this.excludedClassifications = new ArrayList<>();
        this.setLayout( null );
        this.setBackground( AbstractViewer.backgroundColor );
        this.boundsManager = boundsManager;
        this.basePanel = basePanel;
        this.reference = reference;

        // per default, show every available detail
        isInMaxZoomLevel = true;
        inDrawingMode = true;
        isActive = true;

        // init physical bounds
        horizontalMargin = 40;
        verticalMargin = 10;


        paintingAreaInfo = new PaintingAreaInfo();
        //       this.adjustPaintingAreaInfo();

        printMouseOver = false;
        // setup all components
        this.initComponents();
        bounds = new BoundsInfo( 0, 0, 0, 0, 0 );

        this.calcBaseWidth();
        this.recalcCorrelationFactor();

    }


    /**
     * Sets the minimum, maximum and preferred size of this viewer based on the
     * subclass implementation of <code>getMaximalHeight()</code>.
     */
    public void setSizes() {
        setMinimumSize( new Dimension( Integer.MIN_VALUE, getMaximalHeight() ) );
        setMaximumSize( new Dimension( Integer.MAX_VALUE, getMaximalHeight() ) );
        setPreferredSize( new Dimension( getPreferredSize().width, getMaximalHeight() ) );
    }


    /**
     * Carries out the stuff to do when the abstract viewer is closed.
     */
    public void close() {
        boundsManager.removeBoundListener( this );
    }


    /**
     * Carries out the setup of a legend panel.
     * <p>
     * @param label  The menu to display including a title.
     * @param legend The legend panel to display
     */
    public void setupLegend( MenuLabel label, JPanel legend ) {
        this.hasLegend = true;

        int labelY = 0;
        this.setupMenu( label, legend, LEGEND_X, labelY );

        this.legendLabel = label;
        this.legend = legend;
    }


    /**
     * Setup an option panel next to the legend panel.
     * <p>
     * @param label   The menu to display including a title.
     * @param options The options panel to display
     */
    public void setupOptions( MenuLabel label, JPanel options ) {
        this.hasOptions = true;

        int labelY = 0;
        this.setupMenu( label, options, OPTIONS_X, labelY );

        this.optionsLabel = label;
        this.options = options;
    }


    /**
     * Setup a menu panel somewhere.
     * <p>
     * @param menuLabel The menu to display including a title.
     * @param menu      The menu panel to display
     */
    private void setupMenu( MenuLabel menuLabel, JPanel menu, int x, int y ) {

        menuLabel.setSize( new Dimension( 70, 20 ) );
        menuLabel.setBounds( x, y, menuLabel.getSize().width, menuLabel.getSize().height );

        int menuY = y + menuLabel.getSize().height + 2;

        menu.setBounds( x, menuY, menu.getPreferredSize().width, menu.getPreferredSize().height );
        menu.setVisible( false );
    }


    /**
     * Setup a chromosome selection panel next to the legend panel.
     * <p>
     * @param chromSelectionPanel the chromosome selection panel to display
     */
    public void setupChromSelectionPanel( JPanel chromSelectionPanel ) {

        this.hasChromSelection = true;
        chromSelectionPanel.setBounds( 72, 0, chromSelectionPanel.getSize().width, chromSelectionPanel.getSize().height );
        chromSelectionPanel.setVisible( true );
        this.chromSelectionPanel = chromSelectionPanel;
    }


    /**
     * Enables showing or hiding the sequence bar.
     * <p>
     * @param showSeqBar   true, if the sequence bar shall be visible, false
     *                     otherwise
     * @param centerSeqBar true, if the sequence bar shall be centered, false
     *                     if it shall be shown at the top
     */
    public void showSequenceBar( boolean showSeqBar, boolean centerSeqBar ) {
        if( showSeqBar ) {
            this.seqBar = new SequenceBar( this );
            this.centerSeqBar = centerSeqBar;
        }
        else {
            seqBar = null;
        }
        // this.updatePhysicalBounds();
    }


    /**
     * Updates the painting area info of this viewer according to the currently
     * available heigth and width.
     */
    protected void adjustPaintingAreaInfo() {
        this.adjustPaintingAreaInfo( this.getSize().height );
    }


    /**
     * Updates the painting area info of this viewer according to the preferred
     * available heigth and width.
     */
    protected void adjustPaintingAreaInfoPrefSize() {
        this.adjustPaintingAreaInfo( this.getPreferredSize().height );
    }


    /**
     * Updates the painting area info of this viewer according to the available
     * heigth and width.
     */
    private void adjustPaintingAreaInfo( int height ) {
        if( this.getHeight() > 0 && this.getWidth() > 0 ) {
            pAInfoIsAvailable = true;
            paintingAreaInfo.setForwardHigh( verticalMargin );
            paintingAreaInfo.setReverseHigh( this.getHeight() - 1 - verticalMargin );
            paintingAreaInfo.setPhyLeft( horizontalMargin );
            paintingAreaInfo.setPhyRight( this.getWidth() - 1 - horizontalMargin );

            // if existent, leave space for sequence bar
            if( this.seqBar != null ) {
                if( centerSeqBar ) {
                    int y1 = height / 2 - seqBar.getSize().height / 2;
                    int y2 = height / 2 + seqBar.getSize().height / 2;
                    seqBar.setBounds( 0, y1, paintingAreaInfo.getPhyRight(), seqBar.getSize().height );
                    paintingAreaInfo.setForwardLow( y1 - 1 );
                    paintingAreaInfo.setReverseLow( y2 + 1 );
                }
                else {
                    seqBar.setBounds( 0, 20, this.getSize().width, seqBar.getSize().height );
                    paintingAreaInfo.setForwardLow( 20 - 1 );
                    paintingAreaInfo.setReverseLow( seqBar.getSize().height + 21 );
                }

            }
            else {
                paintingAreaInfo.setForwardLow( height / 2 - 1 );
                paintingAreaInfo.setReverseLow( height / 2 + 1 );
            }
        }
        else {
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


    /**
     * @return The maximal height of this viewer.
     */
    protected abstract int getMaximalHeight();


    /**
     * Initializes all listeners of this viewer.
     */
    private void initComponents() {
        this.addComponentListener( new ComponentAdapter() {

            @Override
            public void componentResized( java.awt.event.ComponentEvent evt ) {
                updatePhysicalBounds();
            }


        } );


        this.addMouseWheelListener( new MouseWheelListener() {

            @Override
            public void mouseWheelMoved( MouseWheelEvent e ) {

                if( canZoom && ((zoom <= 500 && zoom > 0 && e.getUnitsToScroll() > 0)
                                || (zoom <= 500 && zoom > 0 && e.getUnitsToScroll() < 0)) ) {
                    int oldZoom = zoom;
                    zoom += e.getUnitsToScroll();
                    if( zoom > 500 ) {
                        zoom = 500;
                    }
                    if( zoom < 1 ) {
                        zoom = 1;
                    }
                    if( zoom < oldZoom ) {
                        boundsManager.navigatorBarUpdated( currentLogMousePos );
                    }
                    boundsManager.zoomLevelUpdated( zoom );
                }
            }


        } );

        this.addMouseMotionListener( new MouseMotionListener() {

            @Override
            public void mouseDragged( MouseEvent e ) {
                setPanPosition( e.getX() );
            }


            @Override
            public void mouseMoved( MouseEvent e ) {

                Point p = e.getPoint();
                // only report mouse position when moved over viewing area and panel is requested to draw
                int tmpPos = transformToLogicalCoord( p.x );
                if( tmpPos >= getBoundsInfo().getLogLeft() && tmpPos <= getBoundsInfo().getLogRight() && isInDrawingMode() ) {
                    basePanel.reportMouseOverPaintingStatus( true );
                    basePanel.reportCurrentMousePos( tmpPos );
                }
                else {
                    basePanel.reportMouseOverPaintingStatus( false );
                    AbstractViewer.this.repaintMousePosition( AbstractViewer.this.getCurrentMousePos(), AbstractViewer.this.getCurrentMousePos() );
                }

            }


        } );
        this.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
            }


            @Override
            public void mousePressed( MouseEvent e ) {

                if( SwingUtilities.isLeftMouseButton( e ) ) {
                    if( canPan ) {
                        isPanning = true;
                        AbstractViewer.this.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                    }
                }
                if( SwingUtilities.isRightMouseButton( e ) ) {
                    JPopupMenu popUp = new JPopupMenu();
                    MenuItemFactory menuItemFactory = new MenuItemFactory();

                    //add copy mouse position option
                    popUp.add( menuItemFactory.getCopyPositionItem( currentLogMousePos ) );
                    //add center current position option
                    popUp.add( menuItemFactory.getJumpToPosItem( boundsManager, getCurrentMousePos() ) );
                    popUp.show( e.getComponent(), e.getX(), e.getY() );
                }
            }


            @Override
            public void mouseReleased( MouseEvent e ) {
                isPanning = false;
                AbstractViewer.this.setCursor( Cursor.getDefaultCursor() );
            }


            @Override
            public void mouseEntered( MouseEvent e ) {
            }


            @Override
            public void mouseExited( MouseEvent e ) {
                basePanel.reportMouseOverPaintingStatus( false );
            }


        } );

        //ensure the tooltips are shown for 20 seconds to be able to read the data
        ToolTipManager.sharedInstance().setDismissDelay( 20000 );

    }


    /**
     * Sets the current mouse position as the navigator bar center position, if
     * panning is allowed and panning is currently active
     * <p>
     * @param position
     */
    private void setPanPosition( int position ) {
        if( isPanning && canPan ) {
            int logi = transformToLogicalCoordForPannig( position );
            //       Logger.getLogger(this.getClass().getName()).log(Level.INFO, "pos "+position+" logi "+logi);
            boundsManager.navigatorBarUpdated( logi );
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
    public void setIsPanModeOn( boolean canPan ) {
        this.canPan = canPan;
    }


    /**
     * @return <code>true</code> if this viewer is allowed to zoom via the mouse
     *         wheel, <code>false</code> otherwise.
     */
    public boolean isCanZoom() {
        return canZoom;
    }


    /**
     * @param canZoom <code>true</code> if this viewer is allowed to zoom via
     *                the mouse wheel, <code>false</code> otherwise.
     */
    public void setCanZoom( boolean canZoom ) {
        this.canZoom = canZoom;
    }


    /**
     * Updates the tool tip text for the given logPos = reference position.
     * <p>
     * @param logPos The reference position for which the tool tip shall be
     *               shown
     */
    public abstract void changeToolTipText( int logPos );


    /**
     * Compute the space that is currently assigned for one base of the genome.
     */
    private void calcBaseWidth() {
        if( pAInfoIsAvailable ) {
            basewidth = (double) paintingAreaInfo.getPhyWidth() / bounds.getLogWidth();
        }
    }


    /**
     * @param logPos position in the reference genome
     * <p>
     * @return the physical boundaries (left, right) of a single base of the
     *         sequence
     *         in the viewer.
     */
    public PhysicalBaseBounds getPhysBoundariesForLogPos( int logPos ) {
        double left = this.transformToPhysicalCoord( logPos );
        double right = left + basewidth - 1;
        return new PhysicalBaseBounds( left, right );
    }


    /**
     * Compute the horizontal position (pixel) for a logical position (base in
     * genome). If a base has more space than one pixel, this method returns the
     * leftmost pixel, meaning the beginning of the available interval for
     * displaying this base.
     * <p>
     * @param logPos a position in the genome
     * <p>
     * @return horizontal position for requested base position.
     */
    protected double transformToPhysicalCoord( int logPos ) {
        double tmp = (logPos - bounds.getLogLeft()) * correlationFactor + horizontalMargin;
        return tmp;
    }


    /**
     * Converts the physical pixel position into the logical sequence position
     * for a given phyPos.
     * <p>
     * @param phyPos physical position (pixel) in the reference genome
     * <p>
     * @return the logical position of a single base in the sequence.
     */
    public int getLogicalPosForPixel( int phyPos ) {
        return this.transformToLogicalCoord( phyPos );
    }


    /**
     * Compute the logical position for any given physical position (pixel).
     * <p>
     * @param physPos horizontal position of a pixel
     * <p>
     * @return logical position corresponding to the pixel
     */
    protected int transformToLogicalCoord( int physPos ) {
        //       Logger.getLogger(this.getClass().getName()).log(Level.INFO, "boundsLeft "+ bounds.getLogLeft()+"right"+ bounds.getLogRight());
        return (int) (((double) physPos - horizontalMargin) / correlationFactor + bounds.getLogLeft());
    }


    /**
     * Compute the logical position for any given physical position
     * <p>
     * @param physPos horizontal position of a pixel
     * <p>
     * @return logical position corresponding to the pixel
     */
    protected int transformToLogicalCoordForPannig( int physPos ) {

        int pos;
        int currentLog = bounds.getCurrentLogPos();
        int leftbound = bounds.getLogLeft();
        int rightBound = bounds.getLogRight();
        pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + bounds.getLogLeft());
        int lb = leftbound - ((rightBound - leftbound) / 2);
        //we want to go to smaller positions
        if( lastPhysPos > physPos ) {
            lastPhysPos = physPos;
            //mouse on the right side of currentLog
            pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + lb);
        }
        else {
            lastPhysPos = physPos;
            //mouse on the right side of currentLog
            if( currentLog < pos ) {
                pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + leftbound);
                // Logger.getLogger(this.getClass().getName()).log(Level.INFO, "rightside plus "+pos);
            }
            else {
                pos = (int) (((double) physPos - horizontalMargin) / correlationFactor + rightBound);
                //     Logger.getLogger(this.getClass().getName()).log(Level.INFO, "leftside plus "+pos);
            }

        }
        if( pos <= 0 ) {
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
        if( pAInfoIsAvailable ) {
            correlationFactor = (double) paintingAreaInfo.getPhyWidth() / bounds.getLogWidth();
        }
    }


    /**
     * Update the physical coordinates of this panel, available width for
     * painting.
     * Method is called automatically, when this panel resizes
     */
    public void updatePhysicalBounds() {
        this.setSizes();
        this.adjustPaintingAreaInfo();
        this.boundsManager.getUpdatedBoundsInfo( this );
    }


    /**
     * Assign new logical bounds to this panel, meaning the range from the
     * sequence that should be displayed. In case the abstract viewer was handed
     * over a scrollbar the value of the scrollbar is adjusted to the middle.
     * <p>
     * @param bounds Information about the interval that should be displayed and
     *               the current position
     */
    @Override
    public void updateLogicalBounds( BoundsInfo bounds ) {
        if( this.isActive() && !this.bounds.equals( bounds ) ) {
            this.bounds = bounds;
            this.calcBaseWidth();
            this.recalcCorrelationFactor();

            if( this.basewidth > 7 ) {
                this.setIsInMaxZoomLevel( true );
            }
            else {
                this.setIsInMaxZoomLevel( false );
            }
            if( this.seqBar != null ) {
                this.seqBar.boundsChanged();
            }
            this.boundsChangedHook();
            this.repaint();
        }

        if( this.scrollPane != null && this.centerScrollBar ) {
            try {
                JScrollBar verticalBar = this.scrollPane.getVerticalScrollBar();
                verticalBar.setValue( verticalBar.getMaximum() / 2 - this.getParent().getHeight() / 2 );
            }
            catch( ArrayIndexOutOfBoundsException e ) {
                //ignore this problem, TODO: identify source of ArrayIndexOutOfBoundsException
            }
        }
    }


    /**
     * Calling this method adds an AdjustmentListener to to the JScrollPane, in
     * which this AbstractViewer is placed. This listener updates the vertical
     * legend and option label and panel positions. If the viewer is not placed
     * in a JScrollPane, nothing is done.
     */
    public void createListenerForScrollBar() {
        if( this.scrollPane != null ) {
            this.scrollPane.getVerticalScrollBar().addAdjustmentListener( new AdjustmentListener() {

                @Override
                public void adjustmentValueChanged( AdjustmentEvent e ) {
                    Rectangle visibleRect = scrollPane.getViewport().getViewRect();
                    if( hasLegend() ) {
                        getLegendLabel().setLocation( visibleRect.x + LEGEND_X, visibleRect.y );
                        getLegendPanel().setLocation( visibleRect.x + LEGEND_X, visibleRect.y + getLegendLabel().getSize().height + 2 );
                    }
                    if( hasOptions() ) {
                        getOptionsLabel().setLocation( visibleRect.x + OPTIONS_X, visibleRect.y );
                        getOptionsPanel().setLocation( visibleRect.x + OPTIONS_X, visibleRect.y + getOptionsLabel().getSize().height + 2 );
                    }
                    if( hasChromSelectionPanel() ) {
                        getChromSelectionPanel().setLocation( visibleRect.x + OPTIONS_X, visibleRect.y );
                    }
                }


            } );
        }
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void setCurrentMousePosition( int newPos ) {
        oldLogMousePos = currentLogMousePos;
        currentLogMousePos = newPos;
        if( oldLogMousePos != currentLogMousePos ) {
            this.repaintMousePosition( oldLogMousePos, currentLogMousePos );
            this.firePropertyChange( PROP_MOUSEPOSITION_CHANGED, oldLogMousePos, currentLogMousePos );
        }

        if( newPos >= this.getBoundsInfo().getLogLeft() && newPos <= this.getBoundsInfo().getLogRight() ) {
            this.changeToolTipText( newPos );
        }
        else {
            this.setToolTipText( null );
        }
    }


    /**
     * Repaints the mouse position rectangle.
     * <p>
     * @param oldPos the old mouse position
     * @param newPos the new mouse position
     */
    private void repaintMousePosition( int oldPos, int newPos ) {
        if( isInDrawingMode() ) {
            PhysicalBaseBounds mouseAreaOld = getPhysBoundariesForLogPos( oldPos );
            PhysicalBaseBounds mouseAreaNew = getPhysBoundariesForLogPos( newPos );

            int min;
            int max;
            if( oldPos >= newPos ) {
                min = (int) mouseAreaNew.getLeftPhysBound();
                max = (int) mouseAreaOld.getLeftPhysBound() + getWidthOfMouseOverlay( oldPos );
            }
            else {
                min = (int) mouseAreaOld.getLeftPhysBound();
                max = (int) mouseAreaNew.getLeftPhysBound() + getWidthOfMouseOverlay( newPos );
            }
            min--;
            max++;
            int width = max - min + 1;

            repaint( min, 0, width, this.getHeight() - 1 );
        }
    }


    /**
     * Paints the rectangle marking the mouse position. It covers the whole
     * height of the viewer.
     * <p>
     * @param g the grapics object to paint in
     */
    private void drawMouseCursor( Graphics g ) {
        int currentLogPos = getCurrentMousePos();
        if( getBoundsInfo().getLogLeft() <= currentLogPos && currentLogPos <= getBoundsInfo().getLogRight() ) {
            PhysicalBaseBounds mouseArea = this.getPhysBoundariesForLogPos( currentLogPos );
            int width = getWidthOfMouseOverlay( currentLogPos );
            PaintingAreaInfo info = this.getPaintingAreaInfo();
            g.drawRect( (int) mouseArea.getLeftPhysBound(), info.getForwardHigh(), width - 1, info.getCompleteHeight() - 1 );
        }
    }


    /**
     * Paints the grey rectangle behind the current center position of the
     * viewer.
     * <p>
     * @param g The graphics to paint on
     */
    private void paintCurrentCenterPosition( Graphics g ) {
        PhysicalBaseBounds coords = getPhysBoundariesForLogPos( getBoundsInfo().getCurrentLogPos() );
        PaintingAreaInfo info = this.getPaintingAreaInfo();
        g.setColor( ColorProperties.CURRENT_POSITION );
        int width = (int) (coords.getPhysWidth() >= 1 ? coords.getPhysWidth() : 1);
        g.fillRect( (int) coords.getLeftPhysBound(), info.getForwardHigh(), width, info.getCompleteHeight() );
    }


    protected int getWidthOfMouseOverlay( int position ) {
        PhysicalBaseBounds mouseArea = getPhysBoundariesForLogPos( position );
        return (int) (mouseArea.getPhysWidth() >= 3 ? mouseArea.getPhysWidth() : 3);
    }


    /**
     * {@inheritDoc }
     */
    @Override
    protected void paintComponent( Graphics graphics ) {
        super.paintComponent( graphics );

        if( isInDrawingMode() ) {
            graphics.setColor( ColorProperties.MOUSEOVER );
            if( printMouseOver ) {
                drawMouseCursor( graphics );
            }
            paintCurrentCenterPosition( graphics );
        }
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void setMouseOverPaintingRequested( boolean requested ) {
        // repaint whole viewer if mouse curser was painted before, but none is not wanted
        if( printMouseOver && !requested ) {
            repaint();
        }
        printMouseOver = requested;
        if( !printMouseOver ) {
            currentLogMousePos = 0;
        }
        firePropertyChange( PROP_MOUSEOVER_REQUESTED, null, requested );
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
     * <p>
     * @return the current bounds values
     */
    public BoundsInfo getBoundsInfo() {
        return this.bounds;
    }


    /**
     * @return The size of the area, that is used for drawing. Logical bounds
     *         depend on the available size of each listener.
     */
    @Override
    public Dimension getPaintingAreaDimension() {
        return pAInfoIsAvailable ? new Dimension( paintingAreaInfo.getPhyWidth(), paintingAreaInfo.getCompleteHeight() ) : null;

    }


    /**
     * @return true, if the PaintingArea has coordinates to calculate bounds,
     *         false otherwise.
     */
    @Override
    public boolean isPaintingAreaAvailable() {
        return pAInfoIsAvailable;
    }


    /**
     * @return The painting area info for this viewer. It contains useful info
     *         about heights, widths and left and right end positions.
     */
    public PaintingAreaInfo getPaintingAreaInfo() {
        return paintingAreaInfo;
    }


    /**
     * @return The hovered reference positions.
     */
    public int getCurrentMousePos() {
        return currentLogMousePos;
    }


    public void forwardChildrensMousePosition( int relPhyPos, JComponent child ) {
        int phyPos = child.getX() + relPhyPos;
        int logPos = transformToLogicalCoord( phyPos );

        basePanel.reportMouseOverPaintingStatus( true );
        basePanel.reportCurrentMousePos( logPos );
    }


    public boolean isInMaxZoomLevel() {
        return isInMaxZoomLevel;
    }


    public boolean isInDrawingMode() {
        return inDrawingMode;
    }


    private void setIsInMaxZoomLevel( boolean isInMaxZoomLevel ) {
        this.isInMaxZoomLevel = isInMaxZoomLevel;

    }


    public void setInDrawingMode( boolean inDrawingMode ) {
        this.inDrawingMode = inDrawingMode;
    }


    /**
     * @return The reference genome associated with this ReferenceViewer
     *         instance.
     */
    public PersistentReference getReference() {
        return this.reference;
    }


    /**
     * @return true, if this viewer is currently active (in the foreground)
     *         and false, if it is inactive
     */
    public boolean isActive() {
        return this.isActive;
    }


    /**
     * Set true, if this viewer should be active (in the foreground or it needs
     * to update its data) and false, if it should be inactive.
     * <p>
     * @param isActive true, if this viewer should be active and false, if not
     */
    public void setActive( boolean isActive ) {
        this.isActive = isActive;
        if( isActive ) {
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


    public MenuLabel getOptionsLabel() {
        return this.optionsLabel;
    }


    public boolean hasOptions() {
        return this.hasOptions;
    }


    public JPanel getOptionsPanel() {
        return this.options;
    }


    /**
     * @return true, if this viewer has a chromosome selection panel, false
     *         otherwise.
     */
    public boolean hasChromSelectionPanel() {
        return hasChromSelection;
    }


    /**
     * @return the chromosome selection panel of this viewer. Might be null.
     */
    public JPanel getChromSelectionPanel() {
        return this.chromSelectionPanel;
    }


    /**
     * @return The list of classification types, which are currently
     *         excluded from the view/calculations by the user.
     */
    public List<Classification> getExcludedClassifications() {
        return this.excludedClassifications;
    }


    /**
     * @return The minimum mapping quality for data queries. If at least one
     *         mapping does not contain a mapping quality, this filter is not used!
     */
    public byte getMinMappingQuality() {
        return this.minMappingQuality;
    }


    /**
     * @param minMappingQuality Sets this value as the minimum mapping quality
     *                          to use for data queries. If at least one mapping does not contain a
     *                          mapping quality, this filter is not used!
     */
    public void setMinMappingQuality( byte minMappingQuality ) {
        this.minMappingQuality = minMappingQuality;
    }


    /**
     * @return Queries the excluded feature type list for the read class
     *         parameter selection and converts them to a ParametersReadClasses object.
     */
    public ParametersReadClasses getReadClassParams() {
        return new ParametersReadClasses( this.getExcludedClassifications(), this.getMinMappingQuality() );
    }


    public boolean isMouseOverPaintingRequested() {
        return printMouseOver;
    }


    public void setHorizontalMargin( int horizontalMargin ) {
        this.horizontalMargin = horizontalMargin;
        this.adjustPaintingAreaInfo();
    }


    public int getHorizontalMargin() {
        return this.horizontalMargin;
    }


    public void setVerticalMargin( int verticalMargin ) {
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
     * A scrollpane should be handed over, in case the scrollpane should adapt
     * its vertical value to the middle position, whenever the genome position
     * was updated.
     * <p>
     * @param scrollPane the scrollpane which should adapt
     */
    public void setScrollPane( JScrollPane scrollPane ) {
        this.scrollPane = scrollPane;
    }


    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }


    /**
     * Sets the property for centering the scrollbar around the center (sequence
     * bar) (true) or not (false).
     * <p>
     * @param centerScrollBar true, if the scrollbar should center around the
     *                        sequence bar, false otherwise
     */
    public void setAutomaticCentering( boolean centerScrollBar ) {
        this.centerScrollBar = centerScrollBar;
    }


    /**
     * @return The image to display, if the viewer waits for something.
     */
    public BufferedImage getLoadingIndicator() {
        return loadingIndicator;
    }


    /**
     * @return true, if the data to display shall be requested again, false
     *         otherwise
     */
    public boolean isNewDataRequestNeeded() {
        return newDataRequestNeeded;
    }


    /**
     * @param newDataRequestNeeded true, if the data to display shall be
     *                             requested again, false otherwise
     */
    public void setNewDataRequestNeeded( boolean newDataRequestNeeded ) {
        this.newDataRequestNeeded = newDataRequestNeeded;
    }


}
