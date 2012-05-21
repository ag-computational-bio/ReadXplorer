package de.cebitec.vamp.databackend.dataObjects;

import java.sql.Timestamp;

/**
 * A persistant reference containing an id, name, description, sequence
 * & timestamp of a reference genome.
 *
 * @author ddoppmeier
 */
public class PersistantReference {

    private int id;
    private String name;
    private String description;
    private String sequence;
    private Timestamp timestamp;

    /**
     * Data holder for a reference genome.
     * @param id The database id of the reference.
     * @param name The name of the reference.
     * @param description The additional description of the reference.
     * @param sequence The genome sequence of the reference.
     * @param timestamp The insertion timestamp of the reference.
     */
    public PersistantReference(int id, String name, String description, String sequence, Timestamp timestamp){
        this.id = id;
        this.name = name;
        this.description = description;
        this.sequence = sequence;
        this.timestamp = timestamp;
    }

    /**
     * @return The additional description of the reference.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The database id of the reference.
     */
    public int getId() {
        return id;
    }

    /**
     * @return The name of the reference.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The genome sequence of the reference.
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * @return The insertion timestamp of the reference.
     */
    public Timestamp getTimeStamp(){
        return timestamp;
    }

    @Override
    public String toString(){
        return name+" "+description;
    }

}
