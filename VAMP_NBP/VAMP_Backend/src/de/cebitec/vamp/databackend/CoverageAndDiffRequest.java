
package de.cebitec.vamp.databackend;

/**
 * IntervalRequest implying that the coverage and diffs have to be obtained.
 * By using this class and checking the instance when carrying out the request,
 * it becomes clear if coverage and diffs are needed or not. 
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoverageAndDiffRequest extends IntervalRequest {
    
    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a
     * CoverageAndDiffRequest can be any request for any interval data. It is
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
     * double track request or ParameterSetMapping.NORMAL, if this is a ordinary
     * track request.
     * @param readClassParams A parameter set which contains all parameters
     * concerning the usage of VAMP's coverage classes and if only uniquely
     * mapped reads shall be used, or all reads.
     */
    public CoverageAndDiffRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender, byte desiredData, ParametersReadClasses readClassParams) {
        super(from, to, totalFrom, totalTo, sender, desiredData, readClassParams);
    }
    
    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a 
     * CoverageAndDiffRequest can be any request for any interval data. It is defined 
     * by at least three essential parameters: The left and right interval borders for the
     * interval under investigation and a ThreadListener, who wants to receive the results 
     * of this request.
     * @param from start position of the coverage request
     * @param to stop position of the coverage request
     * @param totalFrom The total lower boundary of the request which is used 
     * for preloading larger data amounts for faster access.
     * @param totalTo The total upper boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     * @param sender the sending object, that wants to receive the result of the request
     * @param desiredData Can be any byte value representing a filter flag for
     * the results. Can be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or ParameterSetMapping.NORMAL, if this is a ordinary
     * track request.
     */
    public CoverageAndDiffRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender, byte desiredData) {
        super(from, to, totalFrom, totalTo, sender, desiredData);
    }

    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a
     * CoverageAndDiffRequest can be any request for any interval data. It is
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
    public CoverageAndDiffRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender, ParametersReadClasses readClassParams) {
        super(from, to, totalFrom, totalTo, sender, readClassParams);
    }   
    
    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a 
     * CoverageAndDiffRequest can be any request for any interval data. It is defined 
     * by at least three essential parameters: The left and right interval borders for the
     * interval under investigation and a ThreadListener, who wants to receive the results 
     * of this request.
     * @param from start position of the interval
     * @param to end position of the interval
     * @param totalFrom The total lower boundary of the request which is used 
     * for preloading larger data amounts for faster access.
     * @param totalTo The total upper boundary of the request which is used for 
     * preloading larger data amounts for faster access.
     * @param sender the sending object, that wants to receive the result of the request
     */ 
    public CoverageAndDiffRequest(int from, int to, int totalFrom, int totalTo, ThreadListener sender) {
        super(from, to, totalFrom, totalTo, sender);
    } 
    
    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a
     * CoverageAndDiffRequest can be any request for any interval data. It is
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
    public CoverageAndDiffRequest(int from, int to, ThreadListener sender, byte desiredData, ParametersReadClasses readClassParams) {
        super(from, to, from, to, sender, desiredData, readClassParams);
    }

    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a
     * CoverageAndDiffRequest can be any request for any interval data. It is
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
     */
    public CoverageAndDiffRequest(int from, int to, ThreadListener sender, byte desiredData) {
        super(from, to, from, to, sender, desiredData);
    }

    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a
     * CoverageAndDiffRequest can be any request for any interval data. It is
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
    public CoverageAndDiffRequest(int from, int to, ThreadListener sender, ParametersReadClasses readClassParams) {
        super(from, to, from, to, sender, readClassParams);
    }

    /**
     * An instance of CoverageAndDiffRequest implies, that the diffs are needed.
     * Otherwise an ordinary IntervalRequest should be used. Besides that, a
     * CoverageAndDiffRequest can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * @param from start position of the interval
     * @param to end position of the interval
     * @param sender the sending object, that wants to receive the result of the
     * request
     */
    public CoverageAndDiffRequest(int from, int to, ThreadListener sender) {
        super(from, to, from, to, sender);
    }
    
}
