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

package de.cebitec.readxplorer.ui.datavisualisation.abstractviewer;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfo;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.GenomeGapManager;
import de.cebitec.readxplorer.ui.datavisualisation.HighlightAreaListener;
import de.cebitec.readxplorer.ui.datavisualisation.HighlightableI;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.dialogmenus.MenuItemFactory;
import de.cebitec.readxplorer.utils.ColorProperties;
import de.cebitec.readxplorer.utils.PositionUtils;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.sequence.Region;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;


/**
 * A sequence bar is used to display the sequence of a reference genome within
 * another AbstractViewer. Further, it contains several options for highlighting
 * areas, start or stop codons and patterns.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class SequenceBar extends JComponent implements HighlightableI {

    private static final long serialVersionUID = 23446398;
    private final int height = 50;
    private final AbstractViewer parentViewer;
    private final PersistentReference refGen;
    private final Font font;
    private FontMetrics metrics;
    private boolean printSeq;
    private final int baseLineY;
    private final int offsetY;
    private Rectangle highlightRect;
    private GenomeGapManager gapManager;
    // the width in bases (logical positions), that is used for marking
    // a value of 100 means every 100th base is marked by a large and every 50th
    // base is marked by a small bar
    private int markingWidth;
    private int halfMarkingWidth;
    private final int largeBar;
    private final int smallBar;
    private HighlightAreaListener highlightListener;
    private final RegionManager regionManager;
    private byte frameCurrFeature;


    /**
     * A sequence bar is used to display the sequence of a reference genome
     * within another AbstractViewer. Further, it contains several options for
     * highlighting areas, start or stop codons and patterns.
     * <p>
     * @param parentViewer the viewer containing the sequence bar
     */
    public SequenceBar( AbstractViewer parentViewer ) {
        super();
        this.parentViewer = parentViewer;
        this.setSize( new Dimension( 0, this.height ) );
        this.font = new Font( Font.MONOSPACED, Font.PLAIN, 10 );
        this.refGen = parentViewer.getReference();
        this.baseLineY = 30;
        this.offsetY = 10;
        this.largeBar = 11;
        this.smallBar = 7;
        this.markingWidth = 10;
        this.halfMarkingWidth = markingWidth / 2;
        this.initMouseListener(); //this order has to be obeyed, otherwise the highlight listener
        this.initHighlightListener(); //will not be shown in the highlighted area!
        this.regionManager = new RegionManager( this, parentViewer, refGen, highlightListener );
    }


    /**
     * Adds a mouse listener to this sequence bar, which allows selecting the
     * sequence, currently displayed on the screen.
     */
    private void initHighlightListener() {
        highlightListener = new HighlightAreaListener( this, baseLineY, offsetY );
        this.addMouseListener( highlightListener );
        this.addMouseMotionListener( highlightListener );
    }


    private void initMouseListener() {
        this.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getButton() == MouseEvent.BUTTON3 ) {
                    JPopupMenu popUp = new JPopupMenu();
                    MenuItemFactory menuItemFactory = new MenuItemFactory();

                    //add copy mouse position option
                    popUp.add( menuItemFactory.getCopyPositionItem( parentViewer.getCurrentMousePos() ) );
                    //add center current position option
                    popUp.add( menuItemFactory.getJumpToPosItem( parentViewer.getBoundsInformationManager(), parentViewer.getCurrentMousePos() ) );
                    popUp.show( e.getComponent(), e.getX(), e.getY() );
                }
            }


            @Override
            public void mousePressed( MouseEvent e ) {
            }


            @Override
            public void mouseReleased( MouseEvent e ) {
            }


            @Override
            public void mouseEntered( MouseEvent e ) {
            }


            @Override
            public void mouseExited( MouseEvent e ) {
            }


        } );
    }


    public void setGenomeGapManager( GenomeGapManager gapManager ) {
        this.gapManager = gapManager;
    }


    /**
     * Should be called, when the bounds have been changed. Updates the content
     * of the sequence bar.
     */
    public void boundsChanged() {
        this.adjustMarkingInterval();
        this.regionManager.findCodons();
        this.regionManager.findPattern();
        this.regionManager.showCdsRegions();
        this.highlightListener.boundsChangedHook();
    }


    @Override
    protected void paintComponent( Graphics graphics ) {
        Graphics2D g = (Graphics2D) graphics;
        BoundsInfo bounds = parentViewer.getBoundsInfo();
        PaintingAreaInfo info = parentViewer.getPaintingAreaInfo();

        g.setColor( ColorProperties.TRACKPANEL_MIDDLE_LINE );
        this.drawSequence( g );
        // draw a line indicating the sequence
        g.draw( new Line2D.Double( info.getPhyLeft(), baseLineY, info.getPhyRight(), baseLineY ) );

        // draw markings to indicate current parentViewerposition
        int temp = bounds.getLogLeft();
        temp += (halfMarkingWidth - temp % halfMarkingWidth);

        int logright = bounds.getLogRight();
        while( temp <= logright ) {
            if( temp % markingWidth == 0 ) {
                this.drawThickLine( g, temp );
            } else {
                this.drawThinLine( g, temp );
            }
            temp += halfMarkingWidth;
        }

        //paint the hightlight rectangle if there is currently one
        if( this.highlightRect != null ) {
            g.setColor( ColorProperties.HIGHLIGHT_BORDER );
            g.draw( this.highlightRect );
            g.setColor( ColorProperties.HIGHLIGHT_FILL );
            g.fill( this.highlightRect );
        }
    }


    /**
     * Draw sequence, if current zoom allows it.
     * <p>
     * @param g Graphics2D object to print on
     */
    private void drawSequence( Graphics2D g ) {

        // print sequence if sufficient space
        if( printSeq ) {

            // get the font metrics
            g.setFont( font );
            metrics = g.getFontMetrics( font );

            BoundsInfo bounds = parentViewer.getBoundsInfo();
            int logleft = bounds.getLogLeft();
            if( logleft < 1 ) { //might happen for very short reference sequences
                logleft = 1;
            }
            int logright = bounds.getLogRight();
            String currentChromSeq = refGen.getActiveChromSequence( logleft, logright );

            for( int i = logleft; i <= logright; i++ ) {
                this.drawChar( g, i, currentChromSeq );
                this.drawCharReverse( g, i, currentChromSeq );
            }
        }
    }


    /**
     * Draw base of the sequence.
     * <p>
     * @param g        Graphics2D object to paint on
     * @param pos      position of the base in the reference genome starting
     *                 with 1 (not 0!). To get the correct base 1 is substracted
     *                 from pos within this method.
     * @param chromSeq complete chromosome sequence
     */
    private void drawChar( Graphics2D g, int pos, String chromSeq ) {
        // pos depents on slider value and cannot be smaller 1
        // since counting in strings starts with 0, we have to substract 1
        int basePosition = pos - parentViewer.getBoundsInfo().getLogLeft();

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( pos );
        double physX = bounds.getPhyMiddle();
        if( gapManager != null && gapManager.hasGapAt( pos ) ) {
            int numOfGaps = gapManager.getNumOfGapsAt( pos );
            for( int i = 0; i < numOfGaps; i++ ) {
                int tmp = (int) (physX + i * bounds.getPhysWidth());
                String base = "-";
                int offset = metrics.stringWidth( base ) / 2;
                g.drawString( base, (float) tmp - offset, (float) baseLineY - offsetY );
            }
            physX += numOfGaps * bounds.getPhysWidth();
        }
        String base = chromSeq.substring( basePosition, basePosition + 1 );
        int offset = metrics.stringWidth( base ) / 2;
        /* BaseBackground b = new BaseBackground(12,5, base);
         * b.setBounds((int)physX-offset,baseLineY-10,b.getSize().width,
         * b.getSize().height);
         this.add(b); */
        g.drawString( base, (float) physX - offset, (float) baseLineY - offsetY );
    }


    /**
     * draws the a character of the reverse strand of the sequence.
     * <p>
     * @param g        the graphics object to paint on
     * @param pos      position of the base in the reference genome
     * @param chromSeq complete chromosome sequence
     */
    private void drawCharReverse( Graphics2D g, int pos, String chromSeq ) {
        // logX depents on slider value and cannot be smaller 1
        // since counting in strings starts with 0, we have to substract 1
        int basePosition = pos - parentViewer.getBoundsInfo().getLogLeft();

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( pos );
        double physX = bounds.getPhyMiddle();
        if( gapManager != null && gapManager.hasGapAt( pos ) ) {
            int numOfGaps = gapManager.getNumOfGapsAt( pos );
            for( int i = 0; i < numOfGaps; i++ ) {
                int tmp = (int) (physX + i * bounds.getPhysWidth());
                String base = "-";
                int offset = metrics.stringWidth( base ) / 2;
                g.drawString( base,
                              (float) tmp - offset,
                              (float) baseLineY + offsetY );
            }
            physX += numOfGaps * bounds.getPhysWidth();
        }
        String base = chromSeq.substring( basePosition, basePosition + 1 );
        String revBase = SequenceUtils.getDnaComplement( base );
        int offset = metrics.stringWidth( revBase ) / 2;
        g.drawString( revBase,
                      (float) physX - offset,
                      (float) baseLineY + offsetY );
    }


    /**
     * draw a thick vertical line with length largeBar
     * <p>
     * @param g      Graphics2D object to paint on
     * @param logPos logical position, that should be marked
     */
    private void drawThickLine( Graphics2D g, int logPos ) {
        // draw a line and the label (position) in the middle of the space for this base
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( logPos );
        double physX = bounds.getPhyMiddle();
        if( gapManager != null && gapManager.hasGapAt( logPos ) ) {
            physX += gapManager.getNumOfGapsAt( logPos ) * bounds.getPhysWidth();
        }
        g.draw(
                new Line2D.Double(
                        physX, baseLineY - largeBar / 2, physX, baseLineY + largeBar / 2 ) );

        String label = getRulerLabel( logPos );

        int offset = metrics.stringWidth( label ) / 2;
        g.drawString( label, (float) physX - offset, (float) baseLineY + 2 * offsetY );
    }


    /**
     * Return the label for a marking position
     * <p>
     * @param logPos the position that is intended to be marked
     * <p>
     * @return the label used at that mark. 4000 is abbreviated by 4k, for
     *         example.
     */
    private String getRulerLabel( int logPos ) {
        String label = null;
        if( logPos >= 1000 && markingWidth >= 1000 ) {
            if( logPos % 1000 == 0 ) {
                label = String.valueOf( logPos / 1000 );
            } else if( logPos % 500 == 0 ) {
                label = String.valueOf( logPos / 1000 );
                label += ".5";
            }
            label += "K";

        } else {
            label = String.valueOf( logPos );
        }

        return label;
    }


    /**
     * draw a thin vertical line with length smallBar
     * <p>
     * @param g      Graphics2D object to paint on
     * @param logPos logical position, that should be marked
     */
    private void drawThinLine( Graphics2D g, int logPos ) {
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( logPos );
        double physX = bounds.getPhyMiddle();
        if( gapManager != null && gapManager.hasGapAt( logPos ) ) {
            physX += gapManager.getNumOfGapsAt( logPos ) * bounds.getPhysWidth();
        }

        g.draw( new Line2D.Double(
                physX, baseLineY - smallBar / 2, physX, baseLineY + smallBar / 2 ) );
    }


    /**
     * Adjust the width that is used for marking bases to the current size
     */
    private void adjustMarkingInterval() {
        printSeq = parentViewer.isInMaxZoomLevel();

        // asume 50 px for label and leave a gap of 30 px to next label
        int labelWidth = 50;
        labelWidth += 30;

        // pixels available per base
        double pxPerBp = (double) parentViewer.getPaintingAreaInfo().getPhyWidth() / parentViewer.getBoundsInfo().getLogWidth();

        if( 10 * pxPerBp > labelWidth ) {
            markingWidth = 10;
        } else if( 20 * pxPerBp > labelWidth ) {
            markingWidth = 20;
        } else if( 50 * pxPerBp > labelWidth ) {
            markingWidth = 50;
        } else if( 100 * pxPerBp > labelWidth ) {
            markingWidth = 100;
        } else if( 250 * pxPerBp > labelWidth ) {
            markingWidth = 250;
        } else if( 500 * pxPerBp > labelWidth ) {
            markingWidth = 500;
        } else if( 1000 * pxPerBp > labelWidth ) {
            markingWidth = 1000;
        } else if( 5000 * pxPerBp > labelWidth ) {
            markingWidth = 5000;
        } else if( 10000 * pxPerBp > labelWidth ) {
            markingWidth = 10000;
        }

        halfMarkingWidth = markingWidth / 2;
    }


    /**
     * Determines the frame of the currently selected feature. if there is none
     * it is set to 10.
     * <p>
     * @return the correct reading frame (-3 to 3 excluding 0)
     */
    public byte determineFeatureFrame() {
        this.frameCurrFeature = StartCodonFilter.INIT;//if it is 10 later, no selected feature exists yet!
        if( this.parentViewer instanceof ReferenceViewer ) {
            ReferenceViewer refViewer = (ReferenceViewer) this.parentViewer;
            if( refViewer.getCurrentlySelectedFeature() != null ) {
                frameCurrFeature = (byte) PositionUtils.determineFrame( refViewer.getCurrentlySelectedFeature().getPersistentFeature() );
            }
        }
        return frameCurrFeature;
    }


    /**
     * Transforms a region object into a JRegion object for visualization in
     * this sequence bar.
     * <p>
     * @param region the region object to transform for this sequence bar
     * <p>
     * @return the corresponding JRegion object for visualization in this
     *         sequence bar
     */
    public JRegion transformRegionToJRegion( Region region ) {
        BoundsInfo bounds = this.parentViewer.getBoundsInfo();
        int from = this.getStart( bounds, region );
        int to = this.getStop( bounds, region );

        int length = to - from + 1;
        // make sure it is visible when using high zoom levels
        if( length < 3 ) {
            length = 3;
        }
        JRegion jreg = new JRegion( length, 10, region.getType(), region.getStart(), region.getStop() );
        if( region.isFwdStrand() ) {
            jreg.setBounds( from, baseLineY - jreg.getSize().height - 6, jreg.getSize().width, jreg.getSize().height );
        } else {
            jreg.setBounds( from, baseLineY + 4, jreg.getSize().width, jreg.getSize().height );
        }

        return jreg;
    }


    /**
     * Calculates the position of the first pixel of the region handed over to
     * the method. Gaps do not play a role here, because they are not extended
     * to the left.
     * <p>
     * @param bounds the bounds info object of the context of the region
     * @param r      the region, whose start is to be calculated
     * <p>
     * @return the correct position of the first pixel of the region handed over
     *         to the method.
     */
    private int getStart( BoundsInfo bounds, Region r ) {
        int start = r.getStart();
        if( start < bounds.getLogLeft() ) {
            start = bounds.getLogLeft();
        }
        return (int) parentViewer.getPhysBoundariesForLogPos( start ).getLeftPhysBound();
    }


    /**
     * Calculates the position of the last pixel of the region handed over to
     * the method. This includes gaps that might occur in the reference.
     * <p>
     * @param bounds the bounds info object of the context of the region
     * @param r      the region, whose end is to be calculated
     * <p>
     * @return the correct position of the last pixel of the region handed over
     *         to the method.
     */
    private int getStop( BoundsInfo bounds, Region r ) {
        int stop = r.getStop();
        if( stop > bounds.getLogRight() ) {
            stop = bounds.getLogRight();
        }
        PhysicalBaseBounds stopBounds = parentViewer.getPhysBoundariesForLogPos( stop );
        int to = (int) stopBounds.getRightPhysBound();

        if( gapManager != null && gapManager.hasGapAt( stop ) ) {
            to = (int) (gapManager.getNumOfGapsAt( stop ) * stopBounds.getPhysWidth());
        }
        return to;
    }


    /**
     * Calculates which start codons should be highlighted and updates the gui.
     * <p>
     * @param i          the index of the codon to update
     * @param isSelected true, if the codon should be selected
     */
    public void showStartCodons( final int i, final boolean isSelected ) {
        this.regionManager.showStartCodons( i, isSelected );
    }


    /**
     * Calculates which stop codons should be highlighted and updates the gui.
     * <p>
     * @param i          the index of the codon to update
     * @param isSelected true, if the codon should be selected
     */
    public void showStopCodons( final int i, final boolean isSelected ) {
        this.regionManager.showStopCodons( i, isSelected );
    }


    /**
     * Returns if the codon with the index i is currently selected.
     * <p>
     * @param i the index of the codon
     * <p>
     * @return true, if the codon with the index i is currently selected
     */
    public boolean isStartCodonShown( final int i ) {
        return this.regionManager.isStartCodonShown( i );
    }


    /**
     * Detects the occurrences of the given pattern in the currently shown
     * interval or the next occurrence of the pattern in the genome.
     * <p>
     * @param pattern Pattern to search for
     * <p>
     * @return the next (closest) occurrence of the pattern
     */
    public int showPattern( String pattern ) {
        return this.regionManager.showPattern( pattern );
    }


    /**
     * Identifies the codons according to the currently selected codons to show
     * and adds JRegions for highlighting into the sequence bar.
     */
    public void findCodons() {
        this.regionManager.findCodons();
    }


    /**
     * Identifies the currently in this object stored pattern in the genome
     * sequence.
     */
    public void findPattern() {
        this.regionManager.findPattern();
    }


    /**
     * Identifies next (closest) occurrence from either forward or reverse
     * strand of a pattern in the current reference genome.
     * <p>
     * @return the position of the next occurrence of the pattern
     */
    public int findNextPatternOccurrence() {
        return this.regionManager.findNextPatternOccurrence();
    }


    /**
     * @return The frame of the current feature
     */
    public byte getFrameCurrFeature() {
        return this.frameCurrFeature;
    }


    /**
     * Paints the background of each base with a base specific color. Before
     * calling this method make sure to call "removeAll" on this sequence bar!
     * Otherwise the colors accumulate.
     * <p>
     * @param basePosition Position of the current base in the genome
     */
    public void paintBaseBackgroundColor( int basePosition ) {
        if( basePosition < 1 ) {
            basePosition = 1;
        }
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( basePosition );
        if( bounds != null ) {
            double physX = bounds.getPhyMiddle();
            if( gapManager != null && gapManager.hasGapAt( basePosition ) ) {
                int numOfGaps = gapManager.getNumOfGapsAt( basePosition );
                physX += numOfGaps * bounds.getPhysWidth();
            }
            String base = refGen.getActiveChromSequence( basePosition, basePosition );

            if( base != null && metrics != null ) {
                int offset = metrics.stringWidth( base ) / 2;
                BaseBackground b = new BaseBackground( 12, 12, base );
                b.setBounds( (int) physX - offset, baseLineY - 18, b.getSize().width, b.getSize().height );
                this.add( b );
                BaseBackground brev = new BaseBackground( 12, 12, SequenceUtils.getDnaComplement( base ) );
                brev.setBounds( (int) physX - offset, baseLineY + 2, b.getSize().width, b.getSize().height );
                this.add( brev );
            }
        }
    }


    /**
     * Sets the rectangle used for highlighting something in this sequence bar.
     * <p>
     * @param rect the rectangle to set
     */
    @Override
    public void setHighlightRectangle( final Rectangle rect ) {
        this.highlightRect = rect;
        this.repaint();
    }


    /**
     * Returns the persistent reference used for this sequence bar.
     * <p>
     * @return the persistent reference used for this sequence bar
     */
    public PersistentReference getPersistentReference() {
        return this.refGen;
    }


    /**
     * @return The bounds info of the parent viewer
     */
    public BoundsInfo getViewerBoundsInfo() {
        return this.parentViewer.getBoundsInfo();
    }


    /**
     * @return the base width defined in the parent viewer.
     */
    public double getBaseWidth() {
        return this.parentViewer.getBaseWidth();
    }


    /**
     * @return the horizontal margin of the parent viewer.
     */
    public int getViewerHorizontalMargin() {
        return this.parentViewer.getHorizontalMargin();
    }


    public int getCurrentMousePosition() {
        return this.parentViewer.getCurrentMousePos();
    }


    /**
     * @param pixelPos physical position (pixel) in the sequence bar sequence
     * <p>
     * @return the physical pixel position converted into the logical sequence
     *         position.
     */
    public int getLogicalPosForPixel( int pixelPos ) {
        return parentViewer.getLogicalPosForPixel( pixelPos );
    }


    /**
     * @return the y baseline of this sequence bar.
     */
    public int getBaseLineY() {
        return this.baseLineY;
    }


    /**
     * @return The bounds information manager of the parent abstract viewer.
     */
    public BoundsInfoManager getBoundsInfoManager() {
        return this.parentViewer.getBoundsInformationManager();
    }


    /**
     * This method is to be called, when a mouse listener associated to this
     * component registered a mouse moved event.
     * <p>
     * @param e the mouse event which triggered this call
     */
    public void updateMouseListeners( MouseEvent e ) {
        for( MouseMotionListener mml : this.parentViewer.getMouseMotionListeners() ) {
            mml.mouseMoved( e );
            this.setToolTipText( this.parentViewer.getToolTipText() );
        }
    }


    /**
     * Removes all JRegions from this component of all the given types. Removed
     * by Properties.START, Properties.STOP, Properties.PATTERN, Properties.CDS
     * and Properties.ALL
     * <p>
     * @param typeList list of types of components to remove
     */
    protected void removeAll( List<Byte> typeList ) {
        for( Component comp : this.getComponents() ) {
            for( int type : typeList ) {
                if( comp instanceof JRegion && ((JRegion) comp).getType() == type ) {
                    this.remove( comp );
                    break;
                }
            }
        }
    }


    /**
     * Removes all JRegions from this component of a given type. Removed by
     * Properties.START, Properties.STOP, Properties.PATTERN, Properties.CDS and
     * Properties.ALL
     * <p>
     * @param type the type of components to remove
     */
    protected void removeAll( Byte type ) {
        List<Byte> typeList = new ArrayList<>();
        typeList.add( type );
        this.removeAll( typeList );
    }


    /**
     * Sets a list of cds regions for the sequence bar and replaces the list
     * stored in this variable until now.
     * <p>
     * @param cdsRegions the cdsRegions to set
     */
    public void setCdsRegions( List<Region> cdsRegions ) {
        this.regionManager.setCdsRegions( cdsRegions );
    }


}
