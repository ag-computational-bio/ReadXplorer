package vamp.importer;

import java.io.File;
import java.sql.Timestamp;
import vamp.parsing.mappings.MappingParserI;

/**
 *
 * @author ddoppmeier
 */
public class TrackJob {
    
    private File file;
    private String description;
    private RunJob runJob;
    private ReferenceJob refGen;
    private Timestamp timestamp;
    private MappingParserI parser;
    private Long trackID;

    public TrackJob(Long trackID, File file, String description, RunJob runJob, ReferenceJob refGen, MappingParserI parser, Timestamp timestamp){
        this.trackID = trackID;
        this.file = file;
        this.description = description;
        this.runJob = runJob;
        this.refGen = refGen;
        this.timestamp = timestamp;
        this.parser = parser;
    }

    public MappingParserI getParser(){
        return parser;
    }

    public String getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    public ReferenceJob getRefGen() {
        return refGen;
    }

    public RunJob getRunJob() {
        return runJob;
    }

    public Timestamp getTimestamp(){
        return timestamp;
    }

    public Long getID(){
        return trackID;
    }

    @Override
    public String toString(){
        return description+":"+timestamp;
    }

    public void setPersistant(Long trackID) {
        this.trackID = trackID;
    }


}
