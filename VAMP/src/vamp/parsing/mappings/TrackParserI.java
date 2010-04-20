package vamp.parsing.mappings;

import vamp.parsing.common.*;
import java.util.HashMap;
import vamp.parsing.common.ParsingException;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public interface TrackParserI {

    public ParsedTrack parseMappings(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException;

}
