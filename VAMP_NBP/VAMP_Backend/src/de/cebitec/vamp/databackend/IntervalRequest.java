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
    private int totalFrom;
    private int totalTo;
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
     * @param totalFrom The total lower boundary of the request which is used 
     * for preloading larger data amounts for faster access.
     * @param totalTo The total upper boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     * @param sender the sending object, that wants to receive the result of the
     * request
     * @param desiredData Can be any byte value representing a filter flag for
     * the results. Can be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or ParameterSetMapping.NORMAL, if this is an 
     * ordinary track request.
     * @param readClassParams A parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads.
     */
    public IntervalRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender, 
            byte desiredData, ParametersReadClasses readClassParams) {
        this.from = from;
        this.to = to;
        this.totalFrom = totalFrom;
        this.totalTo = totalTo;
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
     * @param totalFrom The total lower boundary of the request which is used 
     * for preloading larger data amounts for faster access.
     * @param totalTo The total upper boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     * @param sender the sending object, that wants to receive the result of the
     * request
     * @param desiredData Can be any byte value representing a filter flag for
     * the results. Can be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or Properties.NORMAL, if this is an ordinary
     * track request.
     */
    public IntervalRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender, byte desiredData) {
        this(from, to, totalFrom, totalTo, sender, desiredData, new ParametersReadClasses());
    }
    
    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param totalFrom The total lower boundary of the request which is used 
     * for preloading larger data amounts for faster access.
     * @param totalTo The total upper boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     * @param sender the sending object, that wants to receive the result of the
     * request
     * @param readClassParams A parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads. 
     */
    public IntervalRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender, ParametersReadClasses readClassParams) {
        this(from, to, totalFrom, totalTo, sender, Properties.NORMAL, readClassParams);
    }
    
    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * @param from visible start position of the coverage request
     * @param to visible stop position of the coverage request
     * @param totalFrom The total lower boundary of the request which is used 
     * for preloading larger data amounts for faster access.
     * @param totalTo The total upper boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     * @param sender the sending object, that wants to receive the result of the
     * request
     */
    public IntervalRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender) {
        this(from, to, totalFrom, totalTo, sender, Properties.NORMAL, new ParametersReadClasses());
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
     * the results. Can be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or ParameterSetMapping.NORMAL, if this is a ordinary
     * track request.
     * @param readClassParams A parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads.
     */
    public IntervalRequest(int from, int to, ThreadListener sender, byte desiredData, ParametersReadClasses readClassParams) {
        this(from, to, from, to, sender, desiredData, readClassParams);
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
     * the results. Can be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or Properties.NORMAL, if this is an ordinary
     * track request.
     */
    public IntervalRequest(int from, int to, ThreadListener sender, byte desiredData) {
        this(from, to, from, to, sender, desiredData, new ParametersReadClasses());
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
        this(from, to, from, to, sender, Properties.NORMAL, readClassParams);
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
        this(from, to, from, to, sender, Properties.NORMAL, new ParametersReadClasses());
    }

    /**
     * @return The visible start position of the interval under investigation
     */
    public int getFrom() {
        return this.from;
    }

    /**
     * @return The visible end position of the interval under investigation
     */
    public int getTo() {
        return this.to;
    }

    /**
     * @return The total lower boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     */
    public int getTotalFrom() {
        return totalFrom;
    }

    /**
     * @return The total upper boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     */
    public int getTotalTo() {
        return totalTo;
    }

    /**
     * @return the sending object, that wants to receive the result of the request
     */
    public ThreadListener getSender() {
        return this.sender;
    }

    /**
     * @return Can be any byte value representing a filter flag for the results.
     * Can be a byte value representing one of the two flags
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
