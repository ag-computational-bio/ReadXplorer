package de.cebitec.readXplorer.databackend.dataObjects;

import java.sql.Timestamp;

/**
 * Since the RUN domain has been excluded a PersistantRun is not needed anymore!
 *
 * @author ddoppmeier
 */
@Deprecated
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
