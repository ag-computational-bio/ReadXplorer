package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
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

    @Override
//    public ParsedTrack parseMappings(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID,
//            String sequenceString, Observer observer) throws ParsingException {
    public ParsedTrack parseMappings(TrackJob trackJob, String sequenceString, Observer observer, 
                    CoverageContainer covContainer) throws ParsingException, OutOfMemoryError {
        // parse mapping files and store them in appropriate source objects
        ParsedTrack track = null;
        MappingParserI mappingp = trackJob.getParser();
        mappingp.registerObserver(observer);
        ParsedMappingContainer mappings = mappingp.parseInput(trackJob, sequenceString);
        mappingp = null;

        // compute the coverage for all mappings
        if (!trackJob.isStepwise() || trackJob.isFirstJob()) {
            this.coverageContainer = new CoverageContainer();
        } else {
            this.coverageContainer = covContainer;
        }
        this.coverageContainer.computeCoverage(mappings);
        
        track = new ParsedTrack(trackJob.getDescription(), mappings, coverageContainer);
        track.setIsStepwise(trackJob.isStepwise());
        track.setTimestamp(trackJob.getTimestamp());

        mappings = null;
        System.gc();

        return track;
    }
}
