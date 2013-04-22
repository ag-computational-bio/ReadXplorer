package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.util.FeatureType;
import de.cebitec.vamp.util.Properties;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolf Hilker
 * 
 * Holds all PersistantSequencePairs which belong to one sequence pair id.
 * Since a pair might have more than one mapping in the visible interval
 * and a pair id might not have an ordinary sequence pair, but several mappings
 * of both reads along the genome we need this data structure.
 */
public class PersistantSeqPairGroup {
    
    private long seqPairId;
    private List<PersistantSequencePair> seqPairs;
    private List<PersistantMapping> singleMappings;
//    private boolean hasNewRead; //set true when new read was added until this variable was send to the observers
//    private ArrayList<Observer> observers;
    private List<FeatureType> excludedFeatureTypes;

    public PersistantSeqPairGroup(){
//        observers = new ArrayList<Observer>();
        this.seqPairs = new ArrayList<>();
        this.singleMappings = new ArrayList<>();
    }

    /**
     * Adds a new mapping to the group and creates a new PersistantSequencePair, if necessary.
     * @param mapping the mapping to add to the group
     * @param type type of the sequence pair this mapping is belonging to (@see de.cebitec.vamp.util.Properties)
     * @param mapping1Id id of the first mapping of the sequence pair to create, or -1 in case of a single mapping
     * @param mapping2Id id of the second mapping of the sequence pair to create, or -1 in case of a single mapping
     * @param replicates number of replicates of the sequence pair to create, or -1 in case of a single mapping
     */
    public void addPersistantMapping(PersistantMapping mapping, byte type, long mapping1Id, long mapping2Id, int replicates){
        
        boolean stored = false;
        if (type != Properties.TYPE_UNPAIRED_PAIR) {
            for (PersistantSequencePair seqPair : this.seqPairs) { //TODO: exponential!!! reduce complexity by hash or else...

                if (mapping.getId() == seqPair.getVisibleMapping().getId()
                        || mapping.getId() == seqPair.getMapping2Id() && seqPair.hasVisibleMapping2()) {

                    //second mapping of this sequence pair = second mappingid will deviate = create a new pair
                    this.seqPairs.add(new PersistantSequencePair(this.seqPairId, mapping1Id, mapping2Id, type, replicates, mapping));
                    stored = true;
                    break;

                } else if (mapping.getId() == seqPair.getMapping2Id()) {

                    // pair already exists, this is the second mapping of that pair = add it
                    seqPair.setVisiblemapping2(mapping);
                    stored = true;
                    break;
                }
            }
            if (!stored) {
                // this mapping defines a new sequence pair for this pair id
                this.seqPairs.add(new PersistantSequencePair(this.seqPairId, mapping1Id, mapping2Id, type, replicates, mapping));
            }
        } else {
            //this is a single mapping, just add id to the list
            this.singleMappings.add(mapping);
        }
        
//            this.hasNewRead = true;
//            this.notifyObservers();
    }
    
    /**
     * Adds a new direct access mapping to the group and creates a new 
     * PersistantSequencePair, if necessary.
     * @param mapping the mapping to add to the group
     * @param type type of the sequence pair this mapping is belonging to (
     * @param bothVisible true, if both mappings of the pair are visible
     * @see de.cebitec.vamp.util.Properties)
     */
    public void addPersistantDirectAccessMapping(PersistantMapping mapping, byte type, boolean bothVisible) {

        boolean stored = false;
        if (type != Properties.TYPE_UNPAIRED_PAIR) {
            int replicates;
            for (PersistantSequencePair seqPair : this.seqPairs) {

                if (!bothVisible) { //second mapping of this sequence pair = create a new pair

                    replicates = seqPair.getSeqPairReplicates() + 1;
                    seqPair.setSeqPairReplicates(replicates);
                    this.seqPairs.add(new PersistantSequencePair(this.seqPairId, mapping.getId(), -1, type, replicates , mapping));
                    stored = true;
                    break;

                } else {

                    // pair already exists, this is the second mapping of that pair = add it
                    seqPair.setVisiblemapping2(mapping);
                    stored = true;
                    break;
                }
            }
            if (!stored) {
                // this mapping defines a new sequence pair for this pair id
                this.seqPairs.add(new PersistantSequencePair(this.seqPairId, mapping.getId(), -1, type, 1, mapping));
            }
        } else {
            //this is a single mapping, just add id to the list
            this.singleMappings.add(mapping);
        }

//            this.hasNewRead = true;
//            this.notifyObservers();
    }

    
    public long getSeqPairId() {
        return seqPairId;
    }

    
    public void setSeqPairId(long seqPairId) {
        this.seqPairId = seqPairId;
    }  

    /**
     * @return List of all sequence pairs belonging to this pair id.
     */
    public List<PersistantSequencePair> getSequencePairs(){
        return this.seqPairs;
    }
    
    /**
     * @return List of all single mappings belonging to this pair id.
     */
    public List<PersistantMapping> getSingleMappings(){
        return this.singleMappings;
    }

//    @Override
//    public void registerObserver(Observer observer) {
//        this.observers.add(observer);
//    }
//
//    @Override
//    public void removeObserver(Observer observer) {
//        this.observers.remove(observer);
//    }
//
//    @Override
//    public void notifyObservers() {
//        for (Observer observer : observers) {
//            observer.update(this.hasNewRead);
//        }
//        this.hasNewRead = false;
//    }

    public void setExcludedFeatureTypes(List<FeatureType> excludedFeatureTypes) {
        this.excludedFeatureTypes = excludedFeatureTypes;
    }
    
}
