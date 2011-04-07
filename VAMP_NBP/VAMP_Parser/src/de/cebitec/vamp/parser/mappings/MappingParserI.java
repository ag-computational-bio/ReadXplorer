package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJobs;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observable;
import java.util.HashMap;

/**
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI, Observable {

    public abstract ParsedMappingContainer parseInput(TrackJobs trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException;

    public ParsedRun parseInputForReadData(TrackJobs trackJob)throws ParsingException;

}
