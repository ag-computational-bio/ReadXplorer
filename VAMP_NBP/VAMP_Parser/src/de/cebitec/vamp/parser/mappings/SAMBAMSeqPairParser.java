package de.cebitec.vamp.parser.mappings;

import java.util.HashMap;

/**
 * A SAMBAMSeqPairParser extends the SAMBAMParser, because it needs to alter
 * the processReadname method in order to store a mapping between sequence id
 * and readname. This mapping is needed to connect the 2 mapping files belonging to
 * a sequence pair sequencing run.
 *
 * @author Rolf Hilker
 */
public class SAMBAMSeqPairParser extends SAMBAMParser implements SequencePairParserI {

    private HashMap<String, Integer> seqIDToReadNameMap;

    public SAMBAMSeqPairParser() {
        this.seqIDToReadNameMap = new HashMap<String, Integer>();
    }

    @Override
    public void processReadname(int seqID, String readName) {
        readName = readName.substring(0, readName.length()-2);
        if (!this.seqIDToReadNameMap.containsKey(readName)){
            //since seqID will always be the same for all reads with identical sequence
            this.seqIDToReadNameMap.put(readName, seqID);
        }
    }

    @Override
    public HashMap<String, Integer> getSeqIDToReadNameMap() {
        return (HashMap<String, Integer>) this.seqIDToReadNameMap.clone();
    }
    
    @Override
    public void resetSeqIdToReadnameMap(){
        this.seqIDToReadNameMap = new HashMap<String, Integer>();
    }
}
