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
public class JokSeqPairParser implements SeqPairProcessorI {

    private HashMap<String, Integer> seqIDToReadNameMap1;
    private HashMap<String, Integer> seqIDToReadNameMap2;
    //TODO: check if implementation of seqIdToReadNameMap2 is needed and seq pair import is still ok with only one map

    public JokSeqPairParser(){
        this.seqIDToReadNameMap1 = new HashMap<String, Integer>();
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
        if (!this.seqIDToReadNameMap1.containsKey(readName)){
            //since seqID will always be the same for all reads with identical sequence
            //there will be more than one readName with the same sequence id (if sequence is identical)
            this.seqIDToReadNameMap1.put(readName, seqID);
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
        this.seqIDToReadNameMap1 = new HashMap<String, Integer>();
    }

}
