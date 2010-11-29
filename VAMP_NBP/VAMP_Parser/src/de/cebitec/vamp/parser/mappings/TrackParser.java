package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsingException;
import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public class TrackParser implements TrackParserI {

    @Override
    public ParsedTrack parseMappings(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException {
        // parse mapping files and store them in appropriate source objects
        MappingParserI mappingp = trackJob.getParser();
        ParsedMappingContainer mappings = mappingp.parseInput(trackJob, readnameToSequenceID, sequenceString);
        
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
