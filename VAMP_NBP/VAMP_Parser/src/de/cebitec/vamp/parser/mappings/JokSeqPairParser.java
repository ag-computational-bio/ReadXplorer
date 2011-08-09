package de.cebitec.vamp.parser.mappings;

import java.util.HashMap;

/**
 * A JokSeqPairParser extends the JokParser, because it needs to alter
 * the processReadname method in order to store a mapping between sequence id
 * and readname. This mapping is needed to connect the 2 mapping files belonging to
 * a sequence pair sequencing run.
 *
 * @author Rolf Hilker
 */
public class JokSeqPairParser extends JokParser implements SequencePairParserI {

    private HashMap<String, Integer> seqIDToReadNameMap;

    public JokSeqPairParser(){
        this.seqIDToReadNameMap = new HashMap<String, Integer>();
    }

    @Override
    public void processReadname(final int seqID, String readName) {
        readName = readName.substring(0, readName.length()-2);
        //HWI-ST486_0090:5:1101:7627:28600#ACTTGA/2
//        List<String> readnames = new ArrayList<String>();
//        if (this.seqIDToReadNameMap.containsKey(seqID)){
//            this.seqIDToReadNameMap.get(seqID).add(readName);
//        } else {
//            readnames.add(readName);
//            this.seqIDToReadNameMap.put(seqID, readnames);
//        }
        if (!this.seqIDToReadNameMap.containsKey(readName)){
            //since seqID will always be the same for all reads with identical sequence
            //there will be more than one readName with the same sequence id (if sequence is identical)
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
