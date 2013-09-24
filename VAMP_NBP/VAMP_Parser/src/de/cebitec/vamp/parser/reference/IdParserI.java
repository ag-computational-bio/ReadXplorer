package de.cebitec.vamp.parser.reference;

import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Observable;
import java.io.File;
import java.util.List;

/**
 * Parser interface for parsers, which only parse sequence identifiers from a 
 * file.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface IdParserI extends Observable {
    
    /**
     * Parses sequence identifiers from a file.
     * @param fileToParse the file to parse
     * @return the list of sequence identifiers
     * @throws ParsingException 
     */
    public List<String> getSequenceIds(File fileToParse) throws ParsingException;
    
}
