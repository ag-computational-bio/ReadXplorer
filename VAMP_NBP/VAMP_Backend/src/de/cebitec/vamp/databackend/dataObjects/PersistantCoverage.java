package de.cebitec.vamp.databackend.dataObjects;

import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public class PersistantCoverage {

    //Normally only mult is needed, but not num!
    
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

    public void setBestMatchFwdMult(int logPos, int value) {
        bestMatchFwdMult.put(logPos, value);
    }

    public void setBestMatchFwdNum(int logPos, int value) {
        bestMatchFwdNum.put(logPos, value);
    }

    public void setBestMatchRevMult(int logPos, int value) {
        bestMatchRevMult.put(logPos, value);
    }

    public void setBestMatchRevNum(int logPos, int value) {
        bestMatchRevNum.put(logPos, value);
    }

    public void setCommonFwdMult(int logPos, int value) {
        commonFwdMult.put(logPos, value);
    }

    public void setCommonFwdNum(int logPos, int value) {
        commonFwdNum.put(logPos, value);
    }

    public void setCommonRevMult(int logPos, int value) {
        commonRevMult.put(logPos, value);
    }

    public void setCommonRevNum(int logPos, int value) {
        commonRevNum.put(logPos, value);
    }

    public void setPerfectFwdMult(int logPos, int value) {
        perfectFwdMult.put(logPos, value);
    }

    public void setPerfectFwdNum(int logPos, int value) {
        perfectFwdNum.put(logPos, value);
    }

    public void setPerfectRevMult(int logPos, int value) {
        perfectRevMult.put(logPos, value);
    }

    public void setPerfectRevNum(int logPos, int value) {
        perfectRevNum.put(logPos, value);
    }
    


     public void setCommonFwdMultTrack1(int logPos, int value) {
        commonFwdMultTrack1.put(logPos, value);
    }
    public void setCommonFwdMultTrack2(int logPos, int value) {
        commonFwdMultTrack2.put(logPos, value);
    }
    public void setCommonRevMultTrack1(int logPos, int value) {
        commonRevMultTrack1.put(logPos, value);
    }
    public void setCommonRevMultTrack2(int logPos, int value) {
        commonRevMultTrack2.put(logPos, value);
    }


    public int getBestMatchFwdMult(int logPos) {
        if (bestMatchFwdMult.containsKey(logPos)) {
            return bestMatchFwdMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getBestMatchFwdNum(int logPos) {
        if (bestMatchFwdNum.containsKey(logPos)) {
            return bestMatchFwdNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getBestMatchRevMult(int logPos) {
        if (bestMatchRevMult.containsKey(logPos)) {
            return bestMatchRevMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getBestMatchRevNum(int logPos) {
        if (bestMatchRevNum.containsKey(logPos)) {
            return bestMatchRevNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonFwdMult(int logPos) {
        if (commonFwdMult.containsKey(logPos)) {
            return commonFwdMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonFwdNum(int logPos) {
        if (commonFwdNum.containsKey(logPos)) {
            return commonFwdNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonRevMult(int logPos) {
        if (commonRevMult.containsKey(logPos)) {
            return commonRevMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonRevNum(int logPos) {
        if (commonRevNum.containsKey(logPos)) {
            return commonRevNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getPerfectFwdMult(int logPos) {
        if (perfectFwdMult.containsKey(logPos)) {
            return perfectFwdMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getPerfectFwdNum(int logPos) {
        if (perfectFwdNum.containsKey(logPos)) {
            return perfectFwdNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getPerfectRevMult(int logPos) {
        if (perfectRevMult.containsKey(logPos)) {
            return perfectRevMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getPerfectRevNum(int logPos) {
        if (perfectRevNum.containsKey(logPos)) {
            return perfectRevNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonFwdMultTrack1(int logPos) {
        if (commonFwdMultTrack1.containsKey(logPos)) {
            return commonFwdMultTrack1.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonRevMultTrack1(int logPos) {
        if (commonRevMultTrack1.containsKey(logPos)) {
            return commonRevMultTrack1.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonFwdMultTrack2(int logPos) {
        if (commonFwdMultTrack2.containsKey(logPos)) {
            return commonFwdMultTrack2.get(logPos);
        } else {
            return 0;
        }
    }

    public int getCommonRevMultTrack2(int logPos) {
        if (commonRevMultTrack2.containsKey(logPos)) {
            return commonRevMultTrack2.get(logPos);
        } else {
            return 0;
        }
    }



    public boolean isInBounds(int posToCheck) {
        if (posToCheck < leftBound || posToCheck > rightBound) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isTwoTracks() {
        return twoTracks;
    }

    public void setTwoTracks(boolean twoTracks) {
        this.twoTracks = twoTracks;
    }
    

}
