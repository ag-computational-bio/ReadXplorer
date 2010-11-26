package de.cebitec.vamp.databackend;

/**
 *
 * @author ddoppmeier
 */
public class CoverageRequest {

    private int from;
    private int to;
    private CoverageThreadListener sender;

    public CoverageRequest(int from, int to, CoverageThreadListener sender){
        this.from = from;
        this.to = to;
        this.sender = sender;
    }

    public int getFrom() {
        return from;
    }

    public CoverageThreadListener getSender() {
        return sender;
    }

    public int getTo() {
        return to;
    }

}
