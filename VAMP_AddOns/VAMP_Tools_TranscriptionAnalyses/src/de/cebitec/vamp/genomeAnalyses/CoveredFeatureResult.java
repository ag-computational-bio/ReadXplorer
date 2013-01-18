package de.cebitec.vamp.genomeAnalyses;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeatureResult {
    
    private List<CoveredFeature> results;
    private HashMap<Integer, String> trackList;
    private ParameterSetCoveredFeatures parameters;
    private int featureListSize;

    public CoveredFeatureResult(List<CoveredFeature> results, HashMap<Integer, String> trackList) {
        this.results = results;
        this.trackList = trackList;
        
    }

    public void setDetectionParameters(ParameterSetCoveredFeatures parameters) {
        this.parameters = parameters;
    }
    
    public void setFeatureListSize(int size) {
        this.featureListSize = size;
    }

    public List<CoveredFeature> getResults() {
        return results;
    }

    public HashMap<Integer, String> getTrackList() {
        return trackList;
    }

    public ParameterSetCoveredFeatures getParameters() {
        return parameters;
    }

    public int getFeatureListSize() {
        return featureListSize;
    }
    
}
