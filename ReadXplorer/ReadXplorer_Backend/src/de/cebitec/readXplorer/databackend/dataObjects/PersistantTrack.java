package de.cebitec.readXplorer.databackend.dataObjects;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private int activeChromId;
    private final int readPairId;

    /**
     * Data storage for a track.
     * @param id unique db id of the track
     * @param path path of the track, if it is a direct file access track
     * @param description
     * @param date creation date
     * @param refGenID id of the reference genome
     * @param activeChromId id of the currently active chromosome.
     * @param readPairId  
     */
    public PersistantTrack(int id, String path, String description, Timestamp date, int refGenID, int activeChromId, int readPairId) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.refGenID = refGenID;
        this.activeChromId = activeChromId;
        this.readPairId = readPairId;
        path = path == null ? "" : path; //ensure that path is not null
        this.path = path;
    }

    /**
     * Data storage for a track. Use this constructor, if the currently active
     * chromosome cannot be set yet.
     * @param id unique db id of the track
     * @param path path of the track, if it is a direct file access track
     * @param description
     * @param date creation date
     * @param refGenID id of the reference genome
     * @param readPairId
     */
    public PersistantTrack(int id, String path, String description, Timestamp date, int refGenID, int readPairId) {
        this(id, path, description, date, refGenID, -1, readPairId);
    }

    /**
     * @return Unique id of this track.
     */
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
     * @return The readXplorer database id of the reference genome
     */
    public int getRefGenID() {
        return refGenID;
    }

    /**
     * @return Id of the currently activate chromosome. Might be -1, if it was 
     * not set yet.
     */
    public int getActiveChromId() {
        return this.activeChromId;
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
    public int getReadPairId() {
        return readPairId;
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
    
    /* 
     * need this to use PersistantReference class as key for HashMap 
     * @see http://stackoverflow.com/questions/27581/overriding-equals-and-hashcode-in-java
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
       return id;
   }
    
    
    /**
     * Checks if the given track is equal to this one.
     * @param o object to compare to this one
     */
    @Override
    public boolean equals(Object o) {
        
        if (o instanceof PersistantTrack) {
            PersistantTrack otrack = (PersistantTrack) o;
            return ( 
               otrack.getDescription().equals(this.description)
                    && otrack.getTimestamp().equals(this.date)
                    && (otrack.getId() == this.id)
                    && (otrack.getFilePath().equals(this.path))
                    && (otrack.getRefGenID() == this.refGenID)
                    && (otrack.getActiveChromId() == this.activeChromId)
                    && (otrack.getReadPairId() == this.readPairId));
        } else { 
            return super.equals(o); 
        }
    }
    
    /**
     * Generates a list of track names from a collection of persistant tracks.
     * @param tracks the tracks whose descriptions need to be returned in a list
     * @return The list of track names from the given collection of persistant tracks.
     */
    public static List<String> generateTrackDescriptionList(Collection<PersistantTrack> tracks) {
        List<String> trackNameList = new ArrayList<>();
        for (PersistantTrack track : tracks) {
            trackNameList.add(track.getDescription());
        }
        return trackNameList;
    }
    
    /**
     * Checks if any of the tracks in the list is a track completely stored in
     * the DB.
     * @param tracks the list of tracks to check
     * @return true, if there is at least one track completely stored in the DB,
     * false otherwise
     */
    public static boolean checkForDBTrack(List<PersistantTrack> tracks) {
        for (PersistantTrack track : tracks) {
            if (track.isDbUsed()) {
                return true;
            }
        }
        return false;
    }

}
