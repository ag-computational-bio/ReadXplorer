package de.cebitec.vamp.parser.common;

import java.util.HashSet;

/**
 *
 * @author ddoppmeier
 */
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
