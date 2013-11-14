package de.cebitec.readXplorer.parser.common;

import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.ReadPairType;
import de.cebitec.readXplorer.util.StatsContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for all read pairs belonging to a pair of tracks.
 * 
 * @author Rolf Hilker
 */
public class ParsedReadPairContainer {
    
    private int trackId1;
    private int trackId2;
    private String description;
    private HashMap<Pair<Long, Long>, ParsedReadPairMapping> parsedSeqPairs;
    private List<Pair<Long, Long>> mappingToPairIDList;
    private StatsContainer statsContainer;

    
    /**
     * Creates a new and empty sequence pair container.
     */
    public ParsedReadPairContainer(){
        this.parsedSeqPairs = new HashMap<>();
        this.mappingToPairIDList = new ArrayList<>();
        this.statsContainer = new StatsContainer();
        this.statsContainer.prepareForTrack();
        this.statsContainer.prepareForSeqPairTrack();
    }

    /**
     * Adds all mappings to a MappingGroup with same pair id, 
     * but different mapping positions
     * Mapping = is unique to a sequence and position
     * @param mappingIDs
     * @param parsedReadPair  
     */
    public void addParsedReadPair(Pair<Long, Long> mappingIDs, ParsedReadPairMapping parsedReadPair) {
        Map<String, Integer> statsMap = statsContainer.getStatsMap();
        if (!this.parsedSeqPairs.containsKey(mappingIDs)) {
            this.parsedSeqPairs.put(mappingIDs, parsedReadPair); //TODO: mappingIDs can be vice versa
            
            if (parsedReadPair.getType() == ReadPairType.PERFECT_PAIR || parsedReadPair.getType() == ReadPairType.PERFECT_UNQ_PAIR) {
                statsMap.put(StatsContainer.NO_UNIQ_PERF_PAIRS, statsMap.get(StatsContainer.NO_UNIQ_PERF_PAIRS) + 1);
            }
            statsMap.put(StatsContainer.NO_UNIQUE_PAIRS, statsMap.get(StatsContainer.NO_UNIQUE_PAIRS) + 1);
        } else {
            this.parsedSeqPairs.get(mappingIDs).addReplicate();
            if (parsedReadPair.getType() == ReadPairType.PERFECT_PAIR || parsedReadPair.getType() == ReadPairType.PERFECT_UNQ_PAIR) {
                statsMap.put(StatsContainer.NO_UNIQ_PERF_PAIRS, statsMap.get(StatsContainer.NO_UNIQ_PERF_PAIRS) - 1);
            }
            statsMap.put(StatsContainer.NO_UNIQUE_PAIRS, statsMap.get(StatsContainer.NO_UNIQUE_PAIRS) - 1);
        }
        statsContainer.incSeqPairStats(parsedReadPair.getType(), 1);
    }
    
    /**
     * Adds a pair containing a mapping id and a sequence pair id to the list.
     * @param mappingToPairId pair to add
     */
    public void addMappingToPairId(Pair<Long, Long> mappingToPairId){
        this.mappingToPairIDList.add(mappingToPairId);
    }
    
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    
    public HashMap<Pair<Long, Long>, ParsedReadPairMapping> getParsedSeqPairs() {
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

    public StatsContainer getStatsContainer() {
        return statsContainer;
    }
      
}
