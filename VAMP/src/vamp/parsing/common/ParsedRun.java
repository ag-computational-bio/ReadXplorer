package vamp.parsing.common;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ddoppmeier
 */
public class ParsedRun {
    
    ConcurrentHashMap<String, ParsedSequence> sequences;
    private HashMap<String, String> errorMap;
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
        sequences = new ConcurrentHashMap<String, ParsedSequence>();
        errorMap = new HashMap<String, String>();
    }

    public String getDescription(){
        return description;
    }

    public Collection<ParsedSequence> getSequences(){
        return sequences.values();
    }
//Parsed Sequence contains the names of the reads with the same sequence
    public void addReadData(String sequence, String readName) throws OutOfMemoryError{

        if(!sequences.containsKey(sequence)){
            sequences.put(sequence, new ParsedSequence());
        }

        sequences.get(sequence).addRead(readName);
    }

    @SuppressWarnings("unchecked")
    public void addErrorList(HashMap errorMap){
        this.errorMap = errorMap;
    }

    /**
     *
     * @return
     */
    public HashMap getErrorList(){
        return errorMap;
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
