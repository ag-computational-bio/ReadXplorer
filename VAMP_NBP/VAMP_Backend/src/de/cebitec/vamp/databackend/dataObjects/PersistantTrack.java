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
     * @param path path of the track, if it is a direct file access track
     * @param description
     * @param date creation date
     * @param refGenID
     * @param seqPairId  
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
     * @return Non-empty String, if this track is stored for direct file access.
     */
    public String getFilePath() {
        return path;
    }

    /**
     * @return The vamp database id of the reference genome
     */
    public int getRefGenID() {
        return refGenID;
    }

    /**
     * @return The timestamp of the creation time of this track
     */
    public Timestamp getTimestamp(){
        return date;
    }

    /**
     * @return Description string of this track
     */
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
     * @return True, if this track is stored within the database, false, if it is
     * stored for direct file access.
     */
    public boolean isDbUsed() {
        return this.path.isEmpty();
    }

    /**
     * @return Returns the description of this track
     */
    @Override
    public String toString(){
        return description;
    }

}
