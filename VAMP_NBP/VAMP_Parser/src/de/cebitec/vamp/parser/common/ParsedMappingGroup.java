package de.cebitec.vamp.parser.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The dna sequence of all parsed mappings in this group is always identical, but
 * positions and direction deviate.
 * 
 * @author ddoppmeier
 */
public class ParsedMappingGroup {

    private ArrayList<ParsedMapping> mappings;
    private int minError;
    private boolean bestMappingTagged;

    public ParsedMappingGroup() {
        mappings = new ArrayList<ParsedMapping>();
        minError = Integer.MAX_VALUE;
        bestMappingTagged = true;
    }

    public void addParsedMapping(ParsedMapping mapping) {
        // if mapping already existed, increase the count of it
        if (mappings.contains(mapping)) {
            mappings.get(mappings.lastIndexOf(mapping)).increaseCounter();
        } else {
            // otherwise just add it
            mappings.add(mapping);
            bestMappingTagged = false;
            if (mapping.getErrors() < minError){
                minError = mapping.getErrors();
            }
        }
    }

    private void tagBestMatches() {
        Iterator<ParsedMapping> it = mappings.iterator();
        while(it.hasNext()){
            ParsedMapping m = it.next();
            if(m.getErrors() == minError){
                m.setIsBestMapping(true);
            } else {
                m.setIsBestMapping(false);
            }
          
        }
        bestMappingTagged = true;
    }

    public List<ParsedMapping> getMappings(){
        if(!bestMappingTagged){
            tagBestMatches();
        }
        return mappings;
    }
}
