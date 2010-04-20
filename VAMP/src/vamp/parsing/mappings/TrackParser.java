package vamp.parsing.mappings;

import java.util.HashMap;
import vamp.importer.TrackJob;
import vamp.parsing.common.CoverageContainer;
import vamp.parsing.common.ParsedMappingContainer;
import vamp.parsing.common.ParsedTrack;
import vamp.parsing.common.ParsingException;

/**
 *
 * @author ddoppmeier
 */
public class TrackParser implements TrackParserI {

    @Override
    public ParsedTrack parseMappings(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException {

        // parse mapping files and store them in appropriate source objects
        MappingParserI mappingp = trackJob.getParser();
        ParsedMappingContainer mappings = mappingp.parseInput(trackJob, readnameToSequenceID);
        
        // release ressources
        readnameToSequenceID = null;
        mappingp = null;

        // compute the coverage for all mappings
        CoverageContainer coverageContainer = new CoverageContainer(mappings);

        ParsedTrack track = new ParsedTrack(trackJob.getDescription(), mappings, coverageContainer);
        track.setTimestamp(trackJob.getTimestamp());

        return track;
    }

}
