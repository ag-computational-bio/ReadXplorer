package de.cebitec.vamp.databackend.dataObjects;

import java.io.Serializable;

/**
 * Container for all different coverage types for a given interval. If you want
 * to set each coverage position separately you have to call
 * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have length 0.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantCoverage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    //2 for fwd (mult = all & num = without duplicates), and 2 rev
    
    private int leftBound;
    private int rightBound;
    private boolean finished;
    private boolean twoTracks = false;
    
    private int[] perfectFwdMultCov;
    private int[] perfectRevMultCov;
    private int[] perfectFwdNumCov;
    private int[] perfectRevNumCov;
    private int[] bestMatchFwdMultCov;
    private int[] bestMatchRevMultCov;
    private int[] bestMatchFwdNumCov;
    private int[] bestMatchRevNumCov;
    private int[] commonFwdMultCov;
    private int[] commonRevMultCov;
    private int[] commonFwdNumCov;
    private int[] commonRevNumCov;
    
    private int internalPos = 0;
    
    private int highestCoverage;
    
    //coverage Infos of track1
//    private HashMap<Integer, Integer> commonFwdMultTrack1;
//    private HashMap<Integer, Integer> commonRevMultTrack1;
    private int[] commonFwdMultCovTrack1;
    private int[] commonRevMultCovTrack1;
    //coverage Infos of track2
//    private HashMap<Integer, Integer> commonFwdMultTrack2;
//    private HashMap<Integer, Integer> commonRevMultTrack2;
    private int[] commonFwdMultCovTrack2;
    private int[] commonRevMultCovTrack2;
    
    public static byte PERFECT = 1;
    public static byte BM = 2;
    public static byte NERROR = 3;
    public static byte DIFF = 1;
    public static byte TRACK2 = 2;
    public static byte TRACK1 = 3;

    /**
     * Container for all different coverage types for a given interval.
     * If you want to set each coverage position separately you have to call
     * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have length 0.
     * @param leftBound left bound of the interval
     * @param rightBound right bound of the interval
     */
    public PersistantCoverage(int leftBound, int rightBound) {
        this(leftBound, rightBound, false);
    }

    /**
     * Container for all different coverage types for a given interval.
     * If you want to set each coverage position separately you have to call
     * <code>incArraysToIntervalSize()</code>. Otherwise the arrays all have length 0.
     * @param leftBound left bound of the interval
     * @param rightBound right bound of the interval
     * @param twoTracks true, if this is a container for storing the coverage of two tracks
     */
    public PersistantCoverage(int leftBound, int rightBound, boolean twoTracks) {
        this.twoTracks = twoTracks;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.finished = false;
        
        perfectFwdMultCov = new int[0];
        perfectRevMultCov = new int[0];
        perfectFwdNumCov = new int[0];
        perfectRevNumCov = new int[0];
        
        bestMatchFwdMultCov = new int[0];
        bestMatchFwdNumCov = new int[0];
        bestMatchRevMultCov = new int[0];
        bestMatchRevNumCov = new int[0];
        
        commonFwdMultCov = new int[0];
        commonFwdNumCov = new int[0];
        commonRevMultCov = new int[0];
        commonRevNumCov = new int[0];
        
        commonFwdMultCovTrack1 = new int[0];
        commonFwdMultCovTrack2 = new int[0];
        commonRevMultCovTrack1 = new int[0];
        commonRevMultCovTrack2 = new int[0];
        
//        perfectFwdMult = new HashMap<Integer, Integer>();
//        perfectFwdNum = new HashMap<Integer, Integer>();
//        perfectRevMult = new HashMap<Integer, Integer>();
//        perfectRevNum = new HashMap<Integer, Integer>();
//        
//        bestMatchFwdMult = new HashMap<Integer, Integer>();
//        bestMatchFwdNum = new HashMap<Integer, Integer>();
//        bestMatchRevMult = new HashMap<Integer, Integer>();
//        bestMatchRevNum = new HashMap<Integer, Integer>();
//
//        commonFwdMult = new HashMap<Integer, Integer>();
//        commonFwdNum = new HashMap<Integer, Integer>();
//        commonRevMult = new HashMap<Integer, Integer>();
//        commonRevNum = new HashMap<Integer, Integer>();

//        commonFwdMult = new HashMap<Integer, Integer>();
//        commonRevMult = new HashMap<Integer, Integer>();
//
//        commonFwdMultTrack1 = new HashMap<Integer, Integer>();
//        commonFwdMultTrack2 = new HashMap<Integer, Integer>();
//        commonRevMultTrack1 = new HashMap<Integer, Integer>();
//        commonRevMultTrack2 = new HashMap<Integer, Integer>();

    }

    public int getLeftBound() {
        return leftBound;
    }

    public int getRightBound() {
        return rightBound;
    }

    public void setLeftBound(int leftBound) {
        this.leftBound = leftBound;
    }

    public void setRightBound(int rightBound) {
        this.rightBound = rightBound;
    }

    public void setFinished() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean coversBounds(int left, int right) {
        if (this.leftBound == 0 && this.rightBound == 0) {
            return false;
        } else if (leftBound <= left && right <= rightBound) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the best match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchFwdMult(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        bestMatchFwdMultCov[this.internalPos] = coverage;
    }

    /**
     * Set the best match forward coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchFwdNum(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        bestMatchFwdNumCov[this.internalPos] = coverage;
    }

    /**
     * Set the best match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchRevMult(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        bestMatchRevMultCov[this.internalPos] = coverage;
    }

    /**
     * Set the best match reverse coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchRevNum(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        bestMatchRevNumCov[this.internalPos] = coverage;
    }

    /**
     * Set the common match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdMult(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonFwdMultCov[this.internalPos] = coverage;
    }

    /**
     * Set the common match forward coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdNum(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonFwdNumCov[this.internalPos] = coverage;
    }

    /**
     * Set the common match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevMult(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonRevMultCov[this.internalPos] = coverage;
    }

    /**
     * Set the common match reverse coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevNum(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonRevNumCov[this.internalPos] = coverage;
    }

    /**
     * Set the perfect match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectFwdMult(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        perfectFwdMultCov[this.internalPos] = coverage;
    }

    /**
     * Set the perfect match forward coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectFwdNum(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        perfectFwdNumCov[this.internalPos] = coverage;
    }

    /**
     * Set the perfect match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectRevMult(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        perfectRevMultCov[this.internalPos] = coverage;
    }

    /**
     * Set the perfect match reverse coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectRevNum(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        perfectRevNumCov[this.internalPos] = coverage;
    }
    


    /**
     * Set the common match forward coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdMultTrack1(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonFwdMultCovTrack1[this.internalPos] = coverage;
    }
    
    /**
     * Set the common match forward coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdMultTrack2(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonFwdMultCovTrack2[this.internalPos] = coverage;
    }
    
    /**
     * Set the common match reverse coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevMultTrack1(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonRevMultCovTrack1[this.internalPos] = coverage;
    }
    
    /**
     * Set the common match reverse coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevMultTrack2(int logPos, int coverage) throws ArrayIndexOutOfBoundsException {
        this.internalPos = logPos - this.leftBound;
        commonRevMultCovTrack2[this.internalPos] = coverage;
    }


    /**
     * Get the best match forward coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchFwdMult(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.bestMatchFwdMultCov.length && this.internalPos > 0) {
            return bestMatchFwdMultCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the best match forward coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchFwdNum(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.bestMatchFwdNumCov.length && this.internalPos > 0) {
            return bestMatchFwdNumCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the best match reverse coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchRevMult(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.bestMatchRevMultCov.length && this.internalPos > 0) {
            return bestMatchRevMultCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the best match reverse coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchRevNum(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.bestMatchRevNumCov.length && this.internalPos > 0) {
            return bestMatchRevNumCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match forward coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonFwdMult(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonFwdMultCov.length && this.internalPos > 0) {
            return commonFwdMultCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match forward coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonFwdNum(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonFwdNumCov.length && this.internalPos > 0) {
            return commonFwdNumCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match reverse coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonRevMult(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonRevMultCov.length && this.internalPos > 0) {
            return commonRevMultCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match reverse coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonRevNum(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonRevNumCov.length && this.internalPos > 0) {
            return commonRevNumCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the perfect match forward coverage with duplicates. If the position is not
     * covered 0 is returned.
     * @param logPos position whose coverage is needed
     */
    public int getPerfectFwdMult(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.perfectFwdMultCov.length && this.internalPos > 0) {
            return perfectFwdMultCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the perfect match forward coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getPerfectFwdNum(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.perfectFwdNumCov.length && this.internalPos > 0) {
            return perfectFwdNumCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the perfect match reverse coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getPerfectRevMult(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.perfectRevMultCov.length && this.internalPos > 0) {
            return perfectRevMultCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the perfect match reverse coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getPerfectRevNum(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.perfectRevNumCov.length && this.internalPos > 0) {
            return perfectRevNumCov[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match forward coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position whose coverage is needed
     */
    public int getCommonFwdMultTrack1(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonFwdMultCovTrack1.length && this.internalPos > 0) {
            return commonFwdMultCovTrack1[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match reverse coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position whose coverage is needed
     */
    public int getCommonRevMultTrack1(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonRevMultCovTrack1.length && this.internalPos > 0) {
            return commonRevMultCovTrack1[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match forward coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position whose coverage is needed
     */
    public int getCommonFwdMultTrack2(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonFwdMultCovTrack2.length && this.internalPos > 0) {
            return commonFwdMultCovTrack2[this.internalPos];
        } else {
            return 0;
        }
    }

    /**
     * Get the common match reverse coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position whose coverage is needed
     */
    public int getCommonRevMultTrack2(int logPos) {
        this.internalPos = logPos - this.leftBound;
        if (this.internalPos < this.commonRevMultCovTrack2.length && this.internalPos > 0) {
            return commonRevMultCovTrack2[this.internalPos];
        } else {
            return 0;
        }
    }
    
    /**
     * @return Get whole best match fwd mult coverage array for the given interval.
     */
    public int[] getBestMatchFwdMult() {
        return this.bestMatchFwdMultCov;
    }

    /**
     * @return Get whole best match fwd coverage array without replicates for 
     * the given interval.
     */    
    public int[] getBestMatchFwdNum() {
        return this.bestMatchFwdNumCov;
    }

    /**
     * @return Get whole best match rev mult coverage array for the given interval.
     */    
    public int[] getBestMatchRevMult() {
        return this.bestMatchRevMultCov;
    }

     /**
     * @return Get whole best match rev coverage array without replicates for 
     * the given interval.
     */   
    public int[] getBestMatchRevNum() {
        return this.bestMatchRevNumCov;
    }
    
    /**
     * @return Get whole common fwd mult coverage array for the given interval
     * of track 1
     */
    public int[] getCommonFwdMultCovTrack1() {
        return this.commonFwdMultCovTrack1;
    }

    /**
     * @return Get whole common fwd mult coverage array for the given interval
     * of track 2
     */    
    public int[] getCommonFwdMultCovTrack2() {
        return this.commonFwdMultCovTrack2;
    }

    /**
     * @return Get whole common rev mult coverage array for the given interval
     * of track 1
     */    
    public int[] getCommonRevMultCovTrack1() {
        return this.commonRevMultCovTrack1;
    }

     /**
     * @return Get whole common rev mult coverage array for the given interval
     * of track 2
     */   
    public int[] getCommonRevMultCovTrack2() {
        return this.commonRevMultCovTrack2;
    }


    /**
     * @param posToCheck the position which should be checked if it is in the 
     * bounds
     * @return true, if the posToCheck is within the bounds of this PersistantCoverage
     * and false otherwise
     */
    public boolean isInBounds(int posToCheck) {
        if (posToCheck < leftBound || posToCheck > rightBound && this.internalPos > 0) {
            return false;
        } else {
            return true;
        }
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

    
    public void setPerfectFwdMult(int[] perfectFwdMultCov) {
        this.perfectFwdMultCov = perfectFwdMultCov;
    }

    public void setPerfectRevMult(int[] perfectRevMultCov) {
        this.perfectRevMultCov = perfectRevMultCov;
    }

    public void setPerfectFwdNum(int[] perfectFwdNumCov) {
        this.perfectFwdNumCov = perfectFwdNumCov;
    }

    public void setPerfectRevNum(int[] perfectRevNumCov) {
        this.perfectRevNumCov = perfectRevNumCov;
    }

    public void setBestMatchFwdMult(int[] bestMatchFwdMultCov) {
        this.bestMatchFwdMultCov = bestMatchFwdMultCov;
    }

    public void setBestMatchRevMult(int[] bestMatchRevMultCov) {
        this.bestMatchRevMultCov = bestMatchRevMultCov;
    }

    public void setBestMatchFwdNum(int[] bestMatchFwdNumCov) {
        this.bestMatchFwdNumCov = bestMatchFwdNumCov;
    }

    public void setBestMatchRevNum(int[] bestMatchRevNumCov) {
        this.bestMatchRevNumCov = bestMatchRevNumCov;
    }

      public void setCommonFwdMult(int[] commonFwdMultCov) {
        this.commonFwdMultCov = commonFwdMultCov;
    }

    public void setCommonRevMult(int[] commonRevMultCov) {
        this.commonRevMultCov = commonRevMultCov;
    }

    public void setCommonFwdNum(int[] commonFwdNumCov) {
        this.commonFwdNumCov = commonFwdNumCov;
    }

    public void setCommonRevNum(int[] commonRevNumCov) {
        this.commonRevNumCov = commonRevNumCov;
    }

    public void setCommonFwdMultTrack1(int[] commonFwdMultCovTrack1) {
        this.commonFwdMultCovTrack1 = commonFwdMultCovTrack1;
    }

    public void setCommonRevMultTrack1(int[] commonRevMultCovTrack1) {
        this.commonRevMultCovTrack1 = commonRevMultCovTrack1;
    }

    public void setCommonFwdMultTrack2(int[] commonFwdMultCovTrack2) {
        this.commonFwdMultCovTrack2 = commonFwdMultCovTrack2;
    }

    public void setCommonRevMultTrack2(int[] commonRevMultCovTrack2) {
        this.commonRevMultCovTrack2 = commonRevMultCovTrack2;
    }
    
    /**
     * Increase the size of all arrays whose size is currently 0 to the interval
     * size covered by this PersistantCoverage. This behaviour prevents overwriting
     * coverage data already stored in this coverage object.
     */
    public void incArraysToIntervalSize() {
        int size = this.rightBound - this.leftBound + 1;
        if (this.perfectFwdMultCov.length == 0) {
            perfectFwdMultCov = new int[size];
        }
        if (this.perfectRevMultCov.length == 0) {
            perfectRevMultCov = new int[size];
        }
        if (this.perfectFwdNumCov.length == 0) {
            perfectFwdNumCov = new int[size];
        }
        if (this.perfectRevNumCov.length == 0) {
            perfectRevNumCov = new int[size];
        }
        
        if (this.bestMatchFwdMultCov.length == 0) {
            bestMatchFwdMultCov = new int[size];
        }
        if (this.bestMatchFwdNumCov.length == 0) {
            bestMatchFwdNumCov = new int[size];
        }
        if (this.bestMatchRevMultCov.length == 0) {
            bestMatchRevMultCov = new int[size];
        }
        if (this.bestMatchRevNumCov.length == 0) {
            bestMatchRevNumCov = new int[size];
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
        if (this.commonFwdMultCovTrack1.length == 0) {
            commonFwdMultCovTrack1 = new int[size];
        }
        if (this.commonRevMultCovTrack1.length == 0) {
            commonRevMultCovTrack1 = new int[size];
        }
        if (this.commonFwdMultCovTrack2.length == 0) {
            commonFwdMultCovTrack2 = new int[size];
        }
        if (this.commonRevMultCovTrack2.length == 0) {
            commonRevMultCovTrack2 = new int[size];
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
        if (this.commonFwdMultCov.length == 0) {
            commonFwdMultCov = new int[size];
        }
        if (this.commonFwdNumCov.length == 0) {
            commonFwdNumCov = new int[size];
        }
        if (this.commonRevMultCov.length == 0) {
            commonRevMultCov = new int[size];
        }
        if (this.commonRevNumCov.length == 0) {
            commonRevNumCov = new int[size];
        }
    }
}
