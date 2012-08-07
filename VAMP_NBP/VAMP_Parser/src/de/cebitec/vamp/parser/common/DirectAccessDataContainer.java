
package de.cebitec.vamp.parser.common;

import de.cebitec.vamp.util.Pair;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for all data collected by a direct access parser.
 *  
 * @author -Rolf Hilker-
 */
public class DirectAccessDataContainer {
    
    private CoverageContainer coverageContainer;
    private Map<String, Pair<Integer, Integer>> classificationMap;
    
    /**
     * Creates an empty container for all data collected by a direct access
     * parser. This means, it stores the coverage container, which stores the
     * position table information for direct access track and the classification
     * map, which stores the mapping of read name to the number of occurences of
     * the read and the lowest number of differences seen for this read.
     */
    public DirectAccessDataContainer() {
        this.coverageContainer = new CoverageContainer();
        this.classificationMap = new HashMap<String, Pair<Integer, Integer>>();
    }

    /**
     * Container for all data collected by a direct access parser.
     * @param coverageContainer the coverage container, which stores the
     * position table information for direct access track
     * @param classificationMap the classification map, which stores the mapping
     * of read name to the number of occurences of the read and the lowest
     * number of differences seen for this read.
     */
    public DirectAccessDataContainer(CoverageContainer coverageContainer, Map<String, Pair<Integer, Integer>> classificationMap) {
        this.coverageContainer = coverageContainer;
        this.classificationMap = classificationMap;
    }

    /**
     * @return the coverage container, which stores the position table 
     * information for direct access track.
     */
    public CoverageContainer getCoverageContainer() {
        return this.coverageContainer;
    }

    /**
     * 
     * @param coverageContainer the coverage container, which stores the 
     * position table information for direct access track.
     */
    public void setCoverageContainer(CoverageContainer coverageContainer) {
        this.coverageContainer = coverageContainer;
    }

    /**
     * @return the classification map, which stores the mapping of read name to 
     * the number of occurences of the read and the lowest number of differences
     * seen for this read.
     */
    public Map<String, Pair<Integer, Integer>> getClassificationMap() {
        return this.classificationMap;
    }

    /**
     * Sets the classification map, which stores the mapping of read name to the
     * number of occurences of the read and the lowest number of differences
     * seen for this read.
     * @param classificationMap 
     */
    public void setClassificationMap(Map<String, Pair<Integer, Integer>> classificationMap) {
        this.classificationMap = classificationMap;
    }
    
}
