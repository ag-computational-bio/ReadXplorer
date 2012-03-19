package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.util.Pair;
import de.cebitec.vamp.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all sequence pairs belonging to a pair of tracks.
 * 
 * @author Rolf Hilker
 */
public class ParsedSeqPairContainer {
    
    private int trackId1;
    private int trackId2;
    private long numOfPerfectSPs = 0;
    private long numUniquePerfectSPs = 0; //counted here
    private long numOfSeqPairs = 0; //counted here
    private long numOfUniqueSPs = 0; //container size at the end of classification
    private long numOfSingleMappings = 0; //container size at the end of classification
    private String description;
    private int average_Seq_Pair_length=0;
    private HashMap<Pair<Long, Long>, ParsedSeqPairMapping> parsedSeqPairs;
    private List<Pair<Long, Long>> mappingToPairIDList;

    
    public ParsedSeqPairContainer(){
        this.parsedSeqPairs = new HashMap<Pair<Long, Long>, ParsedSeqPairMapping>();
        this.mappingToPairIDList = new ArrayList<Pair<Long, Long>>();
    }

    /*
     * Adds all mappings to a MappingGroup with same pair id, 
     * but different mapping positions
     * Mapping = is unique to a sequence and position
     */
    public void addParsedSeqPair(Pair<Long, Long> mappingIDs, ParsedSeqPairMapping parsedSeqPair) {
        if(!this.parsedSeqPairs.containsKey(mappingIDs)){
            this.parsedSeqPairs.put(mappingIDs, parsedSeqPair);   
            if (parsedSeqPair.getType() == Properties.TYPE_PERFECT_PAIR) {
                ++this.numUniquePerfectSPs;
                ++this.numOfPerfectSPs;
            }
            ++this.numOfUniqueSPs;
        } else {
            this.parsedSeqPairs.get(mappingIDs).addReplicate();
            if (parsedSeqPair.getType() == Properties.TYPE_PERFECT_PAIR) {
                --this.numUniquePerfectSPs;
                ++this.numOfPerfectSPs;
            }
            --this.numOfUniqueSPs;
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

    
    public void setNumOfPerfectSPs(long numOfPerfectSPs) {
        System.out.println("old num perf sps: " + this.numOfPerfectSPs + ", new: "+numOfPerfectSPs);
        this.numOfPerfectSPs = numOfPerfectSPs;
    }

    
    public void setNumUniquePerfectSPs(long numUniquePerfectSPs) {
        System.out.println("old: " + this.numUniquePerfectSPs + ", new: "+numUniquePerfectSPs);
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

    
    public long getNumOfSingleMappings() {
        return this.numOfSingleMappings;
    }

    
    public void setNumOfSingleMappings(long numOfSingleMappings) {
        this.numOfSingleMappings = numOfSingleMappings;
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

    public int getTrackId1() {
        return trackId1;
    }

    public void setTrackId1(int trackId1) {
        this.trackId1 = trackId1;
    }

    public int getTrackId2() {
        return trackId2;
    }

    public void setTrackId2(int trackId2) {
        this.trackId2 = trackId2;
    }

    /**
     * @return the average_Seq_Pair_length
     */
    public int getAverage_Seq_Pair_length() {
        return average_Seq_Pair_length;
    }

    /**
     * @param average_Seq_Pair_length the average_Seq_Pair_length to set
     */
    public void setAverage_Seq_Pair_length(int average_Seq_Pair_length) {
        this.average_Seq_Pair_length = average_Seq_Pair_length;
    }
    
    
    
}
