package vamp.parsing.common;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ddoppmeier
 */
public class ParsedRun {

    HashMap<String, ParsedReadname> sequences;
    private HashMap<String, String> errorMap;
    private String description;
    private Timestamp timestamp;
    private long id;
    HashSet<String> s = new HashSet<String>();
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ParsedRun(String description){
        this.description = description;
        sequences = new HashMap<String, ParsedReadname>();
        errorMap = new HashMap<String, String>();
    }

    public String getDescription(){
        return description;
    }
/*
 * this Method returns a Collection of reads
 */
    public Collection<ParsedReadname> getReads(){
        return sequences.values();
    }

        public Set<String> getSequences(){
        return sequences.keySet();
    }




   //ParsedReadname contains the names of the reads with the same sequence
    public void addReadData(String sequence, String readName) throws OutOfMemoryError{
        if(!sequences.containsKey(sequence)){
            sequences.put(sequence, new ParsedReadname());
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

    public void deleteMap(){
        sequences.clear();
    }

    }
