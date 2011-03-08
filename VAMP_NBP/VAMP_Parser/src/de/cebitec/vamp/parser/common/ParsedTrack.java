package de.cebitec.vamp.parser.common;

import java.sql.Timestamp;

/**
 *
 * @author ddoppmeier
 */
public class ParsedTrack {

    private ParsedMappingContainer mappings;
    private CoverageContainer coverageContainer;
    private String description;
    private Timestamp timestamp;
    private long id;

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

    public void clear(){
    mappings.clear();
    coverageContainer.clear();
    }

}
