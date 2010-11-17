package vamp.parsing.mappings;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import vamp.importer.TrackJobs;
import vamp.parsing.common.CoverageContainer;
import vamp.parsing.common.ParsedMappingContainer;
import vamp.parsing.common.ParsedRun;
import vamp.parsing.common.ParsedTrack;
import vamp.parsing.common.ParsingException;

/**
 *
 * @author ddoppmeier
 */
public class TrackParser implements TrackParserI {

    @Override
    public ParsedTrack parseMappings(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException {

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

    @Override
    public ParsedRun parseMappingforReadData(TrackJobs trackJob) throws ParsingException {

        MappingParserI mappingp = trackJob.getParser();
        ParsedRun run= mappingp.parseInputForReadData(trackJob);
        return run;

    }

}
