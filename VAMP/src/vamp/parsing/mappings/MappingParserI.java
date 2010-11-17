package vamp.parsing.mappings;

import vamp.parsing.common.*;
import java.util.HashMap;
import vamp.parsing.common.ParsingException;
import vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI {

    public ParsedMappingContainer parseInput(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException;

    public ParsedRun parseInputForReadData(TrackJobs trackJob)throws ParsingException;

}
