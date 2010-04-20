package vamp.parsing.mappings;

import vamp.parsing.common.*;
import java.util.HashMap;
import vamp.parsing.common.ParsingException;
import vamp.importer.TrackJob;

/**
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI {

    public ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException;

}
