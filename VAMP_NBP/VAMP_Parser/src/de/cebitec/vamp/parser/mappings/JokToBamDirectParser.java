package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.DirectAccessDataContainer;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.parser.output.JokToBamConverter;
import de.cebitec.vamp.util.Observer;
import de.cebitec.vamp.util.StatsContainer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A jok parser, which first reads the jok file, converts it into a bam file
 * sorted by mapping start position and then prepares the new bam file for
 * import into the DB as direct access track.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class JokToBamDirectParser implements MappingParserI, Observer {
    
    private static String name = "Jok to Bam Direct Access Parser";
    private static String[] fileExtension = new String[]{"out", "Jok", "jok", "JOK"};
    private static String fileDescription = "Jok Read Mappings converted to BAM";
    private SamBamDirectParser bamParser;
    private List<Observer> observers;
    private boolean alreadyConverted = false;

    /**
     * A jok parser, which first reads the jok file, converts it into a bam file
     * sorted by mapping start position and then prepares the new bam file for
     * import into the DB as direct access track.
     */
    public JokToBamDirectParser() {
        this.observers = new ArrayList<>();
        this.bamParser = new SamBamDirectParser();
    }

    /**
     * Not implemented for this parser implementation, as currently no
     * preprocessing is needed.
     * @param trackJob the trackjob to preprocess
     * @return true, if the method succeeded
     * @throws ParsingException
     * @throws OutOfMemoryError
     */
    @Override
    public Object preprocessData(TrackJob trackJob) throws ParsingException, OutOfMemoryError {
        return true;
    }
    
    
    @Override
    public Object parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError {
        
        this.preprocessData(trackJob);
        
        //parse the newly converted bam file
        bamParser.registerObserver(this);
        DirectAccessDataContainer trackData = bamParser.parseInput(trackJob, sequenceString);
        bamParser.removeObserver(this);
        
        return trackData;
    }
    
    /**
     * Converts a jok file into a bam file sorted by mapping start position.
     * Also updates the file in the track job to the new file.
     * @param trackJob the track job containing the jok file
     * @param referenceSequence the complete reference sequence string
     * @return true, if the conversion was successful, false otherwise
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    @Override
    public Object convert(TrackJob trackJob, String referenceSequence) throws ParsingException, OutOfMemoryError {
        String refName = trackJob.getRefGen().getName();

        //Convert jok file to bam
        JokToBamConverter jokConverter = new JokToBamConverter();
        List<File> jobs = new ArrayList<>();
        jobs.add(trackJob.getFile());
        jokConverter.registerObserver(this);
        jokConverter.setDataToConvert(jobs, refName, referenceSequence.length());
        boolean success = jokConverter.convert();
        jokConverter.removeObserver(this);

        //update the track job with the new bam file
        trackJob.setFile(jokConverter.getOutputFile());
        return success;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }
    
    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }

    @Override
    public void update(Object args) {
        this.notifyObservers(args);
    }

    /**
     * @return true, if the jok file has already been converted, false otherwise
     */
    public boolean isAlreadyConverted() {
        return this.alreadyConverted;
    }

    @Override
    public void setStatsContainer(StatsContainer statsContainer) {
        //do nothing right now
    }
    
}
