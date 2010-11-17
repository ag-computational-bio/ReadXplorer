package vamp.parsing.mappings;

import vamp.parsing.common.*;
import java.util.HashMap;
import vamp.parsing.common.ParsingException;
import vamp.importer.TrackJobs;

/**
 *
 * @author ddoppmeier
 */
public interface TrackParserI {

    public ParsedTrack parseMappings(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException;

    public ParsedRun parseMappingforReadData(TrackJobs trackJob)throws ParsingException;

}
