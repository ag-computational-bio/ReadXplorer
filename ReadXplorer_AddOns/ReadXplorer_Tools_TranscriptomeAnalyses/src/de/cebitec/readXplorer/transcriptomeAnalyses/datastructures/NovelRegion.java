package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

/**
 *
 * @author jritter
 */
public class NovelRegion {

    private boolean isFWD, isFalsePositive, isSelected;
    private int start;
    private Integer dropoff;
    private Integer trackID;
    private String site;
    private Integer length;
    private String sequence;

    public NovelRegion(boolean isFWD, int start, Integer dropoff, String site, int length, String sequence, boolean isFP, boolean isSelected, Integer trackID) {
        this.isFWD = isFWD;
        this.start = start;
        this.dropoff = dropoff;
        this.trackID = trackID;
        this.site = site;
        this.length = length;
        this.sequence = sequence;
        this.isFalsePositive = isFP;
        this.isSelected = isSelected;
    }

    @Override
    public String toString() {
        return "Startpos: " + this.getPos() + "\tDirection " + this.isFWD + "\tDropoff Position: " + this.getDropOffPos() + "\tSite: " + this.getSite() + "\n";
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

    public Object getDropOffPos() {
        return this.dropoff;
    }

    public int getPos() {
        return this.start;
    }

    public boolean isFwdStrand() {
        return isFWD;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public boolean isFalsePositive() {
        return isFalsePositive;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
