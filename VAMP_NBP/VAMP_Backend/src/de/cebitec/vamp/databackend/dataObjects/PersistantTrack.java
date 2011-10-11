package de.cebitec.vamp.databackend.dataObjects;

import java.sql.Timestamp;

/**
 *
 * @author ddoppmeier
 */
public class PersistantTrack {

    private int id;
    private String description;
    private Timestamp date;
    private int refGenID;
 //   private Long runID; //excluded since RUN domain is excluded from VAMP

    public PersistantTrack(int id, String description, Timestamp date, int refGenID){//, Long runID) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.refGenID = refGenID;
//        this.runID = runID;
    }

    public int getId() {
        return id;
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

    @Override
    public String toString(){
        return description;
    }

//    public Long getRunID() {
//        return runID;
//    } //excluded since RUN domain is excluded from VAMP

}
