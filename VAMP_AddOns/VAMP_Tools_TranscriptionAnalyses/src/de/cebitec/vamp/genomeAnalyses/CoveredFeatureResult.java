package de.cebitec.vamp.genomeAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all data belonging to a covered feature detection result.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoveredFeatureResult extends ResultTrackAnalysis<ParameterSetCoveredFeatures> {
    
    private List<CoveredFeature> results;
    private int featureListSize;

    /**
     * Container for all data belonging to a covered feature detection result.
     * @param results the results of the covered feature detection
     * @param trackList the list of tracks, for which the covered feature 
     *  detection was carried out
     */
    public CoveredFeatureResult(List<CoveredFeature> results, HashMap<Integer, String> trackList) {
        super(trackList);
        this.results = results;
        
    }
    
    public void setFeatureListSize(int size) {
        this.featureListSize = size;
    }
    
    public int getFeatureListSize() {
        return featureListSize;
    }

    public List<CoveredFeature> getResults() {
        return results;
    }
}
