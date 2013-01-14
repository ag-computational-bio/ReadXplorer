package de.cebitec.vamp.genomeAnalyses;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredAnnotationResult {
    
    private List<CoveredAnnotation> results;
    private HashMap<Integer, String> trackList;
    private ParameterSetCoveredAnnos parameters;
    private int annotationListSize;

    public CoveredAnnotationResult(List<CoveredAnnotation> results, HashMap<Integer, String> trackList) {
        this.results = results;
        this.trackList = trackList;
        
    }

    public void setDetectionParameters(ParameterSetCoveredAnnos parameters) {
        this.parameters = parameters;
    }
    
    public void setAnnotationListSize(int size) {
        this.annotationListSize = size;
    }

    public List<CoveredAnnotation> getResults() {
        return results;
    }

    public HashMap<Integer, String> getTrackList() {
        return trackList;
    }

    public ParameterSetCoveredAnnos getParameters() {
        return parameters;
    }

    public int getAnnotationListSize() {
        return annotationListSize;
    }
    
}
