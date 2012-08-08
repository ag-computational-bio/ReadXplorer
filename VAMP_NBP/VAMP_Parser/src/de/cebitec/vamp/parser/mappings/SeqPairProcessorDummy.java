package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.ParsingException;
import java.util.HashMap;

/**
 * Dummy class for parsing a track which does not belong to a sequence pair import.
 * It only contains method stubs.
 *
 * @author -Rolf Hilker-
 */
public class SeqPairProcessorDummy implements SeqPairProcessorI {

    /**
     * Dummy method that does nothing.
     */
    @Override
    public void processReadname(int seqID, String readName) throws ParsingException {
        //do nothing
    }

    /**
     * Dummy method that returns an empty map.
     */
    @Override
    public HashMap<String, Integer> getReadNameToSeqIDMap1() {
        return new HashMap<String, Integer>();
    }
    
    /**
     * Dummy method that returns an empty map.
     */
    @Override
    public HashMap<String, Integer> getReadNameToSeqIDMap2() {
        return new HashMap<String, Integer>();
    }

    /**
     * Dummy method that does nothing.
     */
    @Override
    public void resetSeqIdToReadnameMaps() {
        //do nothing
    }
    
}
