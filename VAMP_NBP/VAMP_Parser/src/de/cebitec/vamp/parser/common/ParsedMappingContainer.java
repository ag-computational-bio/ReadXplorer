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
 * as a all mappings.
 *
 * @author ddoppmeier
 */
public class ParsedMappingContainer implements Observable, Observer {
    private int numOfMappings = 0;
    private int numUniqueSeq ;
    private int numUniqueMappings;
    private int numReads;
    private boolean hasNewRead;
    private HashMap<Integer, ParsedMappingGroup> mappings;
    private ArrayList<Observer> observers;
    private boolean lastMappingContainer=false;
    private boolean firstMappingContainer=false;
    private int averageReadLength;
    /**
     * Creates an empty mapping container.
     */
    public ParsedMappingContainer(){
        observers = new ArrayList<Observer>();
        mappings = new HashMap<Integer, ParsedMappingGroup>();
    }

    public void addParsedMapping(ParsedMapping mapping, int sequenceID){
        numOfMappings++;
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
        mappingInfos.put(4, numUniqueMappings);
        mappingInfos.put(5, numUniqueSeq);
        mappingInfos.put(6, numReads);
        mappingInfos.put(7,averageReadLength);

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

    public void setNumberOfReads(int numberOfReads) {
        this.numReads = numberOfReads;
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

    public boolean isLastMappingContainer() {
        return lastMappingContainer;
    }

    public void setLastMappingContainer(boolean lastMappingContainer) {
        this.lastMappingContainer = lastMappingContainer;
    }

    public boolean isFirstMappingContainer() {
        return firstMappingContainer;
    }

    public void setFirstMappingContainer(boolean firstMappingContainer) {
        this.firstMappingContainer = firstMappingContainer;
    }

    /**
     * @return the averageReadLength
     */
    public int getAverageReadLength() {
        return averageReadLength;
    }

    /**
     * @param averageReadLength the averageReadLength to set
     */
    public void setAverageReadLength(int averageReadLength) {
        this.averageReadLength = averageReadLength;
    }
    
    

}
