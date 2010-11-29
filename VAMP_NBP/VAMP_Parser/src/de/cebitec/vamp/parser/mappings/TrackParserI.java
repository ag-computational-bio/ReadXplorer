package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParsedTrack;
import de.cebitec.vamp.parser.common.ParsingException;
import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public interface TrackParserI {

    public abstract ParsedTrack parseMappings(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException;

    public ParsedRun parseMappingforReadData(TrackJobs trackJob)throws ParsingException;

}
