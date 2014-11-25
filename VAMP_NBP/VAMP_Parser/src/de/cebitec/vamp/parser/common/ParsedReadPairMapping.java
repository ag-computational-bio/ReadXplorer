package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.util.ReadPairType;

/**
 * Container for a parsed sequence pair mapping. The seqPairID allows for identifying
 * all other mappings of this sequence pair.
 * 
 * @author Rolf Hilker
 */
public class ParsedReadPairMapping {
    
    private long id;
    private long seqPairID;
    private long mappingId1;
    private long mappingId2;
    private ReadPairType type;
    private int nbReplicates;
    private int distance;
    
    
    /**
     * A parsed sequence pair needs:
     * Parsed mapping references have to be passed, because the mapping id is not set yet.
     * It will be set when storing the data to the db.
     * @param seqPairID interim id of the sequence pair
     * @param type type of the sequence pair (0-5 = perfect, distance too small, distance too large, orientation wrong
     *      orient wrong and dist too small, orient wrong and dist too large)
     * @param mappingId1 id of fst mapping of the pair
     * @param mappingId2 id of scnd mapping of the pair
     * @param distance the sequence pair distance
     */
    public ParsedReadPairMapping(long mappingId1, long mappingId2, long seqPairID, ReadPairType type, int distance){
        this.mappingId1 = mappingId1;
        this.mappingId2 = mappingId2;
        this.seqPairID = seqPairID;
        this.type = type;
        this.distance = distance;
        this.nbReplicates = 1;
    }
    
    /**
     * @return Unique integer representing the id of this sequence pair mapping.
     */
    public long getId() {
        return this.id;
    }
    
    /** 
     * @return id of this sequence pair. most important id. Searching among all
     * sequence pair mappings for this id will return all mappings of this sequence pair
     * along the genome. Each of them is stored in another ParsedReadPairMapping.
     */
    public long getSequencePairID() {
        return this.seqPairID;
    }
    
    /**
     * @return Mapping id of the fst pair
     */
    public long getMappingId1() {
        return this.mappingId1;
    }
    
    /**
     * @return Mapping id of the second pair
     */
    public long getMappingId2() {
        return this.mappingId2;
    }

    /**
     * @return      PERFECT_PAIR = 0,
     *              DISTANCE_TOO_LARGE_PAIR = 1,
     *              DISTANCE_TOO_SMALL_PAIR = 2,
     *              ORIENTATION_WRONG_PAIR = 3,
     *              ORIENTATION_AND_DIST_TOO_LARGE_PAIR = 4,
     *              ORIENTATION_AND_DIST_TOO_SMALL_PAIR = 5,
     *              UNPAIRED_PAIR = 6
     */
    public ReadPairType getType(){
        return this.type;
    }
    
    /**
     * @param id unique integer representing the id of this sequence pair mapping.
     */
    public void setID(long id) {
        this.id = id;
    }

    /** 
     * @param sequencePairID of this sequence pair. most important id. Searching among all
     * sequence pair mappings for this id will return all mappings of this sequence pair
     * along the genome. Each of them is stored in another ParsedReadPairMapping.
     */
    public void setSequencePairID(long sequencePairID) {
        this.seqPairID = sequencePairID;
    }
    

    /**
     * Increases the count of repliactes by one.
     */
    public void addReplicate() {
        ++this.nbReplicates;
    }
    
    
    public int getReplicates(){
        return this.nbReplicates;
    }

    public int getDistance() {
        return distance;
    }    

    @Override
    public String toString(){
        return "Pair: "+id+", PairID: "+seqPairID+", MID1: "+mappingId1+", MID2: "+mappingId2+", Type: "+type+", nbReplicates: "+nbReplicates;
    }
    
}
