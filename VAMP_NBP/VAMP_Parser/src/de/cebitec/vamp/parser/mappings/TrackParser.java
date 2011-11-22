package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observer;

/**
 * The parser to use for parsing a track.
 *
 * @author ddoppmeier
 */
public class TrackParser implements TrackParserI {
 CoverageContainer coverageContainer ;
    // All parts commented out belong to the RUN domain, which is excluded now!
    // They are left here to provide an easy restore possibility.

    @Override
//    public ParsedTrack parseMappings(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID,
//            String sequenceString, Observer observer) throws ParsingException {
    public ParsedTrack parseMappings(TrackJob trackJob, String sequenceString, Observer observer, CoverageContainer covContainer) throws ParsingException {
        // parse mapping files and store them in appropriate source objects
        MappingParserI mappingp = trackJob.getParser();
        mappingp.registerObserver(observer);
        ParsedMappingContainer mappings = mappingp.parseInput(trackJob, sequenceString);
        mappingp = null;

        // compute the coverage for all mappings
        // CoverageContainer coverageContainer;
        if(!trackJob.isStepwise() || trackJob.isFirstJob()){
       coverageContainer = new CoverageContainer(mappings);
        } else{
        coverageContainer = covContainer;
        coverageContainer.computeCoverage(mappings);
        }
        ParsedTrack track = new ParsedTrack(trackJob.getDescription(), mappings, coverageContainer);
        track.setIsStepwise(trackJob.isStepwise());
        track.setTimestamp(trackJob.getTimestamp());

        mappings = null;
        System.gc();

        return track;
    }
}
