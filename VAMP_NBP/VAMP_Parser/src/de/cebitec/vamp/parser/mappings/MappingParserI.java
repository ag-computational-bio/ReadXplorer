package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.parser.common.ParsedMappingContainer;
import de.cebitec.vamp.parser.common.ParsedRun;
import de.cebitec.vamp.parser.common.ParserI;
import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observable;
//import java.util.HashMap;

/**
 * Interface to be implemented for all mapping parsers.
 *
 * @author ddoppmeier
 */
public interface MappingParserI extends ParserI, Observable {

    //public abstract ParsedMappingContainer parseInput(TrackJob trackJob, HashMap<String, Integer> readnameToSequenceID, String sequenceString) throws ParsingException;
    public abstract ParsedMappingContainer parseInput(TrackJob trackJob, String sequenceString) throws ParsingException;
    
    /**
     * Handles readnames. In an ordinary parser it only counts the reads while
     * in a sequence pair parser it stores a mapping of readname to sequence id to
     * identify pairs later. The last two characters of the readname are cutted, as it is
     * assumed that they contain the read pair information (e.g. /1 & /2 or -1 & -2 for read1 and read2).
     * @param seqID sequence id of current read
     * @param readName readname of current read
     */
    public void processReadname(final int seqID, final String readName);

}
