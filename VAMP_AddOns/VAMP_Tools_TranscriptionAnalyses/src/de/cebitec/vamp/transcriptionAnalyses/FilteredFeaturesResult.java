package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.FilteredFeature;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all data belonging to a filtered features result.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilteredFeaturesResult extends ResultTrackAnalysis<ParameterSetFilteredFeatures> {
    
    private final List<FilteredFeature> filteredFeatures;

    public FilteredFeaturesResult(HashMap<Integer, String> trackList, List<FilteredFeature> filteredFeatures) {
        super(trackList);
        this.filteredFeatures = filteredFeatures;
    }

    public List<FilteredFeature> getResults() {
        return filteredFeatures;
    }
    
    
}
