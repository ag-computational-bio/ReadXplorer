package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParserI;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.StatsContainer;

/**
 * Interface to be implemented for all mapping parsers.
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI, Observable, PreprocessorI {

    //public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException;
    /**
     * Parses the input determined by the track job.
     * @param trackJob the track job to parse
     * @param sequenceString the reference sequence
     * @return the parsed data object
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    public Object parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError;
    
    /**
     * Converts some data for the given track job and the given reference.
     * @param trackJob the track job whose data needs to be converted
     * @param referenceSequence the reference sequence associated to the track job
     * @return Any object the specific implementation needs
     * @throws ParsingException
     * @throws OutOfMemoryError  
     */
    public Object convert(TrackJob trackJob, String referenceSequence) throws ParsingException, OutOfMemoryError;
        
    /**
     * Sets the given stats container to this parser. Then this parser can
     * store statistics.
     * @param statsContainer the stats container to set
     */
    public void setStatsContainer(StatsContainer statsContainer);

}
