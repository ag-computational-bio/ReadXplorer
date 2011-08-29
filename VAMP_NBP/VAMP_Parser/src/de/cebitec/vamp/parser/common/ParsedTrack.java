package de.cebitec.vamp.parser.common;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Contains all data (description, mappings and coverageContainer) belonging
 * to a track, which can be stored into a database now.
 * 
 * @author ddoppmeier
 */
public class ParsedTrack {

    private HashMap<String, Integer> readNameToSeqIDMap;
    private ParsedMappingContainer mappings;
    private CoverageContainer coverageContainer;
    private String description;
    private Timestamp timestamp;
    private long id;

    public ParsedTrack(String description, ParsedMappingContainer mappings, CoverageContainer coverageContainer){
        this.readNameToSeqIDMap = new HashMap<String, Integer>();
        this.description = description;
        this.mappings = mappings;
        this.coverageContainer = coverageContainer;
    }

    public CoverageContainer getCoverageContainer(){
        return coverageContainer;
    }

    public ParsedMappingContainer getParsedMappingContainer() {
        return mappings;
    }

    public String getDescription(){
        return description;
    }

    public void setTimestamp(Timestamp timestamp){
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public long getID() {
        return id;
    }

    public void setID(long id){
        this.id = id;
    }

    /**
     * Needed additional information from sequence pair parsers.
     * @param seqToIdMap mapping of readname to sequence id
     */
    public void setReadnameToSeqIdMap(HashMap<String, Integer> seqToIdMap){
        this.readNameToSeqIDMap = seqToIdMap;
    }

    /**
     * @return the readname to sequence id map
     */
    public HashMap<String, Integer> getReadnameToSeqIdMap(){
        return this.readNameToSeqIDMap;
    }

    /**
     * Clears the mappings, coverage container and ReadnameToseqIDMap.
     * All other information persists!
     */
    public void clear(){
        this.mappings.clear();
        this.coverageContainer.clear();
        this.readNameToSeqIDMap.clear();
    }

}
