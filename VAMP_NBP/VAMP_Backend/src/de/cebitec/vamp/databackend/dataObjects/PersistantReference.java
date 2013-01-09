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
    private int refLength;
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
        this.refLength = sequence.length();
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
     * @return the length of the reference sequence
     */
    public int getRefLength() {
        return refLength;
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
     * check if the given reference genome is equal to this one 
     */
    @Override
    public boolean equals(Object o) {
        
        if (o instanceof PersistantReference) {
            PersistantReference ogenome = (PersistantReference) o;
            return ( 
               ogenome.description.equals(this.description)
                && ogenome.name.equals(this.name)
                && (ogenome.id==this.id)
                && (ogenome.refLength==this.refLength)
                && ogenome.timestamp.equals(this.timestamp)
                
                //for current purposes we do not need to compare the sequence, 
                //in most cases the id should be enoght
                //&& (ogenome.sequence...) 
                    
               );
                    
        }
        else return super.equals(o);
    }

}
