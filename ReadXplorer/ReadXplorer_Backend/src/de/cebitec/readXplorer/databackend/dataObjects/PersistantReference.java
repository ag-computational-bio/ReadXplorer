package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A persistant reference containing an id, name, description & timestamp of a 
 * reference genome. It also garants access to the active and all other 
 * chromosomes of the reference.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantReference implements Observable {

    private int id;
    private int activeChromID;
    private String name;
    private String description;
//    private int refLength;
    private Map<Integer, PersistantChromosome> chromosomes;
    private Timestamp timestamp;
    private int noChromosomes;
    private List<Observer> observers;

    /**
     * Data holder for a reference genome containing an id, name, description &
     * timestamp of a reference genome. It also garants access to the active and
     * all other chromosomes of the reference.
     * @param id The database id of the reference.
     * @param name The name of the reference.
     * @param description The additional description of the reference.
     * @param timestamp The insertion timestamp of the reference.
     */
    public PersistantReference(int id, String name, String description, Timestamp timestamp) {
        this(id, -1, name, description, timestamp);
    }
    
    /**
     * Data holder for a reference genome containing an id, name, description &
     * timestamp of a reference genome. It also garants access to the active and
     * all other chromosomes of the reference.
     * @param id The database id of the reference.
     * @param activeChromId id of the currently active chromosome (>= 0)
     * @param name The name of the reference.
     * @param description The additional description of the reference.
     * @param timestamp The insertion timestamp of the reference.
     */
    public PersistantReference(int id, int activeChromId, String name, String description, Timestamp timestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.chromosomes = ProjectConnector.getInstance().getRefGenomeConnector(id).getChromosomesForGenome();
        this.noChromosomes = this.chromosomes.size();
        this.timestamp = timestamp;
        this.observers = new ArrayList<>();
        if (activeChromId < 0) {
            Iterator<PersistantChromosome> chromIt = chromosomes.values().iterator();
            if (chromIt.hasNext()) {
                activeChromID = chromIt.next().getId();
            }
        } else {
            this.activeChromID = activeChromId;
        }

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
     * @param chromId the id of the chromosome of interest
     * @return The chromosome of interest.
     */
    public PersistantChromosome getChromosome(int chromId) {
        return this.chromosomes.get(chromId);
        }
    
    /**
     * @return The currently active chromosome of this reference.
     */
    public PersistantChromosome getActiveChromosome() {
        return this.chromosomes.get(this.getActiveChromId());
    }

    /**
     * @param chromId the chromosome id of interest
     * @param observer The observer of the chromosome. It is added to the
     * chromosome and should remain in the observer list, as long, as the
     * observer needs the sequence of the chromosome.
     * @return The chromosome sequence for the given chromosome id.
     */
    public String getChromSequence(int chromId, Observer observer) {
        return this.chromosomes.get(chromId).getSequence(observer);
    }
    
    /**
     * @param observer The observer of the chromosome. It is added to the
     * chromosome and should remain in the observer list, as long, as the
     * observer needs the sequence of the chromosome.
     * @return The chromosome sequence for the given chromosome id.
     */
    public String getActiveChromSequence(Observer observer) {
        return this.chromosomes.get(this.getActiveChromId()).getSequence(observer);
    }

    /**
     * @param observer The observer of the chromosome. It is added to the
     * chromosome and should remain in the observer list, as long, as the
     * observer needs the sequence of the chromosome.
     * @return The chromosome sequence for the given chromosome id.
     */
    public int getActiveChromLength() {
        return this.chromosomes.get(this.getActiveChromId()).getLength();
    }
    
    /**
     * @return The map of all chromosomes of this reference genome.
     */
    public Map<Integer, PersistantChromosome> getChromosomes() {
        return this.chromosomes;
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
            return (ogenome.getDescription().equals(this.description)
                    && ogenome.getName().equals(this.name)
                    && (ogenome.getId() == this.id)
                    && (ogenome.getNoChromosomes() == this.getNoChromosomes())
                    && ogenome.getTimeStamp().equals(this.timestamp) //for current purposes we do not need to compare the sequence, 
                    //in most cases the id should be enough
                    //&& ogenome.getSequence().equals(this.sequence); 
                    );
        } else {
            return super.equals(o);
        }
    }

    /**
     * @return The number of chromosomes of this reference genome.
     */
    public int getNoChromosomes() {
        return noChromosomes;
}
    
    /**
     * @return The id of the currently active chromosome
     */
    public int getActiveChromId() {
        return activeChromID;        
    }

    /**
     * @param chromId The id of the currently active chromosome
     */
    public void setActiveChromId(int chromId) {
        if (activeChromID != chromId) {
            this.activeChromID = chromId;
            this.notifyObservers(this.activeChromID);
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }
    
    /**
     * Utility method for calculating the whole genome length. It is generated
     * by adding the length of each available chromosome.
     * @param chromosomeMap the map containing all chromosomes, for which the
     * whole genome length shall be calculated.
     * @return The whole genome length. It is generated by adding the length of
     * each available chromosome.
     */
    public static int calcWholeGenomeLength(Map<Integer, PersistantChromosome> chromosomeMap) {
        int wholeGenomeLength = 0;
        for (PersistantChromosome chrom : chromosomeMap.values()) {
            wholeGenomeLength += chrom.getLength();
        }
        return wholeGenomeLength;
    }

}
