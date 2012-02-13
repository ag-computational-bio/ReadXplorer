package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 * @author ddoppmeier, rhilker
 * 
 * A genome request can be any request for any genome data. It is defined by at
 * least three essential parameters: The left and right interval borders for the
 * interval of the genome, which should be checked and a ThreadListener, who
 * wants to receive the results of this request.
 */
public class GenomeRequest {
    
    private int from;
    private int to;
    private ThreadListener sender;
    private byte desiredCoverage;
    private PersistantFeature feature;

    /**
     * A genome request can be any request for any genome data. It is defined by at
     * least three essential parameters: The left and right interval borders for the
     * interval of the genome, which should be checked and a ThreadListener, who
     * wants to receive the results of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param sender the sending object, that wants to receive the result of the request
     * @param desiredCoverage A value among Properties.PERFECT_COVERAGE = 1, 
     * BEST_MATCH_COVERAGE = 2 or COMMON_COVERAGE = 3, depending on the coverage that is needed.
     */
    public GenomeRequest(int from, int to, ThreadListener sender, byte desiredCoverage){
        this.from = from;
        this.to = to;
        this.sender = sender;
        this.desiredCoverage = desiredCoverage;
        this.feature = null;
    }
        
    /**
     * A genome request can be any request for any genome data. It is defined by at
     * least three essential parameters: The left and right interval borders for the
     * interval of the genome, which should be checked and a ThreadListener, who
     * wants to receive the results of this request.
     * @param from start position of the interval for receiving the mappings
     * @param to stop position of the interval for receiving the mappings
     * @param sender the sending object, that wants to receive the result of the request
     * @param feature the feature belonging to the request
     */
    public GenomeRequest(int from, int to, ThreadListener sender, PersistantFeature feature){
        this.from = from;
        this.to = to;
        this.sender = sender;
        this.feature = feature;
        this.desiredCoverage = 0;
    }

    public int getFrom() {
        return this.from;
    }

    public ThreadListener getSender() {
        return this.sender;
    }

    public int getTo() {
        return this.to;
    }
    
    /**
     * @return A value among Properties.PERFECT_COVERAGE = 1, 
     * BEST_MATCH_COVERAGE = 2 or COMMON_COVERAGE = 3 or 0, if it is not set.
     */
    public byte getDesiredCoverage(){
        return this.desiredCoverage;
    }

    public PersistantFeature getFeature() {
        return this.feature;
    }
    
    

}
