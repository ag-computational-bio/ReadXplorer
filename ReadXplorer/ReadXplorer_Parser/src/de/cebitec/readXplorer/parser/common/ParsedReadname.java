package de.cebitec.readXplorer.parser.common;

import java.util.HashSet;

/**
 * Since the RUN domain has been excluded a PersistantRun is not needed anymore!
 * Should be deprecated as long as it is not needed for anything else.
 *
 * @author ddoppmeier
 */
@Deprecated
public class ParsedReadname {

    private HashSet<String> reads;
    private long id;
    public int numOfReads;
    
    public ParsedReadname(){
        reads = new HashSet<String>();
    }

    public void addRead(String readname){
        reads.add(readname);
    }

    public HashSet<String> getReads(){
        return reads;
    }

    public int getNumOfReads() {
        return reads.size();
    }

    public long getID() {
        return id;
    }

    public void setID(long seqID) {
        this.id = seqID;
    }

}
