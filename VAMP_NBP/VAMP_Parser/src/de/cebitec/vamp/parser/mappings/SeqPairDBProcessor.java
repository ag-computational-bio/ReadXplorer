package de.cebitec.vamp.parser.mappings;

import de.cebitec.vamp.parser.common.ParsingException;
import de.cebitec.vamp.util.Properties;
import java.util.HashMap;

/**
 * A SeqPairDBProcessor stores a mapping between sequence id
 * and readname. This mapping is needed to connect the 2 reads belonging 
 * to a sequence pair sequencing run.
 * @deprecated Storing of data sets in the DB is not allowed anymore
 *
 * @author Rolf Hilker
 */
@Deprecated
public class SeqPairDBProcessor implements SeqPairProcessorI {
    
    private HashMap<String, Integer> seqIDToReadNameMap1;
    private HashMap<String, Integer> seqIDToReadNameMap2;
    private char pairInfo;

    /**
     * A SeqPairDBProcessor extends the SAMBAMParser, because it needs to alter
     * the processReadname method in order to store a mapping between sequence
     * id and readname. This mapping is needed to connect the 2 mapping files
     * belonging to a sequence pair sequencing run.
     */
    public SeqPairDBProcessor() {
        this.seqIDToReadNameMap1 = new HashMap<>();
        this.seqIDToReadNameMap2 = new HashMap<>();
    }

    /**
     * It stores a mapping of readname to sequence id to
     * identify pairs later. The last character of the readname is cutted, as it 
     * is assumed that it contains the read pair information('1' & '2' or 'f' 
     * & 'r' for read1 and read2).
     */ 
    @Override
    public void processReadname(int seqID, String readName) throws ParsingException {
        /* seq pair formats: 
         * - illumina old: /1 (/2) at end
         * - illumina casava > 1.8: " 1:" (" 2:") after first space <- not supported here
         * - 454: .f (.r) at end
         * Add readname to corresponding map
         */
        //TODO: plug in readname endshorter and only store relevant part of read name, if possible
        pairInfo = readName.charAt(readName.length() - 1);
        readName = readName.substring(0, readName.length()-2);
        if (pairInfo == Properties.EXT_A1 || pairInfo == Properties.EXT_B1) {
            if (!this.seqIDToReadNameMap1.containsKey(readName)) {
                //since seqID will always be the same for all reads with identical sequence
                this.seqIDToReadNameMap1.put(readName, seqID);
            }
        } else if (pairInfo == Properties.EXT_A2 || pairInfo == Properties.EXT_B2) {
            if (!this.seqIDToReadNameMap2.containsKey(readName)) {
                this.seqIDToReadNameMap2.put(readName, seqID);
            }
        } else {
            throw new ParsingException("Read " + readName + " doesn't contain sequence pair extension ('1' & '2' or 'f' & 'r'). Read is ignored for the sequence pair import!");
        }
        
    }

    @Override
    public HashMap<String, Integer> getReadNameToSeqIDMap1() {
        return (HashMap<String, Integer>) this.seqIDToReadNameMap1.clone();
    }
    
    @Override
    public HashMap<String, Integer> getReadNameToSeqIDMap2() {
        return (HashMap<String, Integer>) this.seqIDToReadNameMap2.clone();
    }
    
    @Override
    public void resetSeqIdToReadnameMaps(){
        this.seqIDToReadNameMap1.clear();
        this.seqIDToReadNameMap2.clear();
    }

}
