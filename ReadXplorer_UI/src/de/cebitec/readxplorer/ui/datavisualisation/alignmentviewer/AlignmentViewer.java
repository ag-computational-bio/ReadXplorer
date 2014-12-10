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

package de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer;


import de.cebitec.readxplorer.databackend.IntervalRequest;
import de.cebitec.readxplorer.databackend.ThreadListener;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataObjects.Mapping;
import de.cebitec.readxplorer.databackend.dataObjects.MappingResult;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.utils.ColorProperties;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.PaintingAreaInfo;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.PhysicalBaseBounds;
import de.cebitec.readxplorer.ui.datavisualisation.basePanel.BasePanel;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;


/**
 * Viewer to show alignments of reads to the reference.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class AlignmentViewer extends AbstractViewer implements ThreadListener {

    private static final long serialVersionUID = 234765253;
    private final TrackConnector trackConnector;
    private LayoutI layout;
    private final int blockHeight;
    private final int layerHeight;
    private int fwdMappingsInInterval;
    private int revMappingsInInterval;
    private int maxCoverageInInterval;
    private int oldLogLeft;
    private int oldLogRight;
    private boolean showBaseQualities;
    MappingResult mappingResult;


    /**
     * Viewer to show alignments of reads to the reference.
     * <p>
     * @param boundsInfoManager the bounds info manager for the viewer
     * @param basePanel         the base panel on which the viewer is located
     * @param refGenome         the reference genome
     * @param trackConnector    connector of the track to show in this viewer
     */
    public AlignmentViewer( BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistentReference refGenome, TrackConnector trackConnector ) {
        super( boundsInfoManager, basePanel, refGenome );
        this.trackConnector = trackConnector;
        this.setInDrawingMode( true );
        this.showSequenceBar( true, true );
        blockHeight = 8;
        layerHeight = blockHeight + 2;
        mappingResult = new MappingResult( new ArrayList<Mapping>(), null );
        this.setHorizontalMargin( 10 );
        this.setActive( false );
        this.setAutomaticCentering( true );
        this.addPreferenceListeners();
        setupComponents();
    }


    /**
     * Initializes the base quality option boolean and creates a
     * PreferenceChangeListener for the base quality option.
     */
    private void addPreferenceListeners() {
        final Preferences pref = NbPreferences.forModule( Object.class );
        this.showBaseQualities = pref.getBoolean( Properties.BASE_QUALITY_OPTION, true );
        pref.addPreferenceChangeListener( new PreferenceChangeListener() {

            @Override
            public void preferenceChange( PreferenceChangeEvent evt ) {
                if( evt.getKey().equals( Properties.BASE_QUALITY_OPTION ) ) {
                    showBaseQualities = pref.getBoolean( Properties.BASE_QUALITY_OPTION, true );
                }
                showData();
            }


        } );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaximalHeight() {
        return this.getHeight();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void changeToolTipText( int logPos ) {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void boundsChangedHook() {
        if( this.isInMaxZoomLevel() && isActive() ) {
            //  updatePhysicalBounds();
            setInDrawingMode( true );
        }
        else {
            setInDrawingMode( false );
        }
        this.setupComponents();
    }


    /**
     * Sets up all components of the alignment viewer = the alignments.
     */
    private void setupComponents() {

        // at least sufficient horizontal zoom level to show bases
        if( !this.isInMaxZoomLevel() ) {
            this.getBoundsInformationManager().zoomLevelUpdated( 1 );
        }

        if( isInDrawingMode() ) { //request the data to show, receiveData method calls draw methods
            this.requestData( super.getBoundsInfo().getLogLeft(), super.getBoundsInfo().getLogRight() );
        }
    }


    /**
     * Requests new mapping data for the current bounds or shows the mapping
     * data, if it is already available.
     */
    private void requestData( int from, int to ) {

        int logLeft = this.getBoundsInfo().getLogLeft();
        int logRight = this.getBoundsInfo().getLogRight();
        if( logLeft != this.oldLogLeft || logRight != this.oldLogRight || this.isNewDataRequestNeeded() ) {

            this.setNewDataRequestNeeded( false );
            setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
            this.trackConnector.addMappingRequest( new IntervalRequest( from, to, this.getReference().getActiveChromId(), this, true, this.getReadClassParams() ) );
            this.oldLogLeft = logLeft;
            this.oldLogRight = logRight;
        }
        else { //needed when e.g. mapping classes are deselected
            showData();
        }
    }


    /**
     * Method called, when data is available. If the avialable data is a
     * MappingResult, then the viewer is updated with the new mapping
     * data.
     * <p>
     * @param data the new mapping data to show
     */
    @Override
    @SuppressWarnings( "unchecked" )
    public void receiveData( Object data ) {
        if( data.getClass().equals( mappingResult.getClass() ) ) {
            this.mappingResult = ((MappingResult) data);
            this.showData();
        }
    }


    /**
     * Actually takes care of the drawing of all components of the viewer.
     */
    private void showData() {

        this.findMinAndMaxCount( mappingResult.getMappings() ); //for currently shown mappingResult
        this.setViewerHeight();
        this.adjustPaintingAreaInfoPrefSize();
        this.layout = new Layout( mappingResult.getRequest().getFrom(), mappingResult.getRequest().getTo(), mappingResult.getMappings(), getExcludedClassifications() );

        this.removeAll();

        if( this.hasLegend() ) {
            this.add( this.getLegendLabel() );
            this.add( this.getLegendPanel() );
        }
        if( this.hasOptions() ) {
            this.add( this.getOptionsLabel() );
            this.add( this.getOptionsPanel() );
        }
        // if a sequence viewer was set for this panel, add/show it
        if( this.hasSequenceBar() ) {
            this.add( this.getSequenceBar() );
        }

        getSequenceBar().setGenomeGapManager( layout.getGenomeGapManager() );
        this.addAllBlocks( layout );
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );

        this.repaint();
    }


    /**
     * Determines the (min and) max count of mappingResult on a given set of
     * mappingResult.
     * Minimum count is currently disabled as it was not needed.
     * <p>
     * @param mappingResult
     */
    private void findMinAndMaxCount( Collection<Mapping> mappings ) {
//        this.minCountInInterval = Integer.MAX_VALUE; //uncomment all these lines to get min count
        this.fwdMappingsInInterval = 0;

        for( Mapping m : mappings ) {
//            if(coverage < minCountInInterval) {
//                minCountInInterval = coverage;
//            }
            if( m.isFwdStrand() ) {
                ++this.fwdMappingsInInterval;
            }
        }
        this.revMappingsInInterval = mappings.size() - this.fwdMappingsInInterval;
    }


    /**
     * Determines maximum coverage in the currently displayed interval.
     * <p>
     * @param coverage coverage hashmap of positions for current interval
     */
    private void findMaxCoverage( Map<Integer, Integer> coverage ) {
        this.maxCoverageInInterval = 0;

        int coverageAtPos;
        for( Integer position : coverage.keySet() ) {

            coverageAtPos = coverage.get( position );
            if( coverageAtPos > this.maxCoverageInInterval ) {
                this.maxCoverageInInterval = coverageAtPos;
            }
        }
    }


    /**
     * After creating a layout this method creates all visual components which
     * represent the layout. Thus, it creates all block components.
     * Each block component depicts one mapping.
     * <p>
     * @param layout the layout containing all information about the
     *               mappingResult to paint
     */
    private void addAllBlocks( LayoutI layout ) {

        // forward strand
        int countingStep = 1;
        Iterator<LayerI> it = layout.getForwardIterator();
        this.addBlocks( it, countingStep );

        // reverse strand
        countingStep = -1;
        Iterator<LayerI> itRev = layout.getReverseIterator();
        this.addBlocks( itRev, countingStep );
    }


    /**
     * After creating a layout this method creates all visual components which
     * represent the part of the layout stored in the given layer iterator.
     * Thus, it creates all block components for each iterator entry. Each block
     * component depicts one mapping.
     * <p>
     * @param layerIt      the layer iterator containing all information about
     *                     the
     *                     mappings to paint on the current layer
     * @param countingStep define how to count each step (e.g. +1 or -1)
     */
    private void addBlocks( Iterator<LayerI> layerIt, final int countingStep ) {
        int layerCounter = countingStep;
        while( layerIt.hasNext() ) {
            LayerI b = layerIt.next();
            for( Iterator<BlockI> blockIt = b.getBlockIterator(); blockIt.hasNext(); ) {
                BlockI block = blockIt.next();
                this.createJBlock( block, layerCounter );
            }

            layerCounter += countingStep;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent( Graphics graphics ) {
        super.paintComponent( graphics );
        Graphics2D g = (Graphics2D) graphics;

        if( isInDrawingMode() ) {
            g.setColor( ColorProperties.TRACKPANEL_MIDDLE_LINE );
            drawBaseLines( g );
        }
    }


    /**
     * Creates a new block component vertically in the current layer and
     * horizontally
     * covering it's aligned genome positions.
     * <p>
     * @param block        the block to create a jblock (block component) for
     * @param layerCounter determines in which layer the block should be painted
     */
    private void createJBlock( BlockI block, int layerCounter ) {
        BlockComponent jb = new BlockComponent( block, this, layout.getGenomeGapManager(), blockHeight, showBaseQualities );

        // negative layer counter means reverse strand
        int lower = (layerCounter < 0 ? getPaintingAreaInfo().getReverseLow() : getPaintingAreaInfo().getForwardLow());
        int yPosition = lower - layerCounter * layerHeight;
        yPosition -= jb.getHeight() / 2;
        jb.setBounds( jb.getPhyStart(), yPosition, jb.getPhyWidth(), jb.getHeight() );
        this.add( jb );
    }


    private void drawBaseLines( Graphics2D graphics ) {
        PaintingAreaInfo info = getPaintingAreaInfo();
        graphics.drawLine( info.getPhyLeft(), info.getForwardLow(), info.getPhyRight(), info.getForwardLow() );
        graphics.drawLine( info.getPhyLeft(), info.getReverseLow(), info.getPhyRight(), info.getReverseLow() );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int transformToLogicalCoord( int physPos ) {
        int logPos = super.transformToLogicalCoord( physPos );
        if( isInDrawingMode() ) {
            int gapsSmaller = layout.getGenomeGapManager().getAccumulatedGapsSmallerThan( logPos );
            logPos -= gapsSmaller;
        }
        return logPos;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double transformToPhysicalCoord( int logPos ) {

        // if this viewer is operating in detail view mode, adjust logPos
        if( layout != null && isInDrawingMode() ) {
            int gapsSmaller = layout.getGenomeGapManager().getNumOfGapsSmaller( logPos );
            logPos += gapsSmaller;
        }
        return super.transformToPhysicalCoord( logPos );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidthOfMouseOverlay( int position ) {
        PhysicalBaseBounds mouseAreaLeft = getPhysBoundariesForLogPos( position );

        int width = (int) mouseAreaLeft.getPhysWidth();
        // if currentPosition is a gap, the following bases to the right marks the same position!
        // situation may occur, that on startup no layout is computed but this methode is called, although
        if( layout != null && layout.getGenomeGapManager().hasGapAt( position ) ) {
            width *= (layout.getGenomeGapManager().getNumOfGapsAt( position ) + 1);
        }
        return width;
    }


    /**
     * Adapts the height of the alignment viewer according to the content
     * currently displayed.
     */
    private void setViewerHeight() {

        int biggestCoverage = this.maxCoverageInInterval / 2;
        int biggerStrandCoverage = fwdMappingsInInterval > revMappingsInInterval ? fwdMappingsInInterval : revMappingsInInterval;
        if( biggerStrandCoverage > biggestCoverage ) {
            biggestCoverage = biggerStrandCoverage * 2; //to cover both halves
        }
        biggestCoverage = biggestCoverage <= 0 ? 1 : biggestCoverage;
        int newHeight = (int) (this.layerHeight * biggestCoverage * 1.5); //1.5 = factor for possible empty spacings between alignments
        final int spacer = 120;
        this.setPreferredSize( new Dimension( this.getWidth(), newHeight + spacer ) );
        this.revalidate();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySkipped() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }


}
