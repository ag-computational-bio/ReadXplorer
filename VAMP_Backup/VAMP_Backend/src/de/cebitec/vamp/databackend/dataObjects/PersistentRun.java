package de.cebitec.vamp.databackend.dataObjects;

import java.sql.Timestamp;

/**
 *
 * @author ddoppmeier
 */
public class PersistentRun {

    private long id;
    private String description;
    private Timestamp timeStamp;

    public PersistentRun(long id, String description, Timestamp timeStamp){
        this.id = id;
        this.description = description;
        this.timeStamp = timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public long getId() {
        return id;
    }

    public Timestamp getTimestamp(){
        return timeStamp;
    }

}
