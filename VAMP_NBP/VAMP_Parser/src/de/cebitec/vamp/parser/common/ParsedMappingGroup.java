package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public class ParsedMappingGroup implements Observable {

    private ArrayList<ParsedMapping> mappings;
    private int minError;
    private boolean bestMappingTagged;
    private boolean hasNewRead; //set true when new read was added until this variable was send to the observers
    private ArrayList<Observer> observers;

    public ParsedMappingGroup(){
        observers = new ArrayList<Observer>();
        mappings = new ArrayList<ParsedMapping>();
        minError = Integer.MAX_VALUE;
        bestMappingTagged = true;
    }

    public void addParsedMapping(ParsedMapping mapping){
        // if mapping already existed, increase the count of it
        if(mappings.contains(mapping)){
            mappings.get(mappings.lastIndexOf(mapping)).increaseCounter();
        } else {
            // otherwise just add it
            mappings.add(mapping);
            bestMappingTagged = false;
            if(mapping.getErrors() < minError){
                minError = mapping.getErrors();
            }
            this.hasNewRead = true;
            this.notifyObservers();
        }
    }

    private void tagBestMatches(){
        Iterator<ParsedMapping> it = mappings.iterator();
        while(it.hasNext()){
            ParsedMapping m = it.next();
            if(m.getErrors() == minError){
                m.setIsBestmapping(true);
            } else {
                m.setIsBestmapping(false);
            }
          
        }
        bestMappingTagged = true;
    }

    public List<ParsedMapping> getMappings(){
        if(!bestMappingTagged){
            tagBestMatches();
        }
        return mappings;
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
        this.hasNewRead = false;
    }

}
