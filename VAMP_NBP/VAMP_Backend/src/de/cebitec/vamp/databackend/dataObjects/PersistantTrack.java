package de.cebitec.vamp.databackend.dataObjects;

import java.sql.Timestamp;

/**
 * Data storage for a track.
 * 
 * @author ddoppmeier, rhilker
 */
public class PersistantTrack {

    private int id;
    private String path;
    private String description;
    private Timestamp date;
    private int refGenID;
    private final int seqPairId;

    /**
     * Data storage for a track.
     * @param id
     * is loaded via direct file access
     * @param description
     * @param date creation date
     * @param refGenID 
     */
    public PersistantTrack(int id, String path, String description, Timestamp date, int refGenID, int seqPairId) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.refGenID = refGenID;
        this.seqPairId = seqPairId;
        path = path == null ? "" : path; //ensure that path is not null
        this.path = path;
    }

    public int getId() {
        return id;
    }

    /**
     * @return non-empty String, if this track is stored for direct file access.
     */
    public String getFilePath() {
        return path;
    }

    public int getRefGenID() {
        return refGenID;
    }

    public Timestamp getTimestamp(){
        return date;
    }

    public String getDescription(){
        return description;
    }

    /**
     * @return The id of this paired data track.
     */
    public int getSeqPairId() {
        return seqPairId;
    }
    
    /**
     * @return true, if this track is stored within the database, false, if it is
     * stored for direct file access.
     */
    public boolean isDbUsed() {
        return this.path.isEmpty();
    }

    @Override
    public String toString(){
        return description;
    }

}
