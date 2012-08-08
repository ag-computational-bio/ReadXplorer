package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.ParsedSeqPairContainer;

/**
 * Interface for sequence pair classifier implementation.
 * 
 * @author Rolf Hilker
 */
public interface SeqPairClassifierI {
    
    /**
     * Carries out calculations and returns the container containing all necessary
     * data for storing the sequence pairs.
     * @return seq pair container
     */
    public ParsedSeqPairContainer classifySeqPairs();

}
