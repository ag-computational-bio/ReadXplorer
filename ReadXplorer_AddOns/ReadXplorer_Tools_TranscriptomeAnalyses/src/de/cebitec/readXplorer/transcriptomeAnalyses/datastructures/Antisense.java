package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

/**
 *
 * @author jritter
 */
public class Antisense {

    private boolean isFwd;
    private Integer trackID;
    private String type;
    private Integer position;

    public Antisense(boolean isFwd, Integer trackID, String type, Integer position) {
        this.isFwd = isFwd;
        this.trackID = trackID;
        this.type = type;
        this.position = position;
    }

    public boolean isFwdStrand() {
        return this.isFwd;
    }

    public int getPos() {
        return this.position;
    }

    public Object getTrackId() {
        return this.trackID;
    }

    public Object getType() {
        return this.type;
    }
}
