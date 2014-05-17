/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.parser.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for all data collected by a direct access parser.
 *  
 * @author -Rolf Hilker-
 */
public class DirectAccessDataContainer {
    
    private CoverageContainer coverageContainer;
    private Map<String, ParsedClassification> classificationMap;
    
    /**
     * Creates an empty container for all data collected by a direct access
     * parser. This means, it stores the coverage container, which stores the
     * position table information for direct access track and the classification
     * map, which stores the mapping of read name to the number of occurences of
     * the read and the lowest number of differences seen for this read.
     */
    public DirectAccessDataContainer() {
        this.coverageContainer = new CoverageContainer();
        this.classificationMap = new HashMap<>();
    }

    /**
     * Container for all data collected by a direct access parser.
     * @param coverageContainer the coverage container, which stores the
     * position table information for direct access track
     * @param classificationMap the classification map, which stores the mapping
     * of read name to the number of occurences of the read and the lowest
     * number of differences seen for this read.
     */
    public DirectAccessDataContainer(CoverageContainer coverageContainer, Map<String, ParsedClassification> classificationMap) {
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
     * @return The classification map, which stores the mapping of read name to 
     * the read classification data.
     */
    public Map<String, ParsedClassification> getClassificationMap() {
        return this.classificationMap;
    }

    /**
     * Sets the classification map, which stores the mapping of read name to the
     * read classification data
     * @param classificationMap the classification map to set 
     */
    public void setClassificationMap(Map<String, ParsedClassification> classificationMap) {
        this.classificationMap = classificationMap;
    }
    
}
