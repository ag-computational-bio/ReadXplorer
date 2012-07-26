package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.ParsingException;
import java.util.HashMap;

/**
 * Interface contains the additional methods a parser for sequence pairs needs.
 *
 * @author Rolf Hilker
 */
public interface SeqPairProcessorI {

    /** 'Yc' = Tag for read classification in one of the three vamp classes. */
    public static final String TAG_READ_CLASS = "Yc";
    /** 'Yt' = Tag for number of positions a sequence maps to in a reference. */
    public static final String TAG_MAP_COUNT = "Yt";
    /** 'Yi' = Tag for the sequence pair id. */
    public static final String TAG_SEQ_PAIR_ID = "Yi";
    /** 'Ys' = Tag for the sequence pair type. */
    public static final String TAG_SEQ_PAIR_TYPE = "Ys";
    
    //Supported sequence pair extensions.
    /** 1 = Supported extension of read 1. */
    public static final char EXT_A1 = '1';
    /** 2 = Supported extension of read 2. */
    public static final char EXT_A2 = '2';
    /** f = Supported extension of read 1. */
    public static final char EXT_B1 = 'f';
    /** r = Supported extension of read 2. */
    public static final char EXT_B2 = 'r';
    
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
