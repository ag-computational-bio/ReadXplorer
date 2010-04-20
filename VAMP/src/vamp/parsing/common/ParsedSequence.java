package vamp.parsing.common;

import java.util.ArrayList;

/**
 *
 * @author ddoppmeier
 */
public class ParsedSequence {

    private ArrayList<String> reads;
    private long id;

    public ParsedSequence(){
        reads = new ArrayList<String>();
    }

    public void addRead(String readname){
        reads.add(readname);
    }

    public ArrayList<String> getReads(){
        return reads;
    }

    public long getID() {
        return id;
    }

    public void setID(long seqID) {
        this.id = seqID;
    }
}
