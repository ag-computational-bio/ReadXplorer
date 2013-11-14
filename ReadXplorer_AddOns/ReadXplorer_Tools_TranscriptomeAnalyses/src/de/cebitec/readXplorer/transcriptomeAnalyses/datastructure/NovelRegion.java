/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.datastructure;

/**
 *
 * @author jritter
 */
public class NovelRegion {

    private boolean isFWD;
    private int flag;
    private int start;
    private Integer offset;
    private Integer trackID;

    public NovelRegion(boolean isFWD, int start, Integer offset, Integer trackID) {
        this.isFWD = isFWD;
        this.start = start;
        this.offset = offset;
        this.trackID = trackID;
    }

    public boolean isFWD() {
        return isFWD;
    }

    public void setFWD(boolean isFWD) {
        this.isFWD = isFWD;
    }

    public Object getTrackId() {
        return this.trackID;
    }

    public Object getOffset() {
        return this.offset;
    }

    public int getPos() {
        return this.start;
    }

    public boolean isFwdStrand() {
        return isFWD;
    }
}
