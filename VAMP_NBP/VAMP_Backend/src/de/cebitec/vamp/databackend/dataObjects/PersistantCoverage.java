package de.cebitec.vamp.databackend.dataObjects;

import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public class PersistantCoverage {
    
    //2 for fwd (mult = all & num = without duplicates), and 2 rev
    
    private int leftBound;
    private int rightBound;
    private boolean finished;
    private boolean twoTracks = false;
    private HashMap<Integer, Integer> bestMatchFwdMult;
    private HashMap<Integer, Integer> bestMatchFwdNum;
    private HashMap<Integer, Integer> bestMatchRevMult;
    private HashMap<Integer, Integer> bestMatchRevNum;
    private HashMap<Integer, Integer> commonFwdMult;
    private HashMap<Integer, Integer> commonFwdNum;
    private HashMap<Integer, Integer> commonRevMult;
    private HashMap<Integer, Integer> commonRevNum;
    private HashMap<Integer, Integer> perfectFwdMult;
    private HashMap<Integer, Integer> perfectFwdNum;
    private HashMap<Integer, Integer> perfectRevMult;
    private HashMap<Integer, Integer> perfectRevNum;
    private int highestCoverage;
    
    //coverage Infos of track1
    private HashMap<Integer, Integer> commonFwdMultTrack1;
    private HashMap<Integer, Integer> commonRevMultTrack1;
    //coverage Infos of track2
    private HashMap<Integer, Integer> commonFwdMultTrack2;
    private HashMap<Integer, Integer> commonRevMultTrack2;
    
    public static int PERFECT = 1;
    public static int BM = 2;
    public static int NERROR = 3;
    public static int DIFF = 1;
    public static int TRACK2 = 2;
    public static int TRACK1 = 3;

    public PersistantCoverage(int leftBound, int rightBound) {
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.finished = false;
        this.twoTracks = false;
        bestMatchFwdMult = new HashMap<Integer, Integer>();
        bestMatchFwdNum = new HashMap<Integer, Integer>();
        bestMatchRevMult = new HashMap<Integer, Integer>();
        bestMatchRevNum = new HashMap<Integer, Integer>();

        commonFwdMult = new HashMap<Integer, Integer>();
        commonFwdNum = new HashMap<Integer, Integer>();
        commonRevMult = new HashMap<Integer, Integer>();
        commonRevNum = new HashMap<Integer, Integer>();

        perfectFwdMult = new HashMap<Integer, Integer>();
        perfectFwdNum = new HashMap<Integer, Integer>();
        perfectRevMult = new HashMap<Integer, Integer>();
        perfectRevNum = new HashMap<Integer, Integer>();
    }

    public PersistantCoverage(int leftBound, int rightBound, boolean twoTracks) {
        this.twoTracks = twoTracks;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.finished = false;

        commonFwdMult = new HashMap<Integer, Integer>();
        commonRevMult = new HashMap<Integer, Integer>();

        commonFwdMultTrack1 = new HashMap<Integer, Integer>();
        commonFwdMultTrack2 = new HashMap<Integer, Integer>();

        commonRevMultTrack1 = new HashMap<Integer, Integer>();
        commonRevMultTrack2 = new HashMap<Integer, Integer>();

    }

    public int getLeftBound() {
        return leftBound;
    }

    public int getRightBound() {
        return rightBound;
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
    public void setBestMatchFwdMult(int logPos, int coverage) {
        bestMatchFwdMult.put(logPos, coverage);
    }

    /**
     * Set the best match forward coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchFwdNum(int logPos, int coverage) {
        bestMatchFwdNum.put(logPos, coverage);
    }

    /**
     * Set the best match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchRevMult(int logPos, int coverage) {
        bestMatchRevMult.put(logPos, coverage);
    }

    /**
     * Set the best match reverse coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setBestMatchRevNum(int logPos, int coverage) {
        bestMatchRevNum.put(logPos, coverage);
    }

    /**
     * Set the common match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdMult(int logPos, int coverage) {
        commonFwdMult.put(logPos, coverage);
    }

    /**
     * Set the common match forward coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdNum(int logPos, int coverage) {
        commonFwdNum.put(logPos, coverage);
    }

    /**
     * Set the common match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevMult(int logPos, int coverage) {
        commonRevMult.put(logPos, coverage);
    }

    /**
     * Set the common match reverse coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevNum(int logPos, int coverage) {
        commonRevNum.put(logPos, coverage);
    }

    /**
     * Set the perfect match forward coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectFwdMult(int logPos, int coverage) {
        perfectFwdMult.put(logPos, coverage);
    }

    /**
     * Set the perfect match forward coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectFwdNum(int logPos, int coverage) {
        perfectFwdNum.put(logPos, coverage);
    }

    /**
     * Set the perfect match reverse coverage with duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectRevMult(int logPos, int coverage) {
        perfectRevMult.put(logPos, coverage);
    }

    /**
     * Set the perfect match reverse coverage WITHOUT duplicates.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setPerfectRevNum(int logPos, int coverage) {
        perfectRevNum.put(logPos, coverage);
    }
    


    /**
     * Set the common match forward coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdMultTrack1(int logPos, int coverage) {
        commonFwdMultTrack1.put(logPos, coverage);
    }
    
    /**
     * Set the common match forward coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonFwdMultTrack2(int logPos, int coverage) {
        commonFwdMultTrack2.put(logPos, coverage);
    }
    
    /**
     * Set the common match reverse coverage with duplicates for track 1 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevMultTrack1(int logPos, int coverage) {
        commonRevMultTrack1.put(logPos, coverage);
    }
    
    /**
     * Set the common match reverse coverage with duplicates for track 2 in a
     * two track analysis case.
     * @param logPos position
     * @param coverage coverage value 
     */
    public void setCommonRevMultTrack2(int logPos, int coverage) {
        commonRevMultTrack2.put(logPos, coverage);
    }


    /**
     * Get the best match forward coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchFwdMult(int logPos) {
        if (bestMatchFwdMult.containsKey(logPos)) {
            return bestMatchFwdMult.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the best match forward coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchFwdNum(int logPos) {
        if (bestMatchFwdNum.containsKey(logPos)) {
            return bestMatchFwdNum.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the best match reverse coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchRevMult(int logPos) {
        if (bestMatchRevMult.containsKey(logPos)) {
            return bestMatchRevMult.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the best match reverse coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getBestMatchRevNum(int logPos) {
        if (bestMatchRevNum.containsKey(logPos)) {
            return bestMatchRevNum.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the common match forward coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonFwdMult(int logPos) {
        if (commonFwdMult.containsKey(logPos)) {
            return commonFwdMult.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the common match forward coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonFwdNum(int logPos) {
        if (commonFwdNum.containsKey(logPos)) {
            return commonFwdNum.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the common match reverse coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonRevMult(int logPos) {
        if (commonRevMult.containsKey(logPos)) {
            return commonRevMult.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the common match reverse coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getCommonRevNum(int logPos) {
        if (commonRevNum.containsKey(logPos)) {
            return commonRevNum.get(logPos);
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
        if (perfectFwdMult.containsKey(logPos)) {
            return perfectFwdMult.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the perfect match forward coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getPerfectFwdNum(int logPos) {
        if (perfectFwdNum.containsKey(logPos)) {
            return perfectFwdNum.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the perfect match reverse coverage with duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getPerfectRevMult(int logPos) {
        if (perfectRevMult.containsKey(logPos)) {
            return perfectRevMult.get(logPos);
        } else {
            return 0;
        }
    }

    /**
     * Get the perfect match reverse coverage WITHOUT duplicates.
     * @param logPos position whose coverage is needed
     */
    public int getPerfectRevNum(int logPos) {
        if (perfectRevNum.containsKey(logPos)) {
            return perfectRevNum.get(logPos);
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
        if (commonFwdMultTrack1.containsKey(logPos)) {
            return commonFwdMultTrack1.get(logPos);
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
        if (commonRevMultTrack1.containsKey(logPos)) {
            return commonRevMultTrack1.get(logPos);
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
        if (commonFwdMultTrack2.containsKey(logPos)) {
            return commonFwdMultTrack2.get(logPos);
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
        if (commonRevMultTrack2.containsKey(logPos)) {
            return commonRevMultTrack2.get(logPos);
        } else {
            return 0;
        }
    }


    /**
     * @param posToCheck the position which should be checked if it is in the 
     * bounds
     * @return true, if the posToCheck is within the bounds of this PersistantCoverage
     * and false otherwise
     */
    public boolean isInBounds(int posToCheck) {
        if (posToCheck < leftBound || posToCheck > rightBound) {
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

    public void setTwoTracks(boolean twoTracks) {
        this.twoTracks = twoTracks;
    }

    /**
     * Getter for the highest coverage for automatic scaling.
     */
    public int getHighestCoverage() {
        return highestCoverage;
    }

    /**
     * Setter for the highest coverage for automatic scaling.
     */
    public void setHighestCoverage(int highestCoverage) {
        this.highestCoverage = highestCoverage;
    }
    

}
