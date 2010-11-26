package de.cebitec.vamp.parsing.mappings;

import de.cebitec.vamp.importer.TrackJobs;
import de.cebitec.vamp.parsing.common.ParsedMappingContainer;
import de.cebitec.vamp.parsing.common.ParsedRun;
import de.cebitec.vamp.parsing.common.ParserI;
import de.cebitec.vamp.parsing.common.ParsingException;
import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI {

    public ParsedMappingContainer parseInput(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID) throws ParsingException;

    public ParsedRun parseInputForReadData(TrackJobs trackJob)throws ParsingException;

}
