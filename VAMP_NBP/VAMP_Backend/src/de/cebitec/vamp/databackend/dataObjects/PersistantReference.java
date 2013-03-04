package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
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
     * Data holder for a reference genome. This constructor should only be used,
     * if it is known, that the sequence is definitely needed. Otherwise the 
     * sequence can still be querried from this PersistantReference (lazy 
     * evaluation).
     * @param id The database id of the reference.
     * @param name The name of the reference.
     * @param description The additional description of the reference.
     * @param sequence The genome sequence of the reference.
     * @param timestamp The insertion timestamp of the reference.
     */
    public PersistantReference(int id, String name, String description, String sequence, Timestamp timestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sequence = sequence;
        this.refLength = sequence.length();
        this.timestamp = timestamp;
    }
    
    /**
     * Data holder for a reference genome. The sequence is not needed here, since
     * it is gathered, if needed (lazy evaluation).
     * @param id The database id of the reference.
     * @param name The name of the reference.
     * @param description The additional description of the reference.
     * @param timestamp The insertion timestamp of the reference.
     */
    public PersistantReference(int id, String name, String description, Timestamp timestamp){
        this(id, name, description, "", timestamp);
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
        if (this.sequence.isEmpty()) {
            this.sequence = ProjectConnector.getInstance().getRefGenomeConnector(id).getRefSequence();
        }
        return sequence;
    }

    /**
     * @return the length of the reference sequence
     */
    public int getRefLength() {
        if (refLength <= 0) {
            this.sequence = ProjectConnector.getInstance().getRefGenomeConnector(id).getRefSequence();
            this.refLength = this.sequence.length();
        }
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
        return name + " " + description;
    }
    
    /* 
     * Need this to use PersistantReference class as key for HashMap 
     * @see http://stackoverflow.com/questions/27581/overriding-equals-and-hashcode-in-java
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
       return id;
   }
    
    /**
     * Checks if the given reference genome is equal to this one.
     * @param o object to compare to this object
     */
    @Override
    public boolean equals(Object o) {
        
        if (o instanceof PersistantReference) {
            PersistantReference ogenome = (PersistantReference) o;
            return ( 
               ogenome.getDescription().equals(this.description)
                && ogenome.getName().equals(this.name)
                && (ogenome.getId() == this.id)
                && (ogenome.getRefLength() == this.refLength)
                && ogenome.getTimeStamp().equals(this.timestamp)
                
                //for current purposes we do not need to compare the sequence, 
                //in most cases the id should be enough
                //&& ogenome.getSequence().equals(this.sequence); 
                    
               );  
        }
        else { return super.equals(o); }
    }

}
