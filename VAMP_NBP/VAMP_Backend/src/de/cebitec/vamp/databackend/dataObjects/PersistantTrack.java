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
     * check if the given track is equal to this one 
     */
    @Override
    public boolean equals(Object o) {
        
        if (o instanceof PersistantTrack) {
            PersistantTrack otrack = (PersistantTrack) o;
            return ( 
               otrack.description.equals(this.description)
                && otrack.date.equals(this.date)
                && (otrack.id==this.id)
                && (otrack.path.equals(this.path))
                && (otrack.refGenID==this.refGenID)
                && (otrack.seqPairId==this.seqPairId)
               );
                    
        }
        else return super.equals(o);
    }

}
