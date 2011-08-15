package de.cebitec.vamp.databackend.dataObjects;

import java.sql.Timestamp;

/**
 *
 * @author ddoppmeier
 */
public class PersistantTrack {

    private Long id;
    private String description;
    private Timestamp date;
    private Long refGenID;
 //   private Long runID; //excluded since RUN domain is excluded from VAMP

    public PersistantTrack(Long id, String description, Timestamp date, Long refGenID){//, Long runID) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.refGenID = refGenID;
//        this.runID = runID;
    }

    public Long getId() {
        return id;
    }

    public Long getRefGenID() {
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
