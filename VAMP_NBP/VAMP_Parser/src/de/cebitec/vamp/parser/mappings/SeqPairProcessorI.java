package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.ParsingException;
import java.util.HashMap;

/**
 * Interface contains the additional methods a parser for sequence pairs needs.
 *
 * @author Rolf Hilker
 */
public interface SeqPairProcessorI {
    
    /**
     * Processes the read name according to the specific sequence pair processor
     * implementation. 
     * @param seqID sequence pair id which should be associated to the read name
     * @param readName read name to which a sequence pair id should be associated
     * @throws ParsingException  
     */
    public void processReadname(int seqID, String readName) throws ParsingException;
    
    /**
     * @return the mapping between sequence pair id and readname for reads with
     * extension '1' or 'f'.
     */
    public HashMap<String, Integer> getReadNameToSeqIDMap1();
    
    /**
     * @return the mapping between sequence pair id and readname for reads with
     * extension '2' or 'r'.
     */
    public HashMap<String, Integer> getReadNameToSeqIDMap2();
    
    /**
     * Clears the data structures of this parser to save memory.
     */
    public void resetSeqIdToReadnameMaps();

}
