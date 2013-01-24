package de.cebitec.vamp.databackend;

import java.util.HashMap;

/**
 * Data container for a result of an analysis for a list of tracks.
 * @param <T> class type of the parameter set
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ResultTrackAnalysis<T> {
    
    private HashMap<Integer, String> trackList;
    private ParameterSetI<T> parameters;

    /**
     * A result of an analysis for a list of tracks.
     * @param trackList the list of tracks for which the analysis was carried out
     */
    public ResultTrackAnalysis(HashMap<Integer, String> trackList) {
        this.trackList = trackList;
    }

    /**
     * @return the list of tracks for which the analysis was carried out
     */
    public HashMap<Integer, String> getTrackList() {
        return trackList;
    }

    /**
     * Sets the list of tracks for which the analysis was carried out.
     * @param trackList the list of tracks for which the analysis was carried out
     */
    public void setTrackList(HashMap<Integer, String> trackList) {
        this.trackList = trackList;
    }

    /**
     * @return the parameter set which was used for the analysis
     */
    public ParameterSetI<T> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameter set which was used for the analysis
     * @param parameters the parameter set which was used for the analysis
     */
    public void setParameters(ParameterSetI<T> parameters) {
        this.parameters = parameters;
    }
    
}
