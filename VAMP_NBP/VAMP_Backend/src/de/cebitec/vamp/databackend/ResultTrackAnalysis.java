package de.cebitec.vamp.databackend;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data container for a result of an analysis for a list of tracks.
 * @param <T> class type of the parameter set
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public abstract class ResultTrackAnalysis<T>  implements ExcelExportDataI {
    
    private Map<Integer, PersistantTrack> trackMap;
    private List<String> trackNameList;
    private ParameterSetI<T> parameters;
    private Map<String, Integer> statsMap;

    /**
     * A result of an analysis for a list of tracks.
     * @param trackMap the list of tracks for which the analysis was carried out
     */
    public ResultTrackAnalysis(Map<Integer, PersistantTrack> trackMap) {//, PersistantTrack currentTrack) {
        this.trackMap = trackMap;
        this.trackNameList = PersistantTrack.generateTrackDescriptionList(trackMap.values());
        this.statsMap = new HashMap<>();
    }

    /**
     * @return the map of tracks for which the analysis was carried out hashed
     * to their respective track id
     */
    public Map<Integer, PersistantTrack> getTrackMap() {
        return trackMap;
    }

    /**
     * Sets the map of tracks for which the analysis was carried out hashed
     * to their respective track id
     * @param trackMap the map of tracks for which the analysis was carried out
     */
    public void setTrackList(Map<Integer, PersistantTrack> trackMap) {
        this.trackMap = trackMap;
        this.trackNameList = PersistantTrack.generateTrackDescriptionList(trackMap.values());
    }

    /**
     * @return the parameter set which was used for the analysis
     */
    public ParameterSetI<T> getParameters() {
        return this.parameters;
    }

    /**
     * Sets the parameter set which was used for the analysis
     * @param parameters the parameter set which was used for the analysis
     */
    public void setParameters(ParameterSetI<T> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the list of track names associated with the overall analysis, of
     * which this analysis result is one part
     */
    public List<String> getTrackNameList() {
        return this.trackNameList;
    }

    /**
     * @return The statistics map associated with this analysis
     */
    public Map<String, Integer> getStatsMap() {
        return this.statsMap;
    }

    /**
     * Sets the statistics map associated with this analysis.
     * @param statsMap the statistics map associated with this analysis
     */
    public void setStatsMap(Map<String, Integer> statsMap) {
        this.statsMap = statsMap;
    }
    
    /**
     * Creates a table row for statistic entries by the given identifier
     * @param identifier the identifier of the statistic value
     * @return the list containing the identifier and its statistic value
     */
    public List<Object> createStatisticTableRow(String identifier) {
        List<Object> row = new ArrayList<>();
        if (statsMap.containsKey(identifier)) {
            row.add(identifier);
            row.add(this.getStatsMap().get(identifier));
        }
        return row;
    }
    
    /**
     * Creates a table row (= object list) with two elemets.
     * @param fstEntry first entry of the table row
     * @param scndEntry second entry of the table row
     * @return the new table row (= object list)
     */
    public static List<Object> createTwoElementTableRow(Object fstEntry, Object scndEntry) {
        List<Object> row = new ArrayList<>();
        row.add(fstEntry);
        row.add(scndEntry);
        return row;
    }
    
    /**
     * Creates a table row (= object list) with only one element.
     * @param entry the entry of the table row
     * @return the new table row (= object list)
     */
    public static List<Object> createSingleElementTableRow(Object entry) {
        List<Object> row = new ArrayList<>();
        row.add(entry);
        return row;
    }
    
}
