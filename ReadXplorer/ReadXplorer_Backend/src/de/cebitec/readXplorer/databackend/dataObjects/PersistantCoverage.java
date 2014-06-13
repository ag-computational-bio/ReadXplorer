/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.databackend.dataObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Container for all different coverage types for a given interval. If you want
 * to set each coverage position separately you have to call
 * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have length
 * 0.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantCoverage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    //2 for fwd and 2 rev
    
    private int leftBound;
    private int rightBound;
    private boolean twoTracks = false;
    
    private int[] perfectFwdCov;
    private int[] perfectRevCov;
    private int[] bestMatchFwdCov;
    private int[] bestMatchRevCov;
    private int[] commonFwdCov;
    private int[] commonRevCov;
    
    private int highestCoverage;
    
    //coverage Infos of track1
    private int[] commonFwdCovTrack1;
    private int[] commonRevCovTrack1;
    //coverage Infos of track2
    private int[] commonFwdCovTrack2;
    private int[] commonRevCovTrack2;
    
    public static byte PERFECT = 1;
    public static byte BM = 2;
    public static byte NERROR = 3;
    public static byte DIFF = 1;
    public static byte TRACK2 = 2;
    public static byte TRACK1 = 3;

    /**
     * Container for all different coverage types for a given interval. If you
     * want to set each coverage position separately you have to call
     * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have
     * length 0.
     * @param leftBound left bound of the interval
     * @param rightBound right bound of the interval
     */
    public PersistantCoverage(int leftBound, int rightBound) {
        this(leftBound, rightBound, false);
    }

    /**
     * Container for all different coverage types for a given interval. If you
     * want to set each coverage position separately you have to call
     * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have
     * length 0.
     * @param leftBound left bound of the interval
     * @param rightBound right bound of the interval
     * @param twoTracks true, if this is a container for storing the coverage of
     * two tracks
     */
    public PersistantCoverage(int leftBound, int rightBound, boolean twoTracks) {
        this.twoTracks = twoTracks;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        
        perfectFwdCov = new int[0];
        perfectRevCov = new int[0];
            
        bestMatchFwdCov = new int[0];
        bestMatchRevCov = new int[0];
        
        commonFwdCov = new int[0];
        commonRevCov = new int[0];
        
        commonFwdCovTrack1 = new int[0];
        commonFwdCovTrack2 = new int[0];
        commonRevCovTrack1 = new int[0];
        commonRevCovTrack2 = new int[0];
    }

    /**
     * @return The left bound of the stored coverage interval. The borders are
     * inclusive in the data structures.
     */
    public int getLeftBound() {
        return leftBound;
    }

    /**
     * @return The right bound of the stored coverage interval. The borders are
     * inclusive in the data structures.
     */
    public int getRightBound() {
        return rightBound;
    }

    public void setLeftBound(int leftBound) {
        this.leftBound = leftBound;
    }

    public void setRightBound(int rightBound) {
        this.rightBound = rightBound;
    }

    /**
     * @param left left bound of the interval to check
     * @param right right bound of the interval to check
     * @return <code>true</code>, if this coverage object covers the given interval,
     * <code>false</code> otherwise
     */
    public boolean coversBounds(int left, int right) {
        if (this.leftBound == 0 && this.rightBound == 0) {
            return false;
        } else {
            return leftBound <= left && right <= rightBound;
        }
    }
    
    /**
     * Increases the coverage of the coverage arrays in the given list by one.
     * In these arrays 0 is included.
     * @param refStart the start pos of the current read, inclusive
     * @param refStop the stop pos of the current read, inclusive
     * @param coverageArrays the coverage arrays whose positions should be
     * updated
     */
    public void increaseCoverage(int refStart, int refStop, List<int[]> coverageArrays) {
        int indexStart = this.getInternalPos(refStart);
        int indexStop = this.getInternalPos(refStop);
        for (int i = indexStart; i <= indexStop; i++) {
            int currentRefPos = refStart + i - indexStart;
            if (this.coversBounds(currentRefPos, currentRefPos)) {
                for (int[] covArray : coverageArrays) {
                    ++covArray[i];
                }
            }
        }
    }
    
    /**
     * @param logPos reference position to translate
     * @return The internal index position at which the data for the given 
     * reference position can be found
     */
    private int getInternalPos(int logPos) {
        return logPos - this.leftBound;
    }
    
    /**
     * Sets the coverage for a given reference position to a given value in the
     * given array.
     * @param logPos the reference position whose coverage shall be updated
     * @param coverage the coverage value to store
     * @param covArray the coverage array in which the value shall be stored
     */
    private void setCoverage(int logPos, int coverage, int[] covArray) {
        covArray[this.getInternalPos(logPos)] = coverage;
    }
    
    /**
     * Increases the coverage for a given reference position to a given value in 
     * the given array.
     * @param logPos the reference position whose coverage shall be updated
     * @param coverage the coverage value to store
     * @param covArray the coverage array in which the value shall be stored
     */
    private void increaseCoverage(int logPos, int valueToAdd, int[] covArray) {
        covArray[this.getInternalPos(logPos)] += valueToAdd;
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is
     * needed
     * @return the best match forward coverage with duplicates.
     */
    private int getCoverage(int logPos, int[] coverageArray) {
        int internalPos = this.getInternalPos(logPos);
        if (internalPos < coverageArray.length && internalPos >= 0) {
            return coverageArray[internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Set the best match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchFwd(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, bestMatchFwdCov);
    }

    /**
     * Set the best match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchRev(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, bestMatchRevCov);
    }

    /**
     * Set the common match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwd(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, commonFwdCov);
    }

    /**
     * Set the common match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRev(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, commonRevCov);
    }

    /**
     * Set the perfect match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectFwd(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, perfectFwdCov);
    }

    /**
     * Set the perfect match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectRev(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, perfectRevCov);
    }
    


    /**
     * Set the common match forward coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdTrack1(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, commonFwdCovTrack1);
    }
    
    /**
     * Set the common match forward coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdTrack2(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, commonFwdCovTrack2);
    }
    
    /**
     * Set the common match reverse coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevTrack1(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, commonRevCovTrack1);
    }
    
    /**
     * Set the common match reverse coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevTrack2(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.setCoverage(logPos, coverage, commonRevCovTrack2);
    }
    
    /**
     * Increases the best match forward coverage with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseBestMatchFwd(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, bestMatchFwdCov);
    }
     
    /**
     * Increases the best match reverse coverage with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseBestMatchRev(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, bestMatchRevCov);
    }
    
    /**
     * Increases the perfect match forward coverage with duplicates by one.
     * @param logPos position to increase
     */
    public void increasePerfectFwd(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, perfectFwdCov);
    }
    
    /**
     * Increases the perfect match reverse coverage with duplicates by one.
     * @param logPos position to increase
     */
    public void increasePerfectRev(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, perfectRevCov);
    }
    
    /**
     * Increases the common match forward coverage with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseCommonFwd(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, commonFwdCov);
    }

    /**
     * Increases the common match reverse coverage with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseCommonRev(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, commonRevCov);
    }
    
    /**
     * Increases the common match forward coverage for track 1 of a double track
     * viewer with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseCommonFwdTrack1(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, commonFwdCovTrack1);
    }

    /**
     * Increases the common match reverse coverage for track 1 of a double track
     * viewer with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseCommonRevTrack1(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, commonRevCovTrack1);
    }
    
    /**
     * Increases the common match forward coverage for track 2 of a double track
     * viewer with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseCommonFwdTrack2(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, commonFwdCovTrack2);
    }

    /**
     * Increases the common match reverse coverage for track 2 of a double track
     * viewer with duplicates by one.
     * @param logPos position to increase
     */
    public void increaseCommonRevTrack2(int logPos) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage(logPos, 1, commonRevCovTrack2);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the best match forward coverage with duplicates.
     */
    public int getBestMatchFwd(int logPos) {
        return this.getCoverage(logPos, bestMatchFwdCov);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the best match reverse coverage with duplicates.
     */
    public int getBestMatchRev(int logPos) {
        return this.getCoverage(logPos, bestMatchRevCov);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the common match forward coverage with duplicates.
     */
    public int getCommonFwd(int logPos) {
        return this.getCoverage(logPos, commonFwdCov);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the common match reverse coverage with duplicates.
     */
    public int getCommonRev(int logPos) {
        return this.getCoverage(logPos, commonRevCov);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the perfect match forward coverage with duplicates. If the
     * position is not covered 0 is returned.
     */
    public int getPerfectFwd(int logPos) {
        return this.getCoverage(logPos, perfectFwdCov);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the perfect match reverse coverage with duplicates.
     */
    public int getPerfectRev(int logPos) {
        return this.getCoverage(logPos, perfectRevCov);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the common match forward coverage with duplicates for track 1 in
     * a two track analysis case.
     */

    public int getCommonFwdTrack1(int logPos) {
        return this.getCoverage(logPos, commonFwdCovTrack1);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the common match reverse coverage with duplicates for track 1 in
     * a two track analysis case.
     */
    public int getCommonRevTrack1(int logPos) {
        return this.getCoverage(logPos, commonRevCovTrack1);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the common match forward coverage with duplicates for track 2 in
     * a two track analysis case.
     */
    public int getCommonFwdTrack2(int logPos) {
        return this.getCoverage(logPos, commonFwdCovTrack2);
    }

    /**
     * @param logPos absolute position on the chromosome, whose coverage is needed
     * @return the common match reverse coverage with duplicates for track 2 in
     * a two track analysis case.
     */
    public int getCommonRevTrack2(int logPos) {
        return this.getCoverage(logPos, commonRevCovTrack2);
    }
    /**
     * @return Get whole perfect match fwd coverage array for the given
     * interval.
     */
    public int[] getPerfectFwd() {
        return this.perfectFwdCov;
    }
    
    /**
     * @return Get whole perfect match rev coverage array for the given
     * interval.
     */
    public int[] getPerfectRev() {
        return this.perfectRevCov;
    }
    
    /**
     * @return Get whole best match fwd coverage array for the given 
     * interval.
     */
    public int[] getBestMatchFwd() {
        return this.bestMatchFwdCov;
    }

    /**
     * @return Get whole best match rev coverage array for the given 
     * interval.
     */    
    public int[] getBestMatchRev() {
        return this.bestMatchRevCov;
    }
    
     /**
     * @return Get whole common match fwd coverage array with replicates for 
     * the given interval.
     */   
    public int[] getCommonFwd() {
        return this.commonFwdCov;
    }
    
     /**
     * @return Get whole common match rev coverage array with replicates for 
     * the given interval.
     */   
    public int[] getCommonRev() {
        return this.commonRevCov;
    }
        
    /**
     * @return Get whole common fwd coverage array for the given interval
     * of track 1
     */
    public int[] getCommonFwdCovTrack1() {
        return this.commonFwdCovTrack1;
    }

    /**
     * @return Get whole common fwd coverage array for the given interval
     * of track 2
     */    
    public int[] getCommonFwdCovTrack2() {
        return this.commonFwdCovTrack2;
    }

    /**
     * @return Get whole common rev coverage array for the given interval
     * of track 1
     */    
    public int[] getCommonRevCovTrack1() {
        return this.commonRevCovTrack1;
    }

     /**
     * @return Get whole common rev coverage array for the given interval
     * of track 2
     */   
    public int[] getCommonRevCovTrack2() {
        return this.commonRevCovTrack2;
    }

    /**
     * @return true, if this PersistantCoverage was created for handling of two 
     * tracks and false, if it is only for one track.
     */
    public boolean isTwoTracks() {
        return twoTracks;
    }

    /**
     * @param twoTracks set true, if this PersistantCoverage was created for 
     * handling of two tracks and false, if it is only for one track.
     */
    public void setTwoTracks(boolean twoTracks) {
        this.twoTracks = twoTracks;
    }

    /**
     * Getter for the highest coverage for automatic scaling.
     * @return The highest coverage value in this coverage object
     */
    public int getHighestCoverage() {
        return highestCoverage;
    }

    /**
     * Setter for the highest coverage for automatic scaling.
     * @param highestCoverage the highest coverage value in this coverage object
     */
    public void setHighestCoverage(int highestCoverage) {
        this.highestCoverage = highestCoverage;
    }

    
    public void setPerfectFwd(int[] perfectFwdCov) {
        this.perfectFwdCov = perfectFwdCov;
    }

    public void setPerfectRev(int[] perfectRevCov) {
        this.perfectRevCov = perfectRevCov;
    }

    public void setBestMatchFwd(int[] bestMatchFwdCov) {
        this.bestMatchFwdCov = bestMatchFwdCov;
    }

    public void setBestMatchRev(int[] bestMatchRevCov) {
        this.bestMatchRevCov = bestMatchRevCov;
    }

      public void setCommonFwd(int[] commonFwdCov) {
        this.commonFwdCov = commonFwdCov;
    }

    public void setCommonRev(int[] commonRevCov) {
        this.commonRevCov = commonRevCov;
    }

    public void setCommonFwdTrack1(int[] commonFwdCovTrack1) {
        this.commonFwdCovTrack1 = commonFwdCovTrack1;
    }

    public void setCommonRevTrack1(int[] commonRevCovTrack1) {
        this.commonRevCovTrack1 = commonRevCovTrack1;
    }

    public void setCommonFwdTrack2(int[] commonFwdCovTrack2) {
        this.commonFwdCovTrack2 = commonFwdCovTrack2;
    }

    public void setCommonRevTrack2(int[] commonRevCovTrack2) {
        this.commonRevCovTrack2 = commonRevCovTrack2;
    }
    
    /**
     * Increase the size of all arrays whose size is currently 0 to the interval
     * size covered by this PersistantCoverage. This behaviour prevents overwriting
     * coverage data already stored in this coverage object.
     */
    public void incArraysToIntervalSize() {
        int size = this.rightBound - this.leftBound + 1;
        if (this.perfectFwdCov.length == 0) {
            perfectFwdCov = new int[size];
        }
        if (this.perfectRevCov.length == 0) {
            perfectRevCov = new int[size];
        }
        
        if (this.bestMatchFwdCov.length == 0) {
            bestMatchFwdCov = new int[size];
        }
        if (this.bestMatchRevCov.length == 0) {
            bestMatchRevCov = new int[size];
        }
        
        this.incCommonCovArraysToIntervalSize(size);
    }
    
    /**
     * Increase the size of all arrays, needed for the double track viewer,
     * whose size is currently 0 to the interval size covered by this
     * PersistantCoverage. This behaviour prevents overwriting coverage data
     * already stored in this coverage object.
     */
    public void incDoubleTrackArraysToIntervalSize() {
        int size = this.rightBound > this.leftBound ? this.rightBound - this.leftBound + 1 : 0;
        if (this.commonFwdCovTrack1.length == 0) {
            commonFwdCovTrack1 = new int[size];
        }
        if (this.commonRevCovTrack1.length == 0) {
            commonRevCovTrack1 = new int[size];
        }
        if (this.commonFwdCovTrack2.length == 0) {
            commonFwdCovTrack2 = new int[size];
        }
        if (this.commonRevCovTrack2.length == 0) {
            commonRevCovTrack2 = new int[size];
        }
        this.incCommonCovArraysToIntervalSize(size);
    }
    
    /**
     * Increase the size of all common coverage arrays whose size is currently 0
     * to the interval size covered by this PersistantCoverage. This behaviour
     * prevents overwriting coverage data already stored in this coverage
     * object.
     */
    private void incCommonCovArraysToIntervalSize(int size) {
        if (this.commonFwdCov.length == 0) {
            commonFwdCov = new int[size];
        }
        if (this.commonRevCov.length == 0) {
            commonRevCov = new int[size];
        }
    }
}
