package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.CoverageContainer;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Benchmark;
import de.cebitec.vamp.util.Observer;
import org.openide.util.NbBundle;

/**
 * The parser to use for parsing a track.
 *
 * @author ddoppmeier
 */
public class TrackParser implements TrackParserI {
 
    CoverageContainer coverageContainer ;

    @Override
    public ParsedTrack parseMappings(TrackJob trackJob, String sequenceString, Observer observer, 
                    CoverageContainer covContainer) throws ParsingException, OutOfMemoryError {
        long start = System.currentTimeMillis();
        
        // parse mapping files and store them in appropriate source objects
        ParsedTrack track = null;
        MappingParserI mappingp = trackJob.getParser();
        mappingp.registerObserver(observer);
        Object parsedData = mappingp.parseInput(trackJob, sequenceString);
        if (parsedData instanceof ParsedMappingContainer) {
            ParsedMappingContainer mappings = (ParsedMappingContainer) parsedData;

            // compute the coverage for all mappings
            if (!trackJob.isStepwise() || trackJob.isFirstJob()) {
                this.coverageContainer = new CoverageContainer();
            } else {
                this.coverageContainer = covContainer;
            }
            this.coverageContainer.computeCoverage(mappings);

            track = new ParsedTrack(trackJob.getID(), trackJob.getDescription(), mappings, coverageContainer, trackJob.getRefGen().getID());
            track.setIsStepwise(trackJob.isStepwise());
            track.setTimestamp(trackJob.getTimestamp());

            mappings = null;
        }
        mappingp = null;
        System.gc();
        
        long finish = System.currentTimeMillis();
        String msg = NbBundle.getMessage(SamBamDirectParser.class, "Parser.Parsing.Successfully", trackJob.getFile().getName());
        observer.update(Benchmark.calculateDuration(start, finish, msg));

        return track;
    }
}
