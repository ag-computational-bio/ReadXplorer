package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.parser.common.ParsedMapping;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier
 */
public class ParsedMappingContainer {
    int noOfMappings = 0;
    private int numberOfUniqueSeq ;
    private int numberOfReads;
    private HashMap<Integer, ParsedMappingGroup> mappings;

    public ParsedMappingContainer(){
        mappings = new HashMap<Integer, ParsedMappingGroup>();
    }

    public void addParsedMapping(ParsedMapping mapping, int sequenceID){
        noOfMappings++;
        if(!mappings.containsKey(sequenceID)){
            mappings.put(sequenceID, new ParsedMappingGroup());
        }
        mappings.get(sequenceID).addParsedMapping(mapping);
    }

    public Collection<Integer> getMappedSequenceIDs(){
        return mappings.keySet();
    }

    public ParsedMappingGroup getParsedMappingGroupBySeqID(int sequenceID){
        return mappings.get(sequenceID);
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Integer,Integer> getMappingInformations() {
        HashMap<Integer,Integer> mappingInfos = new HashMap<Integer,Integer>();
        int numberOfBM = 0;
        int numberOfPerfect = 0;
        //is the number of unique Mapped Sequences
        int numberOfMappedSeq = mappings.size();
        //the number of created Mappings by the mapper
        int numberOfMappings = 0;

        Collection<ParsedMappingGroup> groups = mappings.values();
        Iterator<ParsedMappingGroup> it = groups.iterator();

        while (it.hasNext()) {
            ParsedMappingGroup p = it.next();
            List<ParsedMapping> mappingList = p.getMappings();
            Iterator maps = mappingList.iterator();
            while (maps.hasNext()) {
                ParsedMapping m = (ParsedMapping) maps.next();
                if(m.isBestMapping() == true) {
                        numberOfBM++;
                    if(m.getErrors() == 0){
                         numberOfPerfect++;
                    }
                }
                numberOfMappings += m.getCount();
            }
    }
         mappingInfos.put(1, numberOfMappings);
         mappingInfos.put(2, numberOfPerfect);
         mappingInfos.put(3, numberOfBM);
         mappingInfos.put(4, numberOfMappedSeq);
         mappingInfos.put(5, numberOfReads);
         mappingInfos.put(6, numberOfUniqueSeq);
      
    return mappingInfos;
}


    public void clear(){
        mappings.clear();
    }

    public void setNumberOfReads(int numberOfReads) {
        this.numberOfReads = numberOfReads;
    }

    public void setNumberOfUniqueSeq(int numberOfUniqueSeq) {
        this.numberOfUniqueSeq = numberOfUniqueSeq;
    }


    

}
