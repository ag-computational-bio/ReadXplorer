package de.cebitec.readXplorer.parser.mappings;

import de.cebitec.readXplorer.parser.common.ParsedReadPairContainer;
import de.cebitec.readXplorer.parser.common.ParsingException;

/**
 * Interface for sequence pair classifier implementation.
 * 
 * @author Rolf Hilker
 */
public interface ReadPairClassifierI extends PreprocessorI {
    
    /**
     * Carries out calculations and returns the container containing all necessary
     * data for storing the sequence pairs.
     * @return seq pair container
     * @throws ParsingException
     * @throws OutOfMemoryError  
     */
    public ParsedReadPairContainer classifySeqPairs() throws ParsingException, OutOfMemoryError;

}
