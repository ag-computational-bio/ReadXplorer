package de.cebitec.vamp.parser.common;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Contains all data (description, mappings and coverageContainer) belonging
 * to a track, which can be stored into a database now.
 * 
 * @author ddoppmeier, rhilker
 */
public class ParsedTrack {

    private HashMap<String, Integer> readNameToSeqIDMap;
    private ParsedMappingContainer mappings;
    private CoverageContainer coverageContainer;
    private String description;
    private Timestamp timestamp;
    private int trackId;
    private boolean isStepwise;
    private boolean isFirstTrack;
    private int refId;

    /**
     * Contains all data (description, mappings and coverageContainer) belonging
     * to a track, which can be stored into a database now.
     * @param trackId corect id of the track has to be set on object creation
     * @param description description of the track
     * @param mappings mappings of the track
     * @param coverageContainer coverage container of the track
     * @param refId reference genome id 
     */
    public ParsedTrack(int trackId, String description, ParsedMappingContainer mappings, CoverageContainer coverageContainer, int refId){
        this.readNameToSeqIDMap = new HashMap<String, Integer>();
        this.description = description;
        this.mappings = mappings;
        this.coverageContainer = coverageContainer;
        this.trackId = trackId;
        this.refId = refId;
    }

    /**
     * @return the coverage container of this track with all coverage information
     */
    public CoverageContainer getCoverageContainer(){
        return coverageContainer;
    }

    /**
     * @return the container of all mappings of this track (if they were stored,
     * if not, the container is just empty)
     */
    public ParsedMappingContainer getParsedMappingContainer() {
        return mappings;
    }

    /**
     * @return the description of the track
     */
    public String getDescription(){
        return description;
    }

    /**
     * @param timestamp the timestamp of the creation time of this track
     */
    public void setTimestamp(Timestamp timestamp){
        this.timestamp = timestamp;
    }

    /**
     * @return the timestamp of the creation time of this track
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * @return the track id of this track
     */
    public int getID() {
        return trackId;
    }

    /**
     * @return the id of the reference genome in the db
     */
    public int getRefId() {
        return refId;
    }
    
    /**
     * @return true, if this is a stepwise import into the db, false otherwise.
     */
    public boolean isStepwise() {
        return isStepwise;
    }

    /**
     * @param isStepwise true, if this is a stepwise import into the db, false otherwise.
     */
    public void setIsStepwise(boolean isStepwise) {
        this.isStepwise = isStepwise;
    }

    /**
     * @return true, if this is the first track of a stepwise import (which
     * then consists of many tracks. One for each chunk of data).
     */
    public boolean isFirstTrack() {
        return isFirstTrack;
    }

    /**
     * @param isFirstTrack true, if this is the first track of a stepwise import (which
     * then consists of many tracks. One for each chunk of data).
     */
    public void setIsFirstTrack(boolean isFirstTrack) {
        this.isFirstTrack = isFirstTrack;
    }
    /**
     * @return the readname to sequence id map
     */
    public HashMap<String, Integer> getReadnameToSeqIdMap(){
        return this.readNameToSeqIDMap;
    }

    
    /**
     * Needed additional information from sequence pair parsers.
     * @param seqToIdMap mapping of readname to sequence id
     */
    public void setReadnameToSeqIdMap(HashMap<String, Integer> seqToIdMap){
        this.readNameToSeqIDMap = seqToIdMap;
    }
    
    /**
     * Clears the mappings, coverage container and ReadnameToseqIDMap.
     * All other information persists!
     */
    public void clear(){
        this.mappings.clear();
        this.readNameToSeqIDMap.clear();
        coverageContainer.clearCoverageContainer();
    }

}
