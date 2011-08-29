package de.cebitec.vamp.parser.mappings;

import java.util.HashMap;

/**
 * A SAMSeqPairParser extends the SAMParser, because it needs to alter
 * the processReadname method in order to store a mapping between sequence id
 * and readname. This mapping is needed to connect the 2 mapping files belonging to
 * a sequence pair sequencing run.
 *
 * @author Rolf Hilker
 */
public class SAMSeqPairParser extends JokParser implements PairedDataParserI {

    private HashMap<String, Integer> seqIDToReadNameMap;

    public SAMSeqPairParser(){
        this.seqIDToReadNameMap = new HashMap<String, Integer>();
    }

    @Override
    public void processReadname(final int seqID, final String readName) {
        if (!this.seqIDToReadNameMap.containsKey(readName)){
            //since seqID will always be the same for all reads with identical sequence
            this.seqIDToReadNameMap.put(readName, seqID);
        }
    }

    @Override
    public HashMap<String, Integer> getSeqIDToReadNameMap() {
        return this.seqIDToReadNameMap;
    }
}
