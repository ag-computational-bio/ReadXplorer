package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.parser.common.ParsedMapping;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ddoppmeier
 */
public class ParsedMappingGroup {

    private ArrayList<ParsedMapping> mappings;
    private int minError;
    private boolean bestMappingTagged;

    public ParsedMappingGroup(){
        mappings = new ArrayList<ParsedMapping>();
        minError = Integer.MAX_VALUE;
        bestMappingTagged = true;
    }

    public void addParsedMapping(ParsedMapping mapping){
        // if mapping already existed, increase the count of it
        if(mappings.contains(mapping)){
            mappings.get(mappings.lastIndexOf(mapping)).increaseCounter();
        } else {
            // otherwise just add it
            mappings.add(mapping);
            bestMappingTagged = false;
            if(mapping.getErrors() < minError){
                minError = mapping.getErrors();
            }
        }
    }

    private void tagBestMatches(){
        for(Iterator<ParsedMapping> it = mappings.iterator(); it.hasNext();){
            ParsedMapping m = it.next();
            if(m.getErrors() == minError){
                m.setIsBestmapping(true);
            } else {
                m.setIsBestmapping(false);
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
