package de.cebitec.vamp.databackend.dataObjects;

import java.sql.Timestamp;

/**
 * A persistant reference containing an id, name, description, sequence
 * & timestamp of a reference.
 *
 * @author ddoppmeier
 */
public class PersistantReference {

    private Long id;
    private String name;
    private String description;
    private String sequence;
    private Timestamp timestamp;

    public PersistantReference(Long id, String name, String description, String sequence, Timestamp timestamp){
        this.id = id;
        this.name = name;
        this.description = description;
        this.sequence = sequence;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSequence() {
        return sequence;
    }

    public Timestamp getTimeStamp(){
        return timestamp;
    }

    @Override
    public String toString(){
        return name+" "+description;
    }

}
