package vamp.parsing.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public class ParsedMappingContainer {

    private HashMap<Integer, ParsedMappingGroup> mappings;

    public ParsedMappingContainer(){
        mappings = new HashMap<Integer, ParsedMappingGroup>();
    }

    public void addParsedMapping(ParsedMapping mapping, int sequenceID){
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

    public int getNumberOfContainedMappings(){
        Collection<ParsedMappingGroup> groups = mappings.values();
        Iterator<ParsedMappingGroup> it = groups.iterator();
        int sum = 0;
        while(it.hasNext()){
            ParsedMappingGroup p = it.next();
            sum += p.getMappings().size();
        }
        return sum;
    }

    @SuppressWarnings("unchecked")
    public HashMap<Integer,Integer> getMappingInformations() {
        HashMap<Integer,Integer> mappingInfos = new HashMap<Integer,Integer>();
        int numberOfBM = 0;
        int numberOfPerfect = 0;
        int numberOfMappedSeq = mappings.keySet().size();
        int numberOfMappings = 0;

        Collection<ParsedMappingGroup> groups = mappings.values();
        Iterator<ParsedMappingGroup> it = groups.iterator();

        while (it.hasNext()) {
            ParsedMappingGroup p = it.next();
            numberOfMappings += p.getMappings().size();
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

            }

    }
         mappingInfos.put(1, numberOfMappings);
         mappingInfos.put(2, numberOfPerfect);
         mappingInfos.put(3, numberOfBM);
         mappingInfos.put(4, numberOfMappedSeq);
         
    return mappingInfos;
}


    public void clear(){
        mappings.clear();
    }
}
