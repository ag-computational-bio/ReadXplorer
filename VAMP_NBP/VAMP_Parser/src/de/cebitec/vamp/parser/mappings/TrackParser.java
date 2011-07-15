package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observer;
//import java.util.HashMap;

/**
 * The parser to use for parsing a track.
 *
 * @author ddoppmeier
 */
public class TrackParser implements TrackParserI {

    // All parts commented out belong to the RUN domain, which is excluded now!
    // They are left here to provide an easy restore possibility.

    @Override
//    public ParsedTrack parseMappings(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID,
//            String sequenceString, Observer observer) throws ParsingException {
    public ParsedTrack parseMappings(TrackJob trackJob, String sequenceString, Observer observer) throws ParsingException {
        // parse mapping files and store them in appropriate source objects
        MappingParserI mappingp = trackJob.getParser();
        mappingp.registerObserver(observer);
//        ParsedMappingContainer mappings = mappingp.parseInput(trackJob, readnameToSequenceID, sequenceString);
        ParsedMappingContainer mappings = null;
        mappings = mappingp.parseInput(trackJob, sequenceString);

        // release resources
//        readnameToSequenceID = null;
//        mappingp = null; //TODO: woanders leeren

        // compute the coverage for all mappings
        CoverageContainer coverageContainer = new CoverageContainer(mappings);

        ParsedTrack track = new ParsedTrack(trackJob.getDescription(), mappings, coverageContainer);
        track.setTimestamp(trackJob.getTimestamp());

        mappings = null;
        System.gc();

        return track;
    }

    @Override
    public ParsedRun parseMappingforReadData(TrackJob trackJob) throws ParsingException {
        MappingParserI mappingp = trackJob.getParser();
        ParsedRun run = mappingp.parseInputForReadData(trackJob);
        return run;
    }
}
