package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all sequence pairs belonging to a pair of tracks.
 * 
 * @author Rolf Hilker
 */
public class ParsedSeqPairContainer {
    
    //have to be calculated after parsing
    private long numOfPerfectSPs;
    private long numUniquePerfectSPs;
    private long numOfSeqPairs = 0;
    private long numOfUniqueSPs = 0;
    private String description;
    
    private HashMap<Pair<Long, Long>, ParsedSeqPairMapping> parsedSeqPairs;
    private List<Pair<Long, Long>> mappingToPairIDList;

    
    public ParsedSeqPairContainer(){
        this.parsedSeqPairs = new HashMap<Pair<Long, Long>, ParsedSeqPairMapping>();
        this.mappingToPairIDList = new ArrayList<Pair<Long, Long>>();
    }

    /*
     * MappingGroup = alle Mappings mit selber Sequenz, aber untersch. pos
     * Mapping = zu einer sequenz und einer pos
     * bei 3 mappings in group für id 1 hieße das: 3 pos im genom, wenn selbes bei read2 auch dann
     * teste also erst auf perfektes mapping und wenn keins da, dann: speichere alle mapping ids zu pair id
     * wenn eins oder mehrere da dann nur die zusätzlichen mapping ids zur pairid speichern
     * 
     */
    public void addParsedSeqPair(Pair<Long, Long> mappingIDs, ParsedSeqPairMapping parsedSeqPair) {
        if(!this.parsedSeqPairs.containsKey(mappingIDs)){
            this.parsedSeqPairs.put(mappingIDs, parsedSeqPair);            
        } else {
            this.parsedSeqPairs.get(mappingIDs).addReplicate();
        }
        ++this.numOfSeqPairs;
    }
    
    /**
     * Adds a pair containing a mapping id and a sequence pair id to the list.
     * @param mappingToPairId pair to add
     */
    public void addMappingToPairId(Pair<Long, Long> mappingToPairId){
        this.mappingToPairIDList.add(mappingToPairId);
    }
    
    
    public void setNumOfSeqPairs(long numOfMatePairs) {
        this.numOfSeqPairs = numOfMatePairs;
    }

    
    public void setNumOfPerfectSPs(long numOfPerfectSPs) {
        this.numOfPerfectSPs = numOfPerfectSPs;
    }

    
    public void setNumOfUniqueSPs(long numOfUniqueSPs) {
        this.numOfUniqueSPs = numOfUniqueSPs;
    }

    
    public void setNumUniquePerfectSPs(long numUniquePerfectSPs) {
        this.numUniquePerfectSPs = numUniquePerfectSPs;
    }

    
    public void setDescription(String description) {
        this.description = description;
    }
    
    
    public long getNumOfSeqPairs() {
        return numOfSeqPairs;
    }

    
    public long getNumOfPerfectSPs() {
        return numOfPerfectSPs;
    }

    
    public long getNumOfUniqueSPs() {
        return numOfUniqueSPs;
    }

    
    public long getNumUniquePerfectSPs() {
        return numUniquePerfectSPs;
    }

    
    public HashMap<Pair<Long, Long>, ParsedSeqPairMapping> getParsedSeqPairs() {
        return parsedSeqPairs;
    }
    
    /**
     * @return The mapping list of all mapping ids to their corresponding 
     * sequence pair ids. Only contains the mappings which don't form a proper pair.
     * To get this mapping for paired sequences use <code>getParsedSeqPairs()</code>.
     */
    public List<Pair<Long, Long>> getMappingToPairIdList(){
        return this.mappingToPairIDList;
    }
    
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Clears only the parsed mate pairs. All other data persists.
     */
    public void clear(){
        parsedSeqPairs.clear();
    }
    
}
