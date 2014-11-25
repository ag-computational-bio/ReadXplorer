package de.cebitec.vamp.databackend;

/**
 * An interval request can be any request for any interval data. It is
 * defined by at least three essential parameters: The left and right interval
 * borders for the interval under investigation and a ThreadListener, who wants
 * to receive the results of this request.
 * 
 * @author ddoppmeier, rhilker
 */
public class IntervalRequest {
    
    private int from;
    private int to;
    private ThreadListener sender;
    private byte desiredData;

    /**
     * An interval request can be any request for any interval data. It is defined
     * by at least three essential parameters: The left and right interval borders for the
     * interval under investigation and a ThreadListener, who wants to receive the results 
     * of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param sender the sending object, that wants to receive the result of the request
     * @param desiredData Can be any byte value representing a filter flag for the results.
     * E.g used for the desired coverage in a coverage request, which is among: 
     * Properties.PERFECT_COVERAGE = 1, BEST_MATCH_COVERAGE = 2 or COMMON_COVERAGE = 3, 
     * depending on the coverage that is needed.
     */
    public IntervalRequest(int from, int to, ThreadListener sender, byte desiredData) {
        this.from = from;
        this.to = to;
        this.sender = sender;
        this.desiredData = desiredData;
    }
        
    /**
     * An interval request can be any request for any interval data. It is defined 
     * by at least three essential parameters: The left and right interval borders for the
     * interval under investigation and a ThreadListener, who wants to receive the results 
     * of this request.
     * @param from start position of the interval
     * @param to end position of the interval
     * @param sender the sending object, that wants to receive the result of the request
     */    
    public IntervalRequest(int from, int to, ThreadListener sender) {
        this.from = from;
        this.to = to;
        this.sender = sender;
        this.desiredData = 0;
    }

    /**
     * @return start position of the interval under investigation
     */
    public int getFrom() {
        return this.from;
    }

    /**
     * @return end position of the interval under investigation
     */
    public int getTo() {
        return this.to;
    }

    /**
     * @return the sending object, that wants to receive the result of the request
     */
    public ThreadListener getSender() {
        return this.sender;
    }
    
    /**
     * @return Any byte value representing a filter flag for the results.
     * E.g used for the desired coverage in a coverage request, which is among: 
     * Properties.PERFECT_COVERAGE = 1, BEST_MATCH_COVERAGE = 2 or COMMON_COVERAGE = 3, 
     * depending on the coverage that is needed.
     */
    public byte getDesiredData() {
        return this.desiredData;
    } 

}
