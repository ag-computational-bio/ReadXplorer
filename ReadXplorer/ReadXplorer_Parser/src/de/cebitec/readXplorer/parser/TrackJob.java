package de.cebitec.readXplorer.parser;

import de.cebitec.readXplorer.parser.mappings.MappingParserI;
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
    private boolean isAlreadyImported;
    
    /**
     * Creates a new track job along with its data.
     * @param trackID id of the track to create
     * @param isDbUsed true, if the track should be stored into the database and false, if 
     * direct file access is desired
     * @param file the file to be parsed as track
     * @param description the description of the track
     * @param refGen the ReferenceJob with all information about the reference
     * @param parser the parser to use for parsing
     * @param isAlreadyImported true, if this direct access track was already imported in another
     * readXplorer db.
     * @param timestamp the timestamp when it was created
     */
    public TrackJob(int trackID, boolean isDbUsed, File file, String description,
            ReferenceJob refGen, MappingParserI parser, boolean isAlreadyImported, Timestamp timestamp) {
        this.trackID = trackID;
        this.isDbUsed = isDbUsed;
        this.file = file;
        this.description = description;
        this.timestamp = timestamp;
        this.parser = parser;
        this.isAlreadyImported = isAlreadyImported;
        this.refGen = refGen;
    }

    /**
     * @return true, if the track should be stored into the database and false, if 
     * direct file access is desired
     */
    public boolean isDbUsed() {
        return isDbUsed;
    }

    /**
     * @return the parser, which shall be used for parsing this track job.
     */
    public MappingParserI getParser() {
        return parser;
    }

    /**
     * @return the description of this track job.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @return the reference genome associated with this track job.
     */
    public ReferenceJob getRefGen() {
        return refGen;
    }

    /**
     * @return the file, which contains the track data and which should be
     * parsed and stored into the db / as direct access track.
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     * @param file the file, which contains the track data and which should be
     * parsed and stored into the db / as direct access track.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @param refGen the reference genome associated with this track job.
     */
    public void setRefGen(ReferenceJob refGen) {
        this.refGen = refGen;
    }

    /**
     * @return the timestamp at which this track job was created.
     */
    @Override
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the track id of this track job used in the db later on.
     */
    @Override
    public int getID() {
        return this.trackID;
    }

    /**
     * @return the name of this track job.
     */
    @Override
    public String getName() {
        return this.getDescription();
    }
    
    /**
     * @return Modified to return the description and the timestamp.
     */
    @Override
    public String toString() {
        return this.description + ":" + this.timestamp;
    }
    
    /**
     * @return true, if this trackjob should be imported stepwise. 
     * This allows to adjust the ram load.
     */
    public boolean isStepwise() {
        return this.stepwise;
    }

    /**
     * @param isStepwise true, if this trackjob should be imported stepwise. 
     * This allows to adjust the ram load.
     */
    public void setIsStepwise(boolean isStepwise) {
        this.stepwise = isStepwise;
    }

    /**
     * @return the start position in the genome, if this is a stepwise parser.
     */
    public int getStart() {
        return this.start;
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
        return this.stop;
    }

    /**
     * @param stop the stop position in the genome, if this is a stepwise parser.
     */
    public void setStop(int stop) {
        this.stop = stop;
    }

    /**
     * @return true, if this is the first job of a stepwise import
     * track job, false otherwise.
     */
    public boolean isFirstJob() {
        return firstJob;
    }

    /**
     * @param isFirstJob true, if this is the first job of a stepwise import
     * track job, false otherwise.
     */
    public void setIsFirstJob(boolean isFirstJob) {
        this.firstJob = isFirstJob;
    }

    /**
     * @return true, if this trackJob is already sorted by read sequence, 
     * false otherwise.
     */
    public boolean isSorted() {
        return this.isSorted;
    }

    /**
     * @param isSorted true, if this trackJob is already sorted by readsequence, 
     * false otherwise.
     */
    public void setIsSorted(boolean isSorted) {
        this.isSorted = isSorted;
    }

    /**
     * @return stepSize the setp size of the import. The step size depicts the chunk of data,
     * which is read in one chunk.
     */
    public int getStepSize() {
        return this.stepSize;
    }

    /**
     * @param stepSize the setp size of the import. The step size depicts the chunk of data,
     * which is read in one chunk.
     */
    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * @return true, if this direct access track was already imported in another
     * readXplorer db, false otherwise.
     */
    public boolean isAlreadyImported() {
        return this.isAlreadyImported;
    }
    

}
