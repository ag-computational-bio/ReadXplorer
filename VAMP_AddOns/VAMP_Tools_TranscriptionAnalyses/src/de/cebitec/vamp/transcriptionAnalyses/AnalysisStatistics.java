package de.cebitec.vamp.transcriptionAnalyses;

import java.util.HashMap;

/**
 * Container for statistics of any analysis consisting of an identifier and
 * an integer value.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisStatistics {

    private HashMap<String, Integer> statisticMap;
    
    /**
     * Container for statistics of any analysis consisting of an identifier and
     * an integer value.
     */
    public AnalysisStatistics() {
        statisticMap = new HashMap<>();
    }
    
    public void addStatisticValue(String identifier, int value) {
        statisticMap.put(identifier, value);
    }   
    
    
}
