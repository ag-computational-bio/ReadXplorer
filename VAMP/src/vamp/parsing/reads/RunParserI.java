package vamp.parsing.reads;

import vamp.parsing.common.*;
import vamp.parsing.common.ParsingException;
import vamp.importer.RunJob;


/**
 *
 * @author ddoppmeier
 */
public interface RunParserI extends ParserI {

    public ParsedRun parseRun(RunJob runJob) throws ParsingException;

}
