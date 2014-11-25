package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;
import de.cebitec.vamp.parser.common.ParsingException;

/**
 * Interface for sequence pair classifier implementation.
 * 
 * @author Rolf Hilker
 */
public interface SeqPairClassifierI extends PreprocessorI {
    
    /**
     * Carries out calculations and returns the container containing all necessary
     * data for storing the sequence pairs.
     * @return seq pair container
     * @throws ParsingException
     * @throws OutOfMemoryError  
     */
    public ParsedSeqPairContainer classifySeqPairs() throws ParsingException, OutOfMemoryError;

}
