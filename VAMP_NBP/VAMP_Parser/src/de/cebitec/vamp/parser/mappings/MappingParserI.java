package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observable;
//import java.util.HashMap;

/**
 * Interface to be implemented for all mapping parsers.
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI, Observable {

    //public abstract ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException;
    public abstract ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException;

    /**
     *
     * @param trackJob
     * @return
     * @throws ParsingException
     * @deprecated Since the RUN domain has been excluded this method is not needed anymore!
     */
    @Deprecated
    public ParsedRun parseInputForReadData(TrackJob trackJob) throws ParsingException;
    
}
