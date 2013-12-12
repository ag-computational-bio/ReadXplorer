package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;

/**
 *
 * @author jritter
 */
public class NovelRegion extends TrackChromResultEntry {

    private boolean isFWD;
    private int flag;
    private int start;
    private Integer offset;

    public NovelRegion(boolean isFWD, int start, Integer offset, int trackId, int chromId) {
        super(trackId, chromId);
        this.isFWD = isFWD;
        this.start = start;
        this.offset = offset;
    }

    public boolean isFWD() {
        return isFWD;
    }

    public void setFWD(boolean isFWD) {
        this.isFWD = isFWD;
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
