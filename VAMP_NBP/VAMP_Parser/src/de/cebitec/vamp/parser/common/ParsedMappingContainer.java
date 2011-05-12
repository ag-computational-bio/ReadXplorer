package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Container for all mappings belonging to one track. Contains statistics as well
 * as a HashMap with all mappings.
 *
 * @author ddoppmeier
 */
public class ParsedMappingContainer implements Observable, Observer {
    private int noOfMappings = 0;
    private int numUniqueSeq ;
    private int numUniqueMappings;
    private boolean hasNewRead;
    private HashMap<Integer, ParsedMappingGroup> mappings;
    private ArrayList<Observer> observers;

    /**
     * Creates an empty mapping container.
     */
    public ParsedMappingContainer(){
        observers = new ArrayList<Observer>();
        mappings = new HashMap<Integer, ParsedMappingGroup>();
    }

    public void addParsedMapping(ParsedMapping mapping, int sequenceID){
        noOfMappings++;
        if(!mappings.containsKey(sequenceID)){
            ParsedMappingGroup mappingGroup = new ParsedMappingGroup();
            mappingGroup.registerObserver(this); //need this to check for new reads
            mappings.put(sequenceID, mappingGroup);
        }
        mappings.get(sequenceID).addParsedMapping(mapping);
    }

    public Collection<Integer> getMappedSequenceIDs(){
        return mappings.keySet();
    }

    public ParsedMappingGroup getParsedMappingGroupBySeqID(int sequenceID){
        return mappings.get(sequenceID);
    }
    
    public HashMap<Integer,Integer> getMappingInformations() {
        HashMap<Integer,Integer> mappingInfos = new HashMap<Integer,Integer>();
        int numberOfBM = 0;
        int numberOfPerfect = 0;
        //is the number of unique Mapped Sequences
        int numberOfMappedSeq = mappings.size();
        //the number of created Mappings by the mapper
        int numberOfMappings = 0;

        Collection<ParsedMappingGroup> groups = mappings.values();
        Iterator<ParsedMappingGroup> it = groups.iterator();

        while (it.hasNext()) {
            ParsedMappingGroup p = it.next();
            List<ParsedMapping> mappingList = p.getMappings();
            Iterator<ParsedMapping> maps = mappingList.iterator();
            while (maps.hasNext()) {
                ParsedMapping m = maps.next();
                if(m.isBestMapping() == true) {
                        numberOfBM++;
                    if(m.getErrors() == 0){
                         numberOfPerfect++;
                    }
                }
                numberOfMappings += m.getCount();
            }
        }

        mappingInfos.put(1, numberOfMappings);
        mappingInfos.put(2, numberOfPerfect);
        mappingInfos.put(3, numberOfBM);
        mappingInfos.put(4, numberOfMappedSeq);
        mappingInfos.put(5, numUniqueMappings);
        mappingInfos.put(6, numUniqueSeq);

        return mappingInfos;
    }

    public void clear(){
        mappings.clear();
    }

    public void setNumberOfUniqueMappings(int numUniqueMappings) {
        this.numUniqueMappings = numUniqueMappings;
    }

    public void setNumberOfUniqueSeq(int numUniqueSeq) {
        this.numUniqueSeq = numUniqueSeq;
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
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this.hasNewRead);
        }
    }

    @Override
    public void update(Object args) {
        if (args instanceof Boolean){
            this.hasNewRead = (Boolean) args;
            this.notifyObservers();
        }
    }

}
