
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
    public CoverageAndDiffRequest(int from, int to, ThreadListener sender, byte desiredData, ParametersReadClasses readClassParams) {
        super(from, to, sender, desiredData, readClassParams);
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
     * @param sender the sending object, that wants to receive the result of the request
     * @param desiredData Can be any byte value representing a filter flag for
     * the results. E.g used for the desired coverage in a coverage request,
     * which is among: Properties.PERFECT_COVERAGE = 1, BEST_MATCH_COVERAGE = 2
     * or COMMON_COVERAGE = 3, depending on the coverage that is needed. Can
     * also be a byte value representing one of the two flags
     * PersistantCoverage.TRACK1 or PersistantCoverage.TRACK2 if this is a
     * double track request or ParameterSetMapping.NORMAL, if this is a ordinary
     * track request.
     */
    public CoverageAndDiffRequest(int from, int to, ThreadListener sender, byte desiredData) {
        super(from, to, sender, desiredData);
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
        super(from, to, sender, readClassParams);
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
     * @param sender the sending object, that wants to receive the result of the request
     */ 
    public CoverageAndDiffRequest(int from, int to, ThreadListener sender) {
        super(from, to, sender);
    }    
    
}
