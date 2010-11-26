package de.cebitec.vamp.parsing.mappings;

import java.util.HashMap;
import de.cebitec.vamp.parsing.common.ParsingException;
import de.cebitec.vamp.importer.TrackJobs;
import de.cebitec.vamp.parsing.common.ParsedRun;
import de.cebitec.vamp.parsing.common.ParsedTrack;

/**
 *
 * @author ddoppmeier
 */
public interface TrackParserI {

    public ParsedTrack parseMappings(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException;

    public ParsedRun parseMappingforReadData(TrackJobs trackJob)throws ParsingException;

}
