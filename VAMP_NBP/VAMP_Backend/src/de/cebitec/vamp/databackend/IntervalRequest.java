package de.cebitec.vamp.databackend;

import de.cebitec.vamp.util.Properties;

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
    private final ParametersReadClasses readClassParams;

    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param sender the sending object, that wants to receive the result of the
     * request
     * @param desiredData Can be any byte value representing a filter flag for
     * the results. E.g used for the desired coverage in a coverage request,
     * which is among: Properties.PERFECT_COVERAGE = 1, BEST_MATCH_COVERAGE = 2
     * or COMMON_COVERAGE = 3, depending on the coverage that is needed. Can
     * also be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or ParameterSetMapping.NORMAL, if this is a ordinary
     * track request.
     * @param readClassParams A parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads.
     */
    public IntervalRequest(int from, int to, ThreadListener sender, byte desiredData, ParametersReadClasses readClassParams) {
        this.from = from;
        this.to = to;
        this.sender = sender;
        this.desiredData = desiredData;
        this.readClassParams = readClassParams;
    }
    
    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param sender the sending object, that wants to receive the result of the
     * request
     * @param desiredData Can be any byte value representing a filter flag for
     * the results. E.g used for the desired coverage in a coverage request,
     * which is among: Properties.PERFECT_COVERAGE = 1, BEST_MATCH_COVERAGE = 2
     * or COMMON_COVERAGE = 3, depending on the coverage that is needed. Can
     * also be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or Properties.NORMAL, if this is a ordinary
     * track request.
     */
    public IntervalRequest(int from, int to, ThreadListener sender, byte desiredData) {
        this(from, to, sender, desiredData, new ParametersReadClasses());
    }
    
    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param sender the sending object, that wants to receive the result of the
     * request
     * @param readClassParams A parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads. 
     */
    public IntervalRequest(int from, int to, ThreadListener sender, ParametersReadClasses readClassParams) {
        this(from, to, sender, Properties.NORMAL, readClassParams);
    }
    
    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param sender the sending object, that wants to receive the result of the
     * request
     */
    public IntervalRequest(int from, int to, ThreadListener sender) {
        this(from, to, sender, Properties.NORMAL, new ParametersReadClasses());
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
     * @return Can be any byte value representing a filter flag for the results.
     * E.g used for the desired coverage in a coverage request, which is among:
     * Properties.PERFECT_COVERAGE = 1, BEST_MATCH_COVERAGE = 2 or
     * COMMON_COVERAGE = 3, depending on the coverage that is needed. Can also
     * be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or Properties.NORMAL, if this is a ordinary track
     * request.
     */
    public byte getDesiredData() {
        return this.desiredData;
    } 

    /**
     * @return A parameter set which contains all parameters concerning the
     * usage of VAMP's coverage classes and if only uniquely mapped reads shall
     * be used, or all reads.
     */
    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }
    
}
