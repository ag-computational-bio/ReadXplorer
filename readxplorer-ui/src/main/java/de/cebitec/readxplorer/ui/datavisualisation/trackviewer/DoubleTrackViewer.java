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


import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataObjects.Coverage;
import de.cebitec.readxplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.PaintingAreaInfo;
import de.cebitec.readxplorer.ui.datavisualisation.basepanel.BasePanel;
import de.cebitec.readxplorer.utils.ColorUtils;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.classification.Classification;
import de.cebitec.readxplorer.utils.classification.ComparisonClass;
import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;


/**
 * Display the coverage for a sequenced track related to a reference genome
 * <p>
 * @author ddoppmeier
 */
public class DoubleTrackViewer extends TrackViewer {

    private static final long serialVersionUID = 572406471;

    private final List<Integer> trackIDs;
    private final int id1;
    private final int id2;
    private List<Classification> visibleClasses;

    //   public static final String PROP_TRACK_CLICKED = "track clicked";
    //  public static final String PROP_TRACK_ENTERED = "track entered";

    /**
     * Create a new panel to show coverage information
     * <p>
     * @param boundsManager manager for component bounds
     * @param basePanel
     * @param refGen        reference genome
     * @param trackCon      database connection to one track, that is displayed
     */
    public DoubleTrackViewer( BoundsInfoManager boundsManager, BasePanel basePanel, PersistentReference refGen,
                              TrackConnector trackCon ) {
        super( boundsManager, basePanel, refGen, trackCon, false );

        trackIDs = trackCon.getTrackIds();
        id1 = trackIDs.get( 0 );
        id2 = trackIDs.size() == 2 ? trackIDs.get( 1 ) : -1;
    }


    @Override
    protected List<Classification> createVisibleClasses() {
        List<Classification> newClassList = new ArrayList<>();
        newClassList.add( ComparisonClass.DIFF_COVERAGE );
        newClassList.add( ComparisonClass.TRACK1_COVERAGE );
        newClassList.add( ComparisonClass.TRACK2_COVERAGE );

        this.visibleClasses = newClassList;
        return newClassList;
    }


    /**
     * Updates the colors of the coverage in this viewer.
     * <p>
     * @param pref The preference object containing the new colors
     * <p>
     * @return
     */
    @Override
    protected Map<Classification, Color> createColors( Preferences pref ) {
        return ColorUtils.updateComparisonClassColors();
    }


    /**
     * Create a GeneralPath that represents the coverage of a certain type.
     * <p>
     * @param isFwdStrand if true, coverage is drawn from bottom to top, if
     *                    false otherwise
     * <p>
     * @return GeneralPath representing the coverage of a certain type
     */
    @Override
    protected Map<Classification, GeneralPath> getCoveragePath( boolean isFwdStrand ) {
        Map<Classification, GeneralPath> classToPathMap = new HashMap<>();
        for( Classification classType : this.visibleClasses ) {
            classToPathMap.put( classType, new GeneralPath() );
        }

        PaintingAreaInfo info = getPaintingAreaInfo();
        int orientation = (isFwdStrand ? -1 : 1); //opposite of strand
        int yLow = (isFwdStrand ? info.getForwardLow() : info.getReverseLow());

        for( Classification classType : classToPathMap.keySet() ) {

            GeneralPath covPath = classToPathMap.get( classType );
            // paint every physical position
            covPath.moveTo( info.getPhyLeft(), yLow );
            for( int pixel = info.getPhyLeft(); pixel < info.getPhyRight(); pixel++ ) {

                int left = this.transformToLogicalCoord( pixel );
                int right = this.transformToLogicalCoord( pixel + 1 ) - 1;

                // physical coordinate pixel and pixel+1 may cover the same base, depending on zoomlevel,
                // if not compute max of range of values represented at position pixel
                double covPixel;
                if( right > left ) {

                    double max = 0;
                    for( int i = left; i <= right; i++ ) {
                        if( this.getCoverageValue( isFwdStrand, classType, i ) > max ) {
                            max = this.getCoverageValue( isFwdStrand, classType, i );
                        }
                    }
                    covPixel = max;

                }
                else {
                    covPixel = this.getCoverageValue( isFwdStrand, classType, left );
                }

                covPixel = this.getCoverageYValue( covPixel );
                if( !this.getPaintingAreaInfo().fitsIntoAvailableSpace( covPixel, isFwdStrand ) ) {
                    covPixel = getPaintingAreaInfo().getAvailableHeight( isFwdStrand );
                }

                covPath.lineTo( pixel, yLow + covPixel * orientation );
            }

            covPath.lineTo( info.getPhyRight(), yLow );
            covPath.closePath();
        }

        return classToPathMap;
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
     *                    be
     *                    obtained
     * <p>
     * @return the coverage value for the given strand, coverage type and
     *         position.
     */
    @Override
    protected double calcCoverageValue( boolean isFwdStrand, Classification classType, int absPos ) {
        double value = 0;
        if( this.getCoverageManagers().size() == 2 ) { //check for correct data structure
            Coverage cov1 = this.getCoverageManagers().get( 0 ).getTotalCoverage( this.getExcludedClassifications() );
            Coverage cov2 = this.getCoverageManagers().get( 1 ).getTotalCoverage( this.getExcludedClassifications() );
            if( classType == ComparisonClass.DIFF_COVERAGE ) {
                int value1 = (int) this.getNormalizedValue( id1, cov1.getCoverage( absPos, isFwdStrand ) );
                int value2 = (int) this.getNormalizedValue( id2, cov2.getCoverage( absPos, isFwdStrand ) );
                value = Math.abs( value2 - value1 );
            }
            else if( classType == ComparisonClass.TRACK2_COVERAGE ) {
                value = this.getNormalizedValue( id2, cov2.getCoverage( absPos, isFwdStrand ) );
            }
            else if( classType == ComparisonClass.TRACK1_COVERAGE ) {
                value = this.getNormalizedValue( id1, cov1.getCoverage( absPos, isFwdStrand ) );
            }
            else {
                Logger.getLogger( this.getClass().getName() ).log( Level.SEVERE, "found unknown coverage type!" );
            }
        }
        else {
            throw new IllegalArgumentException( "The size of the coverage manager list is not equal to 2." );
        }
        return value;
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

        List<CoverageManager> coverageManagers = this.getCoverageManagers();
        if( coverageManagers != null && coverageManagers.size() == 2 ) {
            Coverage coverage = this.getCoverageManagers().get( 0 ).getTotalCoverage( this.getExcludedClassifications() );
            Coverage coverage2 = this.getCoverageManagers().get( 1 ).getTotalCoverage( this.getExcludedClassifications() );
            double covFwd1 = coverage.getFwdCov( logPos );
            double covFwd2 = coverage2.getFwdCov( logPos );
            double covRev1 = coverage.getRevCov( logPos );
            double covRev2 = coverage2.getRevCov( logPos );

            double covFwd1Norm = 0;
            double covFwd2Norm = 0;
            double covRev1Norm = 0;
            double covRev2Norm = 0;
            double diffFwdScale = 0;
            double diffRevScale = 0;

            if( this.hasNormalizationFactor() ) {
                covFwd1Norm = TrackViewer.threeDecAfter( this.getNormalizedValue( id1, covFwd1 ) );
                covFwd2Norm = TrackViewer.threeDecAfter( this.getNormalizedValue( id2, covFwd2 ) );
                covRev1Norm = TrackViewer.threeDecAfter( this.getNormalizedValue( id1, covRev1 ) );
                covRev2Norm = TrackViewer.threeDecAfter( this.getNormalizedValue( id2, covRev2 ) );
                diffFwdScale = TrackViewer.threeDecAfter( Math.abs( covFwd1Norm - covFwd2Norm ) );
                diffRevScale = TrackViewer.threeDecAfter( Math.abs( covRev1Norm - covRev2Norm ) );
//                diffFwdScale = diffFwdScale < 0 ? diffFwdScale * -1 : diffFwdScale; //TODO: check if needed???
//                diffRevScale = diffRevScale < 0 ? diffRevScale * -1 : diffRevScale; //???
            }

            double diffFwd = Math.abs( covFwd1 - covFwd2 );
            double diffRev = Math.abs( covRev1 - covRev2 );

            this.addToBuilder( sb, ComparisonClass.DIFF_COVERAGE, diffFwd, diffFwdScale, SequenceUtils.STRAND_FWD_STRING );
            this.addToBuilder( sb, ComparisonClass.DIFF_COVERAGE, diffRev, diffRevScale, SequenceUtils.STRAND_REV_STRING );
            this.addToBuilder( sb, ComparisonClass.TRACK1_COVERAGE, covFwd1, covFwd1Norm, SequenceUtils.STRAND_FWD_STRING );
            this.addToBuilder( sb, ComparisonClass.TRACK2_COVERAGE, covFwd2, covFwd2Norm, SequenceUtils.STRAND_FWD_STRING );
            this.addToBuilder( sb, ComparisonClass.TRACK1_COVERAGE, covRev1, covRev1Norm, SequenceUtils.STRAND_REV_STRING );
            this.addToBuilder( sb, ComparisonClass.TRACK2_COVERAGE, covRev2, covRev2Norm, SequenceUtils.STRAND_REV_STRING );
        }

        this.setToolTipText( sb.toString() );
    }


    /**
     * Adds the given coverage value for the given mapping classification index
     * to the given string builder as a nice table row.
     * <p>
     * @param sb             The string builder to add to
     * @param classification The current mapping classification
     * @param coverage       The coverage value to store in the StringBuilder
     */
    private void addToBuilder( StringBuilder sb, Classification classification, double coverage, double coverageNorm, String strandString ) {
        String classType = classification.getTypeString() + " " + strandString;
        if( this.hasNormalizationFactor() ) {
            sb.append( createTableRow( classType, coverage, coverageNorm ) );
        }
        else {
            sb.append( createTableRow( classType, coverage ) );
        }
    }


}
