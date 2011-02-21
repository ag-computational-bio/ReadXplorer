package de.cebitec.vamp.databackend.dataObjects;

import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public class PersistantCoverage {

    private int leftBound;
    private int rightBound;
    private boolean finished;
    private boolean twoTracks = false;
    private HashMap<Integer, Integer> bmFwMult;
    private HashMap<Integer, Integer> bmFwNum;
    private HashMap<Integer, Integer> bmRvMult;
    private HashMap<Integer, Integer> bmRvNum;
    private HashMap<Integer, Integer> nFwMult;
    private HashMap<Integer, Integer> nFwNum;
    private HashMap<Integer, Integer> nRvMult;
    private HashMap<Integer, Integer> nRvNum;
    private HashMap<Integer, Integer> zFwMult;
    private HashMap<Integer, Integer> zFwNum;
    private HashMap<Integer, Integer> zRvMult;
    private HashMap<Integer, Integer> zRvNum;
    //coverage Infos of track1

    private HashMap<Integer, Integer> nFwMultTrack1;
    private HashMap<Integer, Integer> nRvMultTrack1;
    //coverage Infos of track2

    private HashMap<Integer, Integer> nFwMultTrack2;
    private HashMap<Integer, Integer> nRvMultTrack2;
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
        bmFwMult = new HashMap<Integer, Integer>();
        bmFwNum = new HashMap<Integer, Integer>();
        bmRvMult = new HashMap<Integer, Integer>();
        bmRvNum = new HashMap<Integer, Integer>();

        nFwMult = new HashMap<Integer, Integer>();
        nFwNum = new HashMap<Integer, Integer>();
        nRvMult = new HashMap<Integer, Integer>();
        nRvNum = new HashMap<Integer, Integer>();

        zFwMult = new HashMap<Integer, Integer>();
        zFwNum = new HashMap<Integer, Integer>();
        zRvMult = new HashMap<Integer, Integer>();
        zRvNum = new HashMap<Integer, Integer>();
    }

    public PersistantCoverage(int leftBound, int rightBound, boolean twoTracks) {
        this.twoTracks = twoTracks;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.finished = false;


        nFwMult = new HashMap<Integer, Integer>();
        nFwNum = new HashMap<Integer, Integer>();
        nRvMult = new HashMap<Integer, Integer>();
        nRvNum = new HashMap<Integer, Integer>();



        nFwMultTrack1 = new HashMap<Integer, Integer>();
        nFwMultTrack2 = new HashMap<Integer, Integer>();
        nRvMultTrack1 = new HashMap<Integer, Integer>();
        nRvMultTrack2 = new HashMap<Integer, Integer>();



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

    public void setBmFwMult(int logPos, int value) {
        bmFwMult.put(logPos, value);
    }

    public void setBmFwNum(int logPos, int value) {
        bmFwNum.put(logPos, value);
    }

    public void setBmRvMult(int logPos, int value) {
        bmRvMult.put(logPos, value);
    }

    public void setBmRvNum(int logPos, int value) {
        bmRvNum.put(logPos, value);
    }

    public void setnFwMult(int logPos, int value) {
        nFwMult.put(logPos, value);
    }

    public void setnFwNum(int logPos, int value) {
        nFwNum.put(logPos, value);
    }

    public void setnRvMult(int logPos, int value) {
        nRvMult.put(logPos, value);
    }

    public void setnRvNum(int logPos, int value) {
        nRvNum.put(logPos, value);
    }

    public void setzFwMult(int logPos, int value) {
        zFwMult.put(logPos, value);
    }

    public void setzFwNum(int logPos, int value) {
        zFwNum.put(logPos, value);
    }

    public void setzRvMult(int logPos, int value) {
        zRvMult.put(logPos, value);
    }

    public void setzRvNum(int logPos, int value) {
        zRvNum.put(logPos, value);
    }
    


     public void setNFwMultTrack1(int logPos, int value) {
        nFwMultTrack1.put(logPos, value);
    }
    public void setNFwMultTrack2(int logPos, int value) {
        nFwMultTrack2.put(logPos, value);
    }
    public void setNRvMultTrack1(int logPos, int value) {
        nRvMultTrack1.put(logPos, value);
    }
    public void setNRvMultTrack2(int logPos, int value) {
        nRvMultTrack2.put(logPos, value);
    }


    public int getBmFwMult(int logPos) {
        if (bmFwMult.containsKey(logPos)) {
            return bmFwMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getBmFwNum(int logPos) {
        if (bmFwNum.containsKey(logPos)) {
            return bmFwNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getBmRvMult(int logPos) {
        if (bmRvMult.containsKey(logPos)) {
            return bmRvMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getBmRvNum(int logPos) {
        if (bmRvNum.containsKey(logPos)) {
            return bmRvNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getnFwMult(int logPos) {
        if (nFwMult.containsKey(logPos)) {
            return nFwMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getnFwNum(int logPos) {
        if (nFwNum.containsKey(logPos)) {
            return nFwNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getnRvMult(int logPos) {
        if (nRvMult.containsKey(logPos)) {
            return nRvMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getnRvNum(int logPos) {
        if (nRvNum.containsKey(logPos)) {
            return nRvNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getzFwMult(int logPos) {
        if (zFwMult.containsKey(logPos)) {
            return zFwMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getzFwNum(int logPos) {
        if (zFwNum.containsKey(logPos)) {
            return zFwNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getzRvMult(int logPos) {
        if (zRvMult.containsKey(logPos)) {
            return zRvMult.get(logPos);
        } else {
            return 0;
        }
    }

    public int getzRvNum(int logPos) {
        if (zRvNum.containsKey(logPos)) {
            return zRvNum.get(logPos);
        } else {
            return 0;
        }
    }

    public int getNFwMultTrack1(int logPos) {
        if (nFwMultTrack1.containsKey(logPos)) {
            return nFwMultTrack1.get(logPos);
        } else {
            return 0;
        }
    }

    public int getNRvMultTrack1(int logPos) {
        if (nRvMultTrack1.containsKey(logPos)) {
            return nRvMultTrack1.get(logPos);
        } else {
            return 0;
        }
    }

    public int getNFwMultTrack2(int logPos) {
        if (nFwMultTrack2.containsKey(logPos)) {
            return nFwMultTrack2.get(logPos);
        } else {
            return 0;
        }
    }

    public int getNRvMultTrack2(int logPos) {
        if (nRvMultTrack2.containsKey(logPos)) {
            return nRvMultTrack2.get(logPos);
        } else {
            return 0;
        }
    }



    private boolean isInBounds(int logPos) {
        if (logPos < leftBound || logPos > rightBound) {
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
