package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all data belonging to an operon detection result.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class OperonDetectionResult extends ResultTrackAnalysis<ParameterSetOperonDet> {
    
    private final List<Operon> detectedOperons;

    public OperonDetectionResult(HashMap<Integer, String> trackList, List<Operon> detectedOperons) {
        super(trackList);
        this.detectedOperons = detectedOperons;
    }

    public List<Operon> getResults() {
        return detectedOperons;
    }
}
