package vamp.parsing.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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

}
