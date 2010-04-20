package vamp.parsing.common;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author ddoppmeier
 */
public class ParsedRun {

    private HashMap<String, ParsedSequence> sequences;
    private String description;
    private Timestamp timestamp;
    private long id;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ParsedRun(String description){
        this.description = description;
        sequences = new HashMap<String, ParsedSequence>();
    }

    public String getDescription(){
        return description;
    }

    public Collection<ParsedSequence> getSequences(){
        return sequences.values();
    }

    public void addReadData(String sequence, String readName){

        if(!sequences.containsKey(sequence)){
            sequences.put(sequence, new ParsedSequence());
        }

        sequences.get(sequence).addRead(readName);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Iterator<String> it = sequences.keySet().iterator(); it.hasNext() ; ){
            String sequence = it.next();
            sb.append(sequence+" "+sequences.get(sequence).getReads().size()+"\n");
        }

        return sb.toString();
    }

    public void setID(long runID) {
        this.id = runID;
    }

    public long getID(){
        return id;
    }

}
