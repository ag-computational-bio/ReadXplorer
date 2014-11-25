package de.cebitec.readXplorer.parser.reference;

import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.util.Observable;
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
