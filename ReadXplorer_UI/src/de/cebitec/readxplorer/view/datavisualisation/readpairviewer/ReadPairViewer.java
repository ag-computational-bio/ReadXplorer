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

package de.cebitec.readxplorer.view.datavisualisation.readpairviewer;


import de.cebitec.readxplorer.databackend.IntervalRequest;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.ThreadListener;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.ReadPairResultPersistent;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readxplorer.view.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.view.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.view.datavisualisation.abstractviewer.PaintingAreaInfo;
import de.cebitec.readxplorer.view.datavisualisation.abstractviewer.PhysicalBaseBounds;
import de.cebitec.readxplorer.view.datavisualisation.alignmentviewer.BlockI;
import de.cebitec.readxplorer.view.datavisualisation.alignmentviewer.LayerI;
import de.cebitec.readxplorer.view.datavisualisation.alignmentviewer.LayoutI;
import de.cebitec.readxplorer.view.datavisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;


/**
 * Viewer for read pairs.
 *
 * @author rhilker
 */
public class ReadPairViewer extends AbstractViewer implements ThreadListener {

    private static final long serialVersionUID = 234765253;
    private final TrackConnector trackConnector;
    private LayoutI layout;
    private final PersistentReference refGen;
    private final int blockHeight;
    private final int layerHeight;
//    private int minCountInInterval;
    private int viewerHeight;
//    private int maxCountInInterval;
//    private int fwdMappingsInInterval;
//    private int revMappingsInInterval;
//    private int maxCoverageInInterval;
    private final float minSaturationAndBrightness;
//    private float maxSaturationAndBrightness;
//    private float percentSandBPerCovUnit;
    private int oldLogLeft;
    private int oldLogRight;
    private ReadPairResultPersistent readPairs;
    private boolean mappingsLoading = false;
    private List<BlockComponentPair> jBlockList;

    private long start;
    private long stop;


    /**
     * Creates a new viewer for displaying read pair information between two
     * tracks. Each of them must hold one sequence of the pair.
     * <p>
     * @param boundsInfoManager the bounds info manager
     * @param basePanel         base panel on which to display this viewer
     * @param refGen            the reference genome
     * @param trackConnector    track connector of one of the two read pair
     *                          tracks
     */
    public ReadPairViewer( BoundsInfoManager boundsInfoManager, BasePanel basePanel, PersistentReference refGen, TrackConnector trackConnector ) {
        super( boundsInfoManager, basePanel, refGen );
//        this.createFocusListener();
//        this.paintPanel = new JPanel();
        this.refGen = refGen;
        this.trackConnector = trackConnector;
        this.showSequenceBar( true, false );
        blockHeight = 5;
        layerHeight = blockHeight + 1;
        minSaturationAndBrightness = 0.9f;
//        maxSaturationAndBrightness = 0.9f;
        this.setHorizontalMargin( 10 );
        this.setupComponents();
        this.setActive( false );
        this.readPairs = new ReadPairResultPersistent( null, null );

        final Preferences pref = NbPreferences.forModule( Object.class );
        pref.addPreferenceChangeListener( new PreferenceChangeListener() {

            @Override
            public void preferenceChange( PreferenceChangeEvent evt ) {
                addBlocks( layout );
            }


        } );
    }


    @Override
    public int getMaximalHeight() {
        return this.getHeight();
    }


    @Override
    public void changeToolTipText( int logPos ) {
    }


    @Override
    public void boundsChangedHook() {

        if( this.isActive() ) {
            this.setInDrawingMode( true );
            this.setupComponents();
        }
        else {
            this.setInDrawingMode( false );
        }

    }


    /**
     * Decides, if data needs to be requested and components have to be set up.
     */
    private void setupComponents() {
        if( this.isInDrawingMode() ) {
            int from = this.getBoundsInfo().getLogLeft();
            int to = this.getBoundsInfo().getLogRight();
            if( from != this.oldLogLeft || to != this.oldLogRight || this.isNewDataRequestNeeded() ) {
                this.requestData( from, to );
            }
        }
    }


    /**
     * Requests the needed mapping data for the given reference interval.
     * <p>
     * @param from left (smaller) border of interval
     * @param to   right (larger) border of interval
     */
    private void requestData( int from, int to ) {
        this.setNewDataRequestNeeded( false );
        start = System.currentTimeMillis();
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        this.jBlockList = new ArrayList<>();
        this.removeAll();
        //check for feature types in the exclusion list and adapt database query for performance
        List<Classification> excludedFeatureTypes = this.getExcludedClassifications(); //TODO: this does not do anything in the reader! rethink filtering here
        //TODO: add unique filter to read pair viewer
        this.mappingsLoading = true;
        ParametersReadClasses readClassParams = new ParametersReadClasses( excludedFeatureTypes, new Byte( "0" ) );
        trackConnector.addMappingRequest( new IntervalRequest( from, to, from - 1000, to + 1000, this.getRefGen().getActiveChromId(), this, false,
                                                               Properties.READ_PAIRS, Byte.valueOf( "0" ), readClassParams ) );
        this.oldLogLeft = from;
        this.oldLogRight = to;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public void receiveData( Object data ) {
        if( data.getClass().equals( readPairs.getClass() ) ) {
            this.readPairs = (ReadPairResultPersistent) data;
            stop = System.currentTimeMillis();
            System.out.println( Benchmark.calculateDuration( start, stop, "Request: " ) );
            this.createAndShowNewLayout();
        }
    }


    /**
     * Creates the complete layout of this viewer for a given interval.
     */
    public void createAndShowNewLayout() {

        if( this.hasLegend() ) {
            this.add( this.getLegendLabel() );
            this.add( this.getLegendPanel() );
        }
        // if a sequence viewer was set for this panel, add/show it
        if( this.hasSequenceBar() ) {
            this.add( this.getSequenceBar() );
        }
        layout = new LayoutPairs( oldLogLeft, oldLogRight, readPairs.getReadPairs(), this.getExcludedClassifications() );
        start = System.currentTimeMillis();
        this.addBlocks( layout );
        stop = System.currentTimeMillis();
        System.out.println( Benchmark.calculateDuration( start, stop, "CreateBlocks: " ) );
        start = System.currentTimeMillis();
        for( BlockComponentPair comp : jBlockList ) {
            this.add( comp );
        }
        stop = System.currentTimeMillis();
        System.out.println( Benchmark.calculateDuration( start, stop, "AddBlocks: " ) );
        this.mappingsLoading = false;
        this.setViewerHeight();
        this.repaint();
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }


    /**
     * Adds the read pair blocks to this viewer.
     * <p>
     * @param layout the layout of the blocks to add
     */
    private void addBlocks( LayoutI layout ) {

        // only show reverse layer
        int layerCounter = -1;
        int countingStep = -1;
        Iterator<LayerI> layerIt = layout.getReverseIterator();
        boolean isOneBlockAdded = false;
        boolean isBlockAdded;
        LayerI b;
        Iterator<BlockI> blockIt;
        BlockPair block;
        while( layerIt.hasNext() ) {
            b = layerIt.next();
            blockIt = b.getBlockIterator();

            while( blockIt.hasNext() ) {
                block = (BlockPair) blockIt.next();
                isBlockAdded = this.createJBlock( block, layerCounter );
                isOneBlockAdded = isOneBlockAdded ? isOneBlockAdded : isBlockAdded;
            }

            if( isOneBlockAdded ) {
                layerCounter += countingStep;
                isOneBlockAdded = false;
            }

        }
        this.viewerHeight = Math.abs( layerCounter ) * this.layerHeight + 20;
    }


    /**
     * Creates a new visible component (BlockComponentPair) representing a
     * read pair no matter if it only consists of a single mapping, one
     * mapping of the pair, or both pair mappings and other single mappings.
     * <p>
     * @param block        the pair data to show is stored in this object
     * @param layerCounter determines the y-position of the component
     * <p>
     * @return true, if the pair has visible components and should be added to
     *         the panel, false otherwise
     */
    private boolean createJBlock( BlockPair block, int layerCounter ) {
        BlockComponentPair blockComp = new BlockComponentPair( block, this, blockHeight, minSaturationAndBrightness );

        if( blockComp.isPaintable() ) {
            // the read pair viewer only uses the negative/reverse layer
            int lower = (layerCounter < 0 ? getPaintingAreaInfo().getReverseLow() : getPaintingAreaInfo().getForwardLow());
            int yPosition = lower - layerCounter * layerHeight;
            // reverse/negative layer
            yPosition -= blockComp.getHeight() / 2;

            blockComp.setBounds( blockComp.getPhyStart(), yPosition, blockComp.getPhyWidth(), blockComp.getHeight() );
            this.jBlockList.add( blockComp );
            return true;
        }
        else {
            return false;
        }

    }


    @Override
    protected void paintComponent( Graphics graphics ) {
        super.paintComponent( graphics );
        Graphics2D g = (Graphics2D) graphics;
//
//        if (this.mappingsLoaded) {
//
        if( isInDrawingMode() ) {
            g.setColor( ColorProperties.TRACKPANEL_MIDDLE_LINE );
            drawBaseLines( g );
        }
////            g.setColor(Color.black);
//        }

        if( mappingsLoading ) {
            Color fillcolor = ColorProperties.TITLE_BACKGROUND;
            g.setColor( fillcolor );
            BufferedImage loadingIndicator = this.getLoadingIndicator();
            if( loadingIndicator != null ) {
                g.drawImage( loadingIndicator, this.getWidth() - 60 - loadingIndicator.getWidth(), 5, loadingIndicator.getWidth(), loadingIndicator.getHeight(), this );
            }
        }
    }


    private void drawBaseLines( Graphics2D graphics ) {
        PaintingAreaInfo info = getPaintingAreaInfo();
        graphics.drawLine( info.getPhyLeft(), info.getForwardLow(), info.getPhyRight(), info.getForwardLow() );
        graphics.drawLine( info.getPhyLeft(), info.getReverseLow(), info.getPhyRight(), info.getReverseLow() );
    }


    @Override
    public int getWidthOfMouseOverlay( int position ) {
        PhysicalBaseBounds mouseAreaLeft = getPhysBoundariesForLogPos( position );

        int width = (int) mouseAreaLeft.getPhysWidth();
        return width;
    }


    public PersistentReference getRefGen() {
        return refGen;
    }


    /**
     * Adapts the height of the alignment viewer according to the content
     * currently displayed.
     */
    private void setViewerHeight() {

//        int biggestCoverage = this.maxCoverageInInterval / 2;
//        int biggerStrandCoverage = this.pairCountInInterval; //fwdMappingsInInterval > revMappingsInInterval ? fwdMappingsInInterval : revMappingsInInterval;
//        if (biggerStrandCoverage > biggestCoverage) {
//            biggestCoverage = biggerStrandCoverage * 2; //to cover both halves
//        }
        int newHeight = this.viewerHeight;//(int) (this.layerHeight * biggestCoverage * 1.5); //1.5 = factor for possible empty spacings between alignments
        final int spacer = 120;
        this.setPreferredSize( new Dimension( this.getWidth(), newHeight + spacer ) );
        this.revalidate();
    }


    @Override
    public void notifySkipped() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

//    private void createFocusListener() {
//        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//        focusManager.addPropertyChangeListener(new PropertyChangeListener() {
//
//            @Override
//            public void propertyChange(PropertyChangeEvent e) {
//                if (("focusOwner".equals(e.getPropertyName()))) {
//                    Component comp = (Component) e.getNewValue();
//                    if (comp instanceof AbstractViewer && comp != ReadPairViewer.this){
//                        setActive(false);
//                    }
//                }
//            }
//        });
//    }

    //    /**
//     * Determines the (min and) max count of mappings on a given set of mappings.
//     * Minimum count is currently disabled as it was not needed.
//     * @param readPairs
//     */
//    private void findMinAndMaxCount(Collection<ReadPairGroup> readPairs) {
////        this.minCountInInterval = Integer.MAX_VALUE; //uncomment all these lines to get min count
//        this.maxCountInInterval = Integer.MIN_VALUE;
////        this.fwdMappingsInInterval = 0;
//        this.pairCountInInterval = 0;
//
////        for (ReadPairGroup pair : readPairs) {
//            ++this.pairCountInInterval;
////            if (pair.getVisibleMapping().isForwardStrand()){
////                ++this.fwdMappingsInInterval;
////            }
//
//        }
////        this.revMappingsInInterval = readPairs.size() - this.fwdMappingsInInterval;
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
}
