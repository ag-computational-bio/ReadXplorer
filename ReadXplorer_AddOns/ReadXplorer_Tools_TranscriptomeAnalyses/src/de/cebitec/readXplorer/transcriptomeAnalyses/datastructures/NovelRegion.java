package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;

/**
 *
 * @author jritter
 */
public class NovelRegion extends TrackChromResultEntry {

    private boolean isFWD, isFalsePositive, isSelected;
    private int start;
    private int dropoff;
    private String site;
    private int length;
    private String sequence;

    public NovelRegion(boolean isFWD, int start, Integer dropoff, String site, int length, String sequence, boolean isFP, boolean isSelected, int trackId, int chromId) {
        super(trackId, chromId);
        this.isFWD = isFWD;
        this.start = start;
        this.dropoff = dropoff;
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

    /**
     * Strand information on which the novel region is located.
     *
     * @return <true> if forward strand else <false>.
     */
    public boolean isFWD() {
        return isFWD;
    }

    public int getDropOffPos() {
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

    public int getLength() {
        return length;
    }

    public String getSequence() {
        return sequence;
    }

    /**
     * Information about the correctness of the detection.
     *
     * @return <true> if it is a false positve detection of a novel region.
     * Default is <false>.
     */
    public boolean isFalsePositive() {
        return isFalsePositive;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
