package vamp.parsing.common;

import java.util.ArrayList;

/**
 *
 * @author ddoppmeier
 */
public class ParsedReadname {

    private ArrayList<String> reads;
    private long id;
    public int numOfReads;
    public ParsedReadname(){
        reads = new ArrayList<String>();
    }

    public void addRead(String readname){
        if(!reads.contains(readname)){
        reads.add(readname);
        }
    }

    public ArrayList<String> getReads(){
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
