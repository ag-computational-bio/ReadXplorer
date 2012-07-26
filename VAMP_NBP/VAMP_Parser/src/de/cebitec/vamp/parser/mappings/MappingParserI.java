package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observable;

/**
 * Interface to be implemented for all mapping parsers.
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI, Observable {

    //public abstract ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException;
    /**
     * Parses the input determined by the track job.
     * @param trackJob the track job to parse
     * @param sequenceString the reference sequence
     * @return the parsed data object
     * @throws ParsingException
     * @throws OutOfMemoryError 
     */
    public abstract Object parseInput(TrackJob trackJob, String sequenceString) throws ParsingException, OutOfMemoryError;
    
    /**
     * @return the sequence pair processor of this parser. It processes and
     *      contains all necessary information for a sequence pair import.
     */
    public SeqPairProcessorI getSeqPairProcessor();
    
    /**
     * @return Additional data calculated in this parser.
     */
    public abstract Object getAdditionalData();

}
