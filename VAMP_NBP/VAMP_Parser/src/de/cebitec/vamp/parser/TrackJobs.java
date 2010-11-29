package de.cebitec.vamp.parser;

import de.cebitec.vamp.parser.mappings.MappingParserI;
import java.io.File;
import java.sql.Timestamp;

/**
 *
 * @author jstraube
 */
public class TrackJobs {

    private File file;
    private String description;
    private Timestamp timestamp;
    private MappingParserI parser;
    private Long trackID;
    private ReferenceJob refGen;

    public TrackJobs(Long trackID, File file, String description, ReferenceJob refGen, MappingParserI parser, Timestamp timestamp){
        this.trackID = trackID;
        this.file = file;
        this.description = description;
        this.timestamp = timestamp;
        this.parser = parser;
        this.refGen = refGen;
    }

    public MappingParserI getParser(){
        return parser;
    }

    public String getDescription() {
        return description;
    }

        public ReferenceJob getRefGen() {
        return refGen;
    }
    public File getFile() {
        return file;
    }

    public void setRefGen(ReferenceJob refGen) {
        this.refGen = refGen;
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
