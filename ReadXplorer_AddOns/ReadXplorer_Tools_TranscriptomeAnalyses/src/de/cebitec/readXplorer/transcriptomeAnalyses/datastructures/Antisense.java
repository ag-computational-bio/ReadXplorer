package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;

/**
 *
 * @author jritter
 */
public class Antisense extends TrackChromResultEntry {

    private boolean isFwd;
    private String type;
    private Integer position;

    public Antisense(boolean isFwd, int trackID, int chromId, String type, Integer position) {
        super (trackID, chromId);
        this.isFwd = isFwd;
        this.type = type;
        this.position = position;
    }

    public boolean isFwdStrand() {
        return this.isFwd;
    }

    public int getPos() {
        return this.position;
    }

    public Object getType() {
        return this.type;
    }
}
