package de.cebitec.vamp.parser;

import de.cebitec.vamp.parser.mappings.MappingParserI;
import java.io.File;
import java.sql.Timestamp;

/**
 * A track job is a container for all necessary data for a track to be parsed.
 *
 * @author jstraube, rhilker
 */
public class TrackJob implements Job {

    private boolean isDbUsed;
    private File file;
    private String description;
    private Timestamp timestamp;
    private MappingParserI parser;
    private int trackID;
    private ReferenceJob refGen;
    private boolean stepwise = false;
    private boolean firstJob = false;
    private int start;
    private int stop;
    private int stepSize;
    private boolean isSorted = true;
    
    /**
     * Creates a new track job along with its data.
     * @param trackID id of the track to create
     * @param isDbUsed true, if the track should be stored into the database and false, if 
     * direct file access is desired
     * @param file the file to be parsed as track
     * @param description the description of the track
     * @param refGen the ReferenceJob with all information about the reference
     * @param parser the parser to use for parsing
     * @param timestamp the timestamp when it was created
     */
    public TrackJob(int trackID, boolean isDbUsed, File file, String description,
            ReferenceJob refGen, MappingParserI parser, Timestamp timestamp) {
        this.trackID = trackID;
        this.isDbUsed = isDbUsed;
        this.file = file;
        this.description = description;
        this.timestamp = timestamp;
        this.parser = parser;
        this.refGen = refGen;
    }

    /**
     * @return true, if the track should be stored into the database and false, if 
     * direct file access is desired
     */
    public boolean isDbUsed() {
        return isDbUsed;
    }

    public MappingParserI getParser() {
        return parser;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public ReferenceJob getRefGen() {
        return refGen;
    }

    @Override
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setRefGen(ReferenceJob refGen) {
        this.refGen = refGen;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int getID() {
        return trackID;
    }

    @Override
    public String getName() {
        return getDescription();
    }
    
    @Override
    public String toString() {
        return description + ":" + timestamp;
    }

    public void setIdPersistant(int trackID) {
        this.trackID = trackID;
    }
    
    public boolean isStepwise() {
        return stepwise;
    }

    public void setIsStepwise(boolean isStepwise) {
        this.stepwise = isStepwise;
    }

    /**
     * @return the start position in the genome, if this is a stepwise parser.
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start position in the genome, if this is a stepwise parser.
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the stop position in the genome, if this is a stepwise parser.
     */
    public int getStop() {
        return stop;
    }

    /**
     * @param stop the stop position in the genome, if this is a stepwise parser.
     */
    public void setStop(int stop) {
        this.stop = stop;
    }

    public boolean isFirstJob() {
        return firstJob;
    }

    public void setIsFirstJob(boolean isFirstJob) {
        this.firstJob = isFirstJob;
    }

    public boolean isSorted() {
        return isSorted;
    }

    public void setIsSorted(boolean isSorted) {
        this.isSorted = isSorted;
    }

    
    
    public int getStepSize() {
        return stepSize;
    }

    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }
    
    

}
