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

package de.cebitec.readxplorer.ui.datavisualisation.trackviewer;


import de.cebitec.readxplorer.databackend.IntervalRequest;
import de.cebitec.readxplorer.databackend.ThreadListener;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageAndDiffResult;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageManager;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.PaintingAreaInfo;
import de.cebitec.readxplorer.ui.datavisualisation.basepanel.BasePanel;
import de.cebitec.readxplorer.utils.ColorProperties;
import de.cebitec.readxplorer.utils.ColorUtils;
import de.cebitec.readxplorer.utils.Pair;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.Classification;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JSlider;
import org.openide.util.NbPreferences;

import static java.util.logging.Level.SEVERE;


/**
 * Display the coverage for a sequenced track related to a reference genome
 * <p>
 * @author ddoppmeier
 */
public class TrackViewer extends AbstractViewer implements ThreadListener {

    private static final Logger LOG = Logger.getLogger( TrackViewer.class.getName() );


    private static final long serialVersionUID = 572406471;
    private static final int MININTERVALLENGTH = 25000;

    private final Preferences pref = NbPreferences.forModule( Object.class );
    private NormalizationSettings normSetting = null;
    private TrackConnector trackCon;
    private final List<Integer> trackIDs;
    private List<CoverageManager> covManagers;
    private CoverageManager covManager;
    private boolean covLoaded;
    private final boolean twoTracks;
    private final int id1;
    private final int id2;
    private boolean colorChanges;
    private boolean hasNormalizationFactor = false;
    private boolean automaticScaling = pref.getBoolean( Properties.VIEWER_AUTO_SCALING, false );
    private boolean useMinimalIntervalLength = true;

    private JSlider verticalSlider = null;

    private double scaleFactor;
    private int scaleLineStep;
    private final int labelMargin;
    //mapping class list determining the order of the paths
    private List<Classification> classList;
    private Map<Classification, Color> classToColorMap;
    private final Map<Classification, Pair<GeneralPath, GeneralPath>> classToPathsMap;
    //   public static final String PROP_TRACK_CLICKED = "track clicked";
    //  public static final String PROP_TRACK_ENTERED = "track entered";
    private final boolean combineTracks;


    /**
     * Create a new panel to show coverage information
     * <p>
     * @param boundsManager manager for component bounds
     * @param basePanel
     * @param refGen        reference genome
     * @param trackCon      database connection to one track, that is displayed
     * @param combineTracks true, if the coverage of the tracks contained in the
     *                      track connector should be combined.
     */
    public TrackViewer( BoundsInfoManager boundsManager, BasePanel basePanel, PersistentReference refGen,
                        TrackConnector trackCon, boolean combineTracks ) {
        super( boundsManager, basePanel, refGen );

        this.covManager = new CoverageManager( 0, 0 );
        this.trackCon = trackCon;
        this.twoTracks = this.trackCon.getAssociatedTrackNames().size() > 1;
        this.combineTracks = combineTracks;
        trackIDs = trackCon.getTrackIds();
        id1 = trackIDs.get( 0 );
        id2 = trackIDs.size() == 2 ? trackIDs.get( 1 ) : -1;
        labelMargin = 3;
        scaleFactor = 1;
        covLoaded = false;
        classToColorMap = new HashMap<>();
        classToPathsMap = new HashMap<>();

        this.setupClassesAndColors();

        pref.addPreferenceChangeListener( new PreferenceChangeListener() {

            @Override
            public void preferenceChange( PreferenceChangeEvent evt ) {
                setColors( createColors( pref ) );
                repaint();
            }


        } );

        this.setSizes();
    }


    /**
     * Sets up the mapping classes and associates them to their respective
     * colors and paths painted by this viewer.
     */
    private void setupClassesAndColors() {
        this.classList = this.createVisibleClasses();
        for( Classification mappingClass : classList ) { //init paths map with empty paths
            classToPathsMap.put( mappingClass, new Pair<>( new GeneralPath(), new GeneralPath() ) );
        }

        this.setColors( this.createColors( pref ) );
    }


    /**
     * @return The list of classifications used in this viewer. The order of the
     *         classes is important for painting the classes later. They are
     *         painted ascending from index 0.
     */
    protected List<Classification> createVisibleClasses() {
        List<Classification> newClassList = new ArrayList<>();
        newClassList.add( MappingClass.SINGLE_PERFECT_MATCH );
        newClassList.add( MappingClass.PERFECT_MATCH );
        newClassList.add( MappingClass.SINGLE_BEST_MATCH );
        newClassList.add( MappingClass.BEST_MATCH );
        newClassList.add( MappingClass.COMMON_MATCH );

        return newClassList;
    }


    /**
     * Updates the colors of the coverage in this viewer.
     * <p>
     * @param pref The preference object containing the new colors
     * <p>
     * @return
     */
    protected Map<Classification, Color> createColors( Preferences pref ) {
        Map<Classification, Color> newClassToColorMap = new HashMap<>();
        boolean uniformColoration = pref.getBoolean( ColorProperties.UNIFORM_DESIRED, false );
        if( uniformColoration ) {
            String colorRGB = pref.get( ColorProperties.UNIFORM_COLOR_STRING, "" );
            if( !colorRGB.isEmpty() ) {
                for( Classification classType : classList ) {
                    newClassToColorMap.put( classType, new Color( Integer.parseInt( colorRGB ) ) );
                }
            }
        } else {

            newClassToColorMap = ColorUtils.updateMappingClassColors();
        }
        return newClassToColorMap;
    }


    /**
     * Updates the colors of the coverage in this viewer.
     * <p>
     * @param classToColorMap
     */
    protected final void setColors( Map<Classification, Color> classToColorMap ) {
        this.classToColorMap = classToColorMap;
    }


    @Override
    public void paintComponent( Graphics graphics ) {
        super.paintComponent( graphics );
        Graphics2D g = (Graphics2D) graphics;

        // set rendering hints
        Map<Object, Object> hints = new HashMap<>();
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g.setRenderingHints( hints );

        if( this.covLoaded || this.colorChanges ) {

            this.paintCoverage( g );

        } else {
            Color fillcolor = ColorProperties.TITLE_BACKGROUND;
            g.setColor( fillcolor );
            BufferedImage loadingIndicator = this.getLoadingIndicator();
            if( loadingIndicator != null ) {
                g.drawImage( loadingIndicator, this.getWidth() - 60 - loadingIndicator.getWidth(), 5, loadingIndicator.getWidth(), loadingIndicator.getHeight(), this );
            }
            //g.fillRect(0, 0, this.getHeight()/4, this.getHeight()/4); //this.getWidth(), this.getHeight()/3);
        }

        // draw scales
        g.setColor( ColorProperties.TRACKPANEL_SCALE_LINES );
        this.createLines( this.scaleLineStep, g );

        // draw black middle line
        g.setColor( ColorProperties.TRACKPANEL_MIDDLE_LINE );
        drawBaseLines( g );
    }


    /**
     * Paints the coverage paths into the given Graphics2D object in the reverse
     * order stored in the classList
     * <p>
     * @param g the graphics object to paint on
     */
    private void paintCoverage( Graphics2D g ) {
        // fill and draw all coverage paths
        for( int i = classList.size(); --i >= 0;) {
            Classification classType = classList.get( i );
            if( classList.contains( classType ) && classToPathsMap.containsKey( classType ) ) {
                Pair<GeneralPath, GeneralPath> pathPair = classToPathsMap.get( classType );
                Color color = classToColorMap.get( classType );

                g.setColor( color );
                g.fill( pathPair.getFirst() );
                g.draw( pathPair.getFirst() );
                g.fill( pathPair.getSecond() );
                g.draw( pathPair.getSecond() );
            }
        }
    }


    /**
     * Draws the separating lines in the middle of the viewer.
     * <p>
     * @param graphics The graphics to paint on
     */
    private void drawBaseLines( Graphics2D graphics ) {
        PaintingAreaInfo info = getPaintingAreaInfo();
        graphics.drawLine( info.getPhyLeft(), info.getForwardLow(), info.getPhyRight(), info.getForwardLow() );
        graphics.drawLine( info.getPhyLeft(), info.getReverseLow(), info.getPhyRight(), info.getReverseLow() );
    }


    /**
     * Normalizes the value handed over to the method acodeording to the
     * normalization method choosen for the given track. If no normalization is
     * active, the value is returned unchanged.
     * <p>
     * @param trackID the track id this value belongs to
     * @param value   the value that should be normalized
     * <p>
     * @return the normalized value.
     */
    protected double getNormalizedValue( int trackID, double value ) {
        if( this.normSetting != null && this.normSetting.getHasNormFac( trackID ) ) {
            return this.normSetting.getIsLogNorm( trackID ) ? TrackViewer.log2( value ) : value * this.normSetting.getFactors( trackID );
        } else {
            return value;
        }
    }


    /**
     * Load coverage information for the current bounds.
     */
    private void requestCoverage() {
        covLoaded = false;
        this.setNewDataRequestNeeded( false );
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        int totalFrom = getBoundsInfo().getLogLeft();
        int totalTo = getBoundsInfo().getLogRight();
        if( this.useMinimalIntervalLength && totalTo - totalFrom < MININTERVALLENGTH ) {
            totalFrom -= MININTERVALLENGTH;
            totalTo += MININTERVALLENGTH;
        }
        trackCon.addCoverageRequest( new IntervalRequest(
                getBoundsInfo().getLogLeft(),
                getBoundsInfo().getLogRight(),
                totalFrom,
                totalTo,
                this.getReference().getActiveChromId(), this, false, this.getReadClassParams() ) );
    }


    @Override
    public synchronized void receiveData( Object coverageData ) {
        if( coverageData instanceof CoverageAndDiffResult ) {
            CoverageAndDiffResult covResult = (CoverageAndDiffResult) coverageData;
            this.covManagers = covResult.getCovManagers();
            this.covManager = covResult.getCovManager();

            this.createCoveragePaths();

            this.computeAutomaticScaling();
            this.computeScaleStep();
            this.covLoaded = true;
            this.repaint();
            this.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
        }
    }


    @Override
    public void boundsChangedHook() {
        if( this.covManager == null || this.isNewDataRequestNeeded() ||
                 !this.covManager.coversBounds( getBoundsInfo().getLogLeft(), getBoundsInfo().getLogRight() ) ) {
            this.requestCoverage();
        } else {
            // coverage already loaded
            this.createCoveragePaths();

            this.computeAutomaticScaling();
            this.covLoaded = true;
        }

        if( this.hasOptions() ) {
            this.add( this.getOptionsLabel() );
            this.add( this.getOptionsPanel() );
        }

        if( this.hasLegend() ) {
            this.add( this.getLegendLabel() );
            this.add( this.getLegendPanel() );
        }
    }


    /**
     * Creates the coverage paths, which are later painted in the viewer.
     */
    protected void createCoveragePaths() {
        this.covManager.setHighestCoverage( 0 );
        if( this.getCoverageManagers() != null && !this.getCoverageManagers().isEmpty() ) {
            Map<Classification, GeneralPath> fwdPaths = this.getCoveragePath( true );
            Map<Classification, GeneralPath> revPaths = this.getCoveragePath( false );
            for( Classification classType : classList ) {
                classToPathsMap.put( classType, new Pair<>( fwdPaths.get( classType ), revPaths.get( classType ) ) );
            }
        }
    }


    /**
     * Create a Map of GeneralPaths to their classification that represents the
     * coverage of a certain class.
     * <p>
     * @param isFwdStrand if true, coverage is drawn from bottom to top, if
     *                    false otherwise
     * <p>
     * @return Map of GeneralPaths to their classification representing the
     *         coverage of their certain class
     */
    protected Map<Classification, GeneralPath> getCoveragePath( boolean isFwdStrand ) {

        Map<Classification, GeneralPath> classToPathMap = new HashMap<>();
        for( Classification classType : classList ) {
            classToPathMap.put( classType, new GeneralPath() );
        }

        PaintingAreaInfo info = getPaintingAreaInfo();
        int orientation = (isFwdStrand ? -1 : 1); //opposite of strand
        int yLow = (isFwdStrand ? info.getForwardLow() : info.getReverseLow());
        for( int pixelX = info.getPhyLeft(); pixelX < info.getPhyRight(); pixelX++ ) {

            int left = this.transformToLogicalCoord( pixelX ); //this only once per class
            int right = this.transformToLogicalCoord( pixelX + 1 ) - 1;
            double totalCovPixel = -1;
            for( Classification classType : classList ) {

                if( this.getPaintingAreaInfo().fitsIntoAvailableSpace( totalCovPixel + 1, isFwdStrand ) ) {
                    // physical coordinate pixel and pixel+1 may cover the same base, depending on zoomlevel,
                    // if not compute max of range of values represented at position pixel
                    double yValue;
                    if( right > left ) {

                        double max = 0;
                        for( int i = left; i <= right; i++ ) {
                            double covValue = this.getCoverageValue( isFwdStrand, classType, i );
                            if( covValue > max ) {
                                max = covValue;
                            }
                        }
                        yValue = max;

                    } else {
                        yValue = this.getCoverageValue( isFwdStrand, classType, left );
                    }

                    if( yValue > 0 ) {
                        GeneralPath covPath = classToPathMap.get( classType );
                        // paint every physical position
                        double start = yLow + orientation + totalCovPixel * orientation;
                        covPath.moveTo( pixelX, start );

                        yValue = this.getCoverageYValue( yValue );
                        totalCovPixel += yValue;

                        if( !this.getPaintingAreaInfo().fitsIntoAvailableSpace( totalCovPixel, isFwdStrand ) ) {
                            totalCovPixel = this.getPaintingAreaInfo().getAvailableHeight( isFwdStrand );
                        }
                        covPath.lineTo( pixelX, yLow + orientation + totalCovPixel * orientation );
                        covPath.lineTo( pixelX, start );
                        covPath.closePath();
                    }
                } //otherwise we do not paint the path if it is out of reach
            }
        }

        return classToPathMap;
    }


    /**
     * Returns the coverage value for the given strand, coverage type and
     * position.
     * <p>
     * @param isFwdStrand if true, coverage is drawn from bottom to top, if
     *                    false otherwise
     * @param classType   the mapping classification type of the coverage path
     *                    handled here
     * @param absPos      the reference position for which the coverage should
     *                    be obtained
     * <p>
     * @return the coverage value for the given strand, coverage type and
     *         position.
     */
    protected double getCoverageValue( boolean isFwdStrand, Classification classType, int absPos ) {
        double value = this.calcCoverageValue( isFwdStrand, classType, absPos );

        if( value > this.covManager.getHighestCoverage() ) {
            this.covManager.setHighestCoverage( (int) Math.ceil( value ) );
        }

        return value;

    }


    /**
     * Calculates the (normalized) coverage value for the given strand, coverage
     * type and position.
     * <p>
     * @param isFwdStrand if true, coverage is drawn from bottom to top, if
     *                    false otherwise
     * @param classType   the mapping classification type of the coverage path
     *                    handled here
     * @param absPos      the reference position for which the coverage should
     *                    be obtained
     * <p>
     * @return the coverage value for the given strand, coverage type and
     *         position.
     */
    protected double calcCoverageValue( boolean isFwdStrand, Classification classType, int absPos ) {

        double value = 0;
        try {
            value = covManager.getCoverage( classType ).getCoverage( absPos, isFwdStrand );
        } catch( IllegalArgumentException e ) {
            LOG.log( SEVERE, "found unknown mapping classification type!" );
        }
        value = getNormalizedValue( id1, value );

        return value;

    }


    /**
     * @param coverage the coverage for a certain position
     * <p>
     * @return The current y value of the given coverage path. This represents
     *         the absoulte position on the screen (pixel) up to which the
     *         coverage path should reach.
     */
    protected int getCoverageYValue( double coverage ) {
        int value = (int) Math.round( coverage / this.scaleFactor );
        if( coverage > 0 ) {
            value = (value > 0 ? value : 1);
        }

        return value;
    }


    /**
     * @return The maximal height for this viewer.
     */
    @Override
    public int getMaximalHeight() {
        return pref.getInt( Properties.VIEWER_HEIGHT, Properties.DEFAULT_HEIGHT );
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void changeToolTipText( int logPos ) {

        StringBuilder sb = new StringBuilder( 200 );
        sb.append( "<html>" );
        sb.append( "<b>Position</b>: " ).append( logPos );
        sb.append( "<br>" );
        sb.append( "<table>" );
        sb.append( "<tr><td align=\"left\"><b>Forward strand (" ).append( getCovSum( logPos, true ) ).append( ")</b></td></tr>" );

        this.appendTooltipCoverage( logPos, true, sb );
        sb.append( "</table>" );

        sb.append( "<table>" );
        sb.append( "<tr><td align=\"left\"><b>Reverse strand (" ).append( getCovSum( logPos, false ) ).append( ")</b></td></tr>" );
        this.appendTooltipCoverage( logPos, false, sb );
        sb.append( "</table>" );
        sb.append( "</html>" );

        this.setToolTipText( sb.toString() );
    }


    /**
     * @param logPos      current genome position
     * @param isFwdStrand true, if fwd strand, false otherwise
     * <p>
     * @return the complete coverage sum at the current position
     */
    private double getCovSum( int logPos, boolean isFwdStrand ) {
        return covManager.getTotalCoverage( getExcludedClassifications(), logPos, isFwdStrand );
    }


    /**
     * Fetches and appends the coverage of all mapping classes for the given
     * position and strand to the given StringBuilder.
     * <p>
     * @param logPos      genomic position of interest
     * @param isFwdStrand true, if fwd strand, false otherwise
     * @param sb          the StringBuilder to add the coverage entry to
     */
    private void appendTooltipCoverage( int logPos, boolean isFwdStrand, StringBuilder sb ) {
        for( Classification classType : classList ) {
            double fwd = covManager.getCoverage( classType ).getCoverage( logPos, isFwdStrand );
            this.addToBuilder( sb, classType, fwd );
        }
    }


    /**
     * Adds the given coverage value for the given mapping classification index
     * to the given string builder as a nice table row.
     * <p>
     * @param sb             The string builder to add to
     * @param classification The current mapping classification
     * @param coverage       The coverage value to store in the StringBuilder
     */
    private void addToBuilder( StringBuilder sb, Classification classification, double coverage ) {
        String classType = classification.getTypeString();
        if( hasNormalizationFactor ) {
            sb.append( createTableRow( classType, coverage, TrackViewer.threeDecAfter( getNormalizedValue( id1, coverage ) ) ) );
        } else {
            sb.append( createTableRow( classType, coverage ) );
        }
    }


    protected String createTableRow( String label, double value ) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"left\">" + String.valueOf( (int) value ) + "</td></tr>";
    }


    protected String createTableRow( String label, double value, double scaleFacVal ) {
        return "<tr><td align=\"right\">" + label + ":</td><td align=\"left\">" + String.valueOf( scaleFacVal ) + " (" + String.valueOf( (int) value ) + ")" + "</td></tr>";
    }


    @Override
    public void close() {
        super.close();
        ProjectConnector.getInstance().removeTrackConnector( trackCon.getTrackID() );
        trackCon = null; //TODO: close does not work correctly always!
    }


    /**
     * Method to be called when the vertical zoom level of this track viewer was
     * changed, thus the coverage paths have to be recalculated acodeording to
     * the new zoom level. A scaleFactor of 1 means a 1:1 translation of
     * coverage to pixels. A value smaller than 1 is adjusted to 1.
     * <p>
     * @param value the new vertical zoom slider value
     */
    public void verticalZoomLevelUpdated( int value ) {
        this.scaleFactor = value < 1 ? 1 : Math.pow( value, 2 );

        if( this.covManager != null ) {
            this.createCoveragePaths();
        }

        this.computeScaleStep();
        this.repaint();
    }


    protected static double threeDecAfter( double val ) {
        int tmp = (int) (val * 1000);
        return tmp / 1000.0;
    }


    private static double log2( double num ) {
        num = num == 0 ? 1 : num;
        return (Math.log( num ) / Math.log( 2 ));
    }


    /**
     * Computes the scale line step value, which should be used for the current
     * coverage values.
     */
    private void computeScaleStep() {
        //A scaleFactor of 1 means a 1:1 translation of coverage to pixels.
        int visibleCoverage = (int) (this.getPaintingAreaInfo().getAvailableForwardHeight() * this.scaleFactor);

        if( visibleCoverage <= 10 ) {
            this.scaleLineStep = 1;
        } else if( visibleCoverage <= 50 ) {
            this.scaleLineStep = 10;
        } else if( visibleCoverage <= 100 ) {
            this.scaleLineStep = 20;
        } else if( visibleCoverage <= 200 ) {
            this.scaleLineStep = 50;
        } else if( visibleCoverage <= 500 ) {
            this.scaleLineStep = 100;
        } else if( visibleCoverage <= 1000 ) {
            this.scaleLineStep = 250;
        } else if( visibleCoverage <= 3000 ) {
            this.scaleLineStep = 500;
        } else if( visibleCoverage <= 4000 ) {
            this.scaleLineStep = 750;
        } else if( visibleCoverage <= 7500 ) {
            this.scaleLineStep = 1000;
        } else if( visibleCoverage <= 15000 ) {
            this.scaleLineStep = 2500;
        } else if( visibleCoverage <= 25000 ) {
            this.scaleLineStep = 5000;
        } else if( visibleCoverage <= 45000 ) {
            this.scaleLineStep = 7500;
        } else if( visibleCoverage <= 65000 ) {
            this.scaleLineStep = 10000;
        } else if( visibleCoverage <= 200000 ) {
            this.scaleLineStep = 20000;
        } else if( visibleCoverage <= 500000 ) {
            this.scaleLineStep = 50000;
        } else if( visibleCoverage <= 1000000 ) {
            this.scaleLineStep = 100000;
        } else {
            this.scaleLineStep = 300000;
        }
    }


    /**
     * Automatically detects the most suitable scaling value to fit the coverage
     * to the track viewer. This Method transforms highest coverage to slider
     * value, where the slider values range from 1-200. A scaleFactor of 1 means
     * a 1:1 translation of coverage to pixels. A larger scaleFactor means, that
     * the coverage is shrinked to fit the available painting area.
     */
    private void computeAutomaticScaling() {
        if( this.automaticScaling && this.covManager != null && this.verticalSlider != null ) {
            double oldScaleFactor = this.scaleFactor;
            double availablePixels = this.getPaintingAreaInfo().getAvailableForwardHeight();
            this.scaleFactor = Math.ceil( this.covManager.getHighestCoverage() / availablePixels );
            this.scaleFactor = this.scaleFactor < 1 ? 1.0 : this.scaleFactor;

            //set the inverse of the value set in verticalZoomLevelUpdated
            this.verticalSlider.setValue( (int) (Math.ceil( Math.sqrt( this.scaleFactor ) )) );
            if( oldScaleFactor != this.scaleFactor ) {
                this.createCoveragePaths();
                this.repaint();
            }
        }
    }


    /**
     * Creates the scaling lines in the background.
     * <p>
     * @param step The scaling step to paint
     * @param g    The graphics object to paint on
     */
    private void createLines( int step, Graphics2D g ) {
        PaintingAreaInfo info = this.getPaintingAreaInfo();

        int tmp = step;
        int physY = getCoverageYValue( step );

        while( physY <= info.getAvailableForwardHeight() ) {

            int forwardY = info.getForwardLow() - physY;
            int reverseY = info.getReverseLow() + physY;

            int lineLeft = info.getPhyLeft();
            int lineRight = info.getPhyRight();

            g.draw( new Line2D.Double( lineLeft, reverseY, lineRight, reverseY ) );
            g.draw( new Line2D.Double( lineLeft, forwardY, lineRight, forwardY ) );

            int labelHeight = g.getFontMetrics().getMaxAscent();
            String label = getLabel( tmp, step );


            tmp += step;
            physY = getCoverageYValue( tmp );

            int labelLeft = lineLeft - labelMargin - g.getFontMetrics().stringWidth( label );
            int labelRight = lineRight + labelMargin;

            g.drawString( label, labelLeft, reverseY + labelHeight / 2 );
            g.drawString( label, labelLeft, forwardY + labelHeight / 2 );
            // right labels
            g.drawString( label, labelRight, reverseY + labelHeight / 2 );
            g.drawString( label, labelRight, forwardY + labelHeight / 2 );
        }
    }


    private String getLabel( int logPos, int step ) {
        String label = null;
        if( logPos >= 1000 && step >= 1000 ) {
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
     * Method for updating this track viewer, when the normalization value was
     * changed.
     */
    public void normalizationValueChanged() {
        this.hasNormalizationFactor = this.normSetting.getIdToValue().keySet().size() == 2
                                      ? (normSetting.getHasNormFac( id1 ) || normSetting.getHasNormFac( id2 ))
                                      : normSetting.getHasNormFac( id1 );
        this.boundsChangedHook();
        this.repaint();
    }


    public void colorChanges() {
        this.colorChanges = true;
        this.repaint();
    }


    public TrackConnector getTrackCon() {
        return this.trackCon;
    }


    @Override
    public String toString() {
        return getName();
    }


    /**
     * @return true, if this is a double track viewer, which combines the
     *         selected tracks into a single coverage wave.
     */
    public boolean isCombineTracks() {
        return this.combineTracks;
    }


    public NormalizationSettings getNormalizationSettings() {
        return normSetting;
    }


    public void setNormalizationSettings( NormalizationSettings normSetting ) {
        this.normSetting = normSetting;
    }


    public void setVerticalZoomSlider( JSlider verticalSlider ) {
        this.verticalSlider = verticalSlider;
    }

//    /**
//     * In case this viewer should receive the ability to combine coverages from a
//     * CoverageManager array, this method provides this functionality.
//     * @param coverages the coverages of different tracks, which should be combined
//     * @return
//     */
//    private CoverageManager combineCoverages(CoverageManager[] coverages) {
//
//        CoverageManager resultCov = new CoverageManager(coverages[0].getLeftBound(), coverages[0].getRightBound());
//
//        for (int i = coverages[0].getLeftBound(); i < coverages[0].getRightBound(); ++i) {
//            for (CoverageManager cove : coverages) {
//                resultCov.getCoverage(MappingClass.PERFECT_MATCH).setFwdCoverage(i, resultCov.getCovManager(MappingClass.PERFECT_MATCH).getFwdCov(i) + cove.getCovManager(MappingClass.PERFECT_MATCH).getFwdCov(i));
//                resultCov.setPerfectFwdNum(i, resultCov.getPerfectFwdNum(i) + cove.getPerfectFwdNum(i));
//                resultCov.getCoverage(MappingClass.PERFECT_MATCH).setRevCoverage(i, resultCov.getCovManager(MappingClass.PERFECT_MATCH).getRevCov(i) + cove.getCovManager(MappingClass.PERFECT_MATCH).getRevCov(i));
//                resultCov.setPerfectRevNum(i, resultCov.getPerfectRevNum(i) + cove.getPerfectRevNum(i));
//
//                resultCov.getCoverage(MappingClass.BEST_MATCH).setFwdCoverage(i, resultCov.getCoverage(MappingClass.BEST_MATCH).getFwdCov(i) + cove.getCoverage(MappingClass.BEST_MATCH).getFwdCov(i));
//                resultCov.setBestMatchFwdNum(i, resultCov.getBestMatchFwdNum(i) + cove.getBestMatchFwdNum(i));
//                resultCov.getCoverage(MappingClass.BEST_MATCH).setRevCoverage(i, resultCov.getCoverage(MappingClass.BEST_MATCH).getRevCov(i) + cove.getCoverage(MappingClass.BEST_MATCH).getRevCov(i));
//                resultCov.setBestMatchRevNum(i, resultCov.getBestMatchRevNum(i) + cove.getBestMatchRevNum(i));
//
//                resultCov.getCoverage(MappingClass.COMMON_MATCH).setFwdCoverage(i, resultCov.getCovManager(MappingClass.COMMON_MATCH).getFwdCov(i) + cove.getCovManager(MappingClass.COMMON_MATCH).getFwdCov(i));
//                resultCov.setCommonFwdNum(i, resultCov.getCommonFwdNum(i) + cove.getCommonFwdNum(i));
//                resultCov.getCoverage(MappingClass.COMMON_MATCH).setRevCoverage(i, resultCov.getCovManager(MappingClass.COMMON_MATCH).getRevCov(i) + cove.getCovManager(MappingClass.COMMON_MATCH).getRevCov(i));
//                resultCov.setCommonRevNum(i, resultCov.getCommonRevNum(i) + cove.getCommonRevNum(i));
//            }
//        }
//
//        return resultCov;
//    }

    /**
     * @return true, if this is a track viewer for at least two tracks.
     */
    public boolean isTwoTracks() {
        return this.twoTracks;
    }


    /**
     * @param automaticScaling Set <code>true</code>, if the coverage slider
     *                         should automatically adapt to the coverage shown
     *                         (the complete coverage in the interval always is
     *                         visible). <code>false</code>, if the slider value
     *                         should only be changed manually by the user.
     */
    public void setAutomaticScaling( boolean automaticScaling ) {
        this.automaticScaling = automaticScaling;
        this.computeAutomaticScaling();
    }


    @Override
    public void notifySkipped() {
        //do nothing
    }


    /**
     * @return <code>true</code> if the queried interval length should be
     *         extended to the <code>MININTERVALLENGTH</code>,
     *         <code>false</code> if the original bounds should be used for the
     *         coverage queries.
     */
    public boolean isUseMinimalIntervalLength() {
        return useMinimalIntervalLength;
    }


    /**
     * @param useMinimalIntervalLength <code>true</code> if the queried interval
     *                                 length should be extended to the
     *                                 <code>MININTERVALLENGTH</code>,
     *                                 <code>false</code> if the original bounds
     *                                 should be used for the coverage queries.
     */
    public void setUseMinimalIntervalLength( boolean useMinimalIntervalLength ) {
        this.useMinimalIntervalLength = useMinimalIntervalLength;
    }


    /**
     * @return The coverage managers of this viewer
     */
    protected List<CoverageManager> getCoverageManagers() {
        return this.covManagers;
    }


    protected boolean hasNormalizationFactor() {
        return this.hasNormalizationFactor;
    }


    /**
     * @return The list of available classifications for this viewer.
     */
    protected List<Classification> getClassList() {
        return classList;
    }


}
