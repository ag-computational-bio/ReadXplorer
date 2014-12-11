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

package de.cebitec.readxplorer.databackend.dataObjects;


import de.cebitec.readxplorer.utils.classification.Classification;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Container for all different coverage types for a given interval. If you want
 * to set each coverage position separately you have to call
 * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have length
 * 0.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CoverageManager implements Serializable {

    private static final long serialVersionUID = 1L;

    //2 for fwd and 2 rev

    private int leftBound;
    private int rightBound;
    private boolean twoTracks = false;

    private Map<Classification, Coverage> coverageMap;

    private int highestCoverage;

    public static byte DIFF = 1;
    public static byte TRACK2 = 2;
    public static byte TRACK1 = 3;
    private Coverage totalCoverage;


    /**
     * Container for all different coverage types for a given interval. If you
     * want to set each coverage position separately you have to call
     * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have
     * length 0.
     * <p>
     * @param leftBound  left bound of the interval
     * @param rightBound right bound of the interval
     */
    public CoverageManager( int leftBound, int rightBound ) {
        this( leftBound, rightBound, false );
    }


    /**
     * Container for all different coverage types for a given interval. If you
     * want to set each coverage position separately you have to call
     * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have
     * length 0.
     * <p>
     * @param leftBound  left bound of the interval in reference coordinates
     * @param rightBound right bound of the interval in reference coordinates
     * @param twoTracks  true, if this is a container for storing the coverage
     *                   of
     *                   two tracks
     */
    public CoverageManager( int leftBound, int rightBound, boolean twoTracks ) {
        this.twoTracks = twoTracks;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.coverageMap = new HashMap<>();

        for( Classification classification : MappingClass.values() ) {
            coverageMap.put( classification, new Coverage( leftBound, rightBound, classification ) );
        }
    }


    /**
     * @return The left bound of the stored coverage interval in reference
     *         coordinates. The borders are inclusive in the data structures.
     */
    public int getLeftBound() {
        return leftBound;
    }


    /**
     * @return The right bound of the stored coverage interval in reference
     *         coordinates. The borders are inclusive in the data structures.
     */
    public int getRightBound() {
        return rightBound;
    }


    /**
     * @param leftBound The left bound of the stored coverage interval in
     *                  reference coordinates. The borders are inclusive in the data structures.
     */
    public void setLeftBound( int leftBound ) {
        this.leftBound = leftBound;
        for( Map.Entry<Classification, Coverage> entry : coverageMap.entrySet() ) {
            entry.getValue().setLeftBound( leftBound );
        }
    }


    /**
     * @param rightBound The right bound of the stored coverage interval in
     *                   reference coordinates. The borders are inclusive in the data structures.
     */
    public void setRightBound( int rightBound ) {
        this.rightBound = rightBound;
        for( Map.Entry<Classification, Coverage> entry : coverageMap.entrySet() ) {
            entry.getValue().setRightBound( rightBound );
        }
    }


    /**
     * @param left  left bound of the interval to check
     * @param right right bound of the interval to check
     * <p>
     * @return <code>true</code>, if this coverage object covers the given
     *         interval, <code>false</code> otherwise
     */
    public boolean coversBounds( int left, int right ) {
        if( this.leftBound == 0 && this.rightBound == 0 ) {
            return false;
        }
        else {
            return leftBound <= left && right <= rightBound;
        }
    }


    /**
     * Increases the coverage of the respective positions of the given coverage
     * array by one.
     * In these arrays 0 is included.
     * <p>
     * @param refStart      the start pos of the current mapping, inclusive
     * @param refStop       the stop pos of the current mapping, inclusive
     * @param coverageArray the coverage array whose coverage should be
     *                      updated for the given interval
     */
    public void increaseCoverage( int refStart, int refStop, int[] coverageArray ) {
        int indexStart = this.getInternalPos( refStart );
        int indexStop = this.getInternalPos( refStop );
        for( int i = indexStart; i <= indexStop; i++ ) {
            int currentRefPos = refStart + i - indexStart;
            if( this.coversBounds( currentRefPos, currentRefPos ) ) {
                ++coverageArray[i];
            }
        }
    }


    /**
     * @param logPos reference position to translate
     * <p>
     * @return The internal index position at which the data for the given
     *         reference position can be found
     */
    public int getInternalPos( int logPos ) {
        return logPos - this.leftBound;
    }


    /**
     * Obtain the complete coverage of a certain classification type.
     * <p>
     * @param classification The classification whose associated data is needed
     * <p>
     * @return The complete coverage of a certain classification type or
     *         <code>null</code> if the given classification is unknown.
     * <p>
     * @throws IllegalArgumentException If an unknown classification has been
     *                                  passed
     */
    public Coverage getCoverage( Classification classification ) {
        if( coverageMap.containsKey( classification ) ) {
            return coverageMap.get( classification );
        }
        throw new IllegalArgumentException( "The given read mapping classification does not exist: " + classification );
    }


    /**
     * @return true, if this CoverageManager was created for handling of two
     *         tracks and false, if it is only for one track.
     */
    public boolean isTwoTracks() {
        return twoTracks;
    }


    /**
     * @param twoTracks set true, if this CoverageManager was created for
     *                  handling of two tracks and false, if it is only for one track.
     */
    public void setTwoTracks( boolean twoTracks ) {
        this.twoTracks = twoTracks;
    }


    /**
     * Getter for the highest coverage for automatic scaling.
     * <p>
     * @return The highest coverage value in this coverage object
     */
    public int getHighestCoverage() {
        return highestCoverage;
    }


    /**
     * Setter for the highest coverage for automatic scaling.
     * <p>
     * @param highestCoverage the highest coverage value in this coverage object
     */
    public void setHighestCoverage( int highestCoverage ) {
        this.highestCoverage = highestCoverage;
    }


    /**
     * Increase the size of all arrays whose size is currently 0 to the interval
     * size covered by this Coverage object. This behaviour prevents overwriting
     * coverage data already stored in this coverage object.
     */
    public void incArraysToIntervalSize() {
        for( Map.Entry<Classification, Coverage> entry : coverageMap.entrySet() ) {
            Coverage coverage = entry.getValue();
            coverage.incArraysToIntervalSize();
        }
    }


    /**
     * Calculates the total summed coverage value for a SINGLE POSITION of all
     * allowed classifications for the given strand and coverage type.
     * <p>
     * @param excludedClasses The list of excluded read mapping classes
     * @param pos             the reference position for which the coverage
     *                        should be
     *                        obtained
     * @param isFwdStrand     <code>true</code>, if the coverage from the fwd
     *                        strand
     *                        is needed, <code>false</code> otherwise
     * <p>
     * @return The total summed coverage value for the given SINGLE POSITION.
     */
    public int getTotalCoverage( List<Classification> excludedClasses, int pos, boolean isFwdStrand ) {
        int value = 0;

        for( MappingClass mappingClass : MappingClass.values() ) {
            if( !excludedClasses.contains( mappingClass ) ) {
                value += this.getCoverage( mappingClass ).getCoverage( pos, isFwdStrand );
            }
        }
        return value;
    }


    /**
     * Calculates the total coverage of the WHOLE INTERVAL composed of the
     * different mapping classes and depending on the currently excluded
     * classes. <br>
     * As long as the data query by which this manager was created
     * contained the excludedClasses list, this method can also be used with an
     * empty list. Passing an empty list when some classifications were excluded
     * originally decreases the performance of the method, because it sums all
     * coverage arrays for all classifications, even if the array only contains
     * 0 entries.
     * <p>
     * @param excludedClasses The list of mapping classes currently excluded
     *                        from the calculation
     * <p>
     * @return The total coverage of the WHOLE INTERVAL.
     */
    public Coverage getTotalCoverage( List<Classification> excludedClasses ) {
        if( this.totalCoverage == null ) {
            this.totalCoverage = new Coverage( leftBound, rightBound, FeatureType.ANY );
            this.totalCoverage.incArraysToIntervalSize();

            for( MappingClass mappingClass : MappingClass.values() ) {
                if( !excludedClasses.contains( mappingClass ) ) {
                    this.updateTotalCoverage( mappingClass );
                }
            }
        }
        return this.totalCoverage;
    }


    /**
     * Adds the coverage of each position in the coverage array of the given
     * mapping class to the totalCoverage object.
     * <p>
     * @param mappingClass The mappings class whose data shall be added
     */
    private void updateTotalCoverage( MappingClass mappingClass ) {
        Coverage coverage = this.getCoverage( mappingClass );
        int[] fwdCov = coverage.getFwdCov();
        int[] revCov = coverage.getRevCov();
        int[] fwdTotal = this.totalCoverage.getFwdCov();
        int[] revTotal = this.totalCoverage.getRevCov();
        for( int i = 0; i < fwdCov.length; ++i ) {
            fwdTotal[i] += fwdCov[i];
            revTotal[i] += revCov[i];
        }
    }


    /**
     * @return The list of classifications for which the coverage is maintained
     *         by this coverage manager.
     */
    public List<Classification> getIncludedClassifications() {
        return new ArrayList<>( this.coverageMap.keySet() );
    }


}
