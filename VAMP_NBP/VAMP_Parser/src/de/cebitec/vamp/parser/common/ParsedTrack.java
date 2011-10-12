package de.cebitec.vamp.parser.common;

import java.sql.Timestamp;

/**
 * Contains all data (description, mappings and coverageContainer) belonging
 * to a track, which can be stored into a database now.
 * 
 * @author ddoppmeier
 */
public class ParsedTrack {

    private ParsedMappingContainer mappings;
    private CoverageContainer coverageContainer;
    private String description;
    private Timestamp timestamp;
    private long id;
    private boolean isStepwise;
    private boolean isFirstTrack;

    public ParsedTrack(String description, ParsedMappingContainer mappings, CoverageContainer coverageContainer){
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

    public boolean isStepwise() {
        return isStepwise;
    }

    public void setIsStepwise(boolean isStepwise) {
        this.isStepwise = isStepwise;
    }

    public boolean isFirstTrack() {
        return isFirstTrack;
    }

    public void setIsFirstTrack(boolean isFirstTrack) {
        this.isFirstTrack = isFirstTrack;
    }

    
    
    /**
     * Clears the mappings and the coverage container.
     * All other information persits!
     */
    public void clear(){
    mappings.clear();
    coverageContainer.clear();
    }

}
