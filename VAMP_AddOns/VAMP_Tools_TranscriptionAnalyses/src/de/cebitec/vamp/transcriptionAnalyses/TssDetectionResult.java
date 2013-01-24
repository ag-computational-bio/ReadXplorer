
package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TranscriptionStart;
import java.util.HashMap;
import java.util.List;

/**
 * Container for all data belonging to a transcription start site detection
 * result.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TssDetectionResult extends ResultTrackAnalysis<ParameterSetTSS> {
    
    private List<TranscriptionStart> results;
    private List<String> promotorRegions;
    
    /**
     * Container for all data belonging to a transcription start site detection
     * result.
     * @param results the results of the TSS detection
     * @param trackList the list of tracks, for which the TSS detection was carried out
     */
    public TssDetectionResult(List<TranscriptionStart> results, HashMap<Integer, String> trackList) {
        super(trackList);
        this.results = results;
    }

    /**
     * @return The results of the TSS detection
     */
    public List<TranscriptionStart> getResults() {
        return results;
    }

    /**
     * @return Promotor regions of the detected TSS 
     */
    public List<String> getPromotorRegions() {
        return promotorRegions;
    }

    /**
     * Sets the promotor regions of the detected TSS 
     * @param promotorRegions Promotor regions of the detected TSS 
     */
    public void setPromotorRegions(List<String> promotorRegions) {
        this.promotorRegions = promotorRegions;
    }
    
}
