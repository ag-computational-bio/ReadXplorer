package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 * Data structure for storing a feature (gene), which is detected as expressed
 * and its corresponding data.
 * 
 * @author -Rolf Hilker-
 */
public class FilteredFeature {

    private PersistantFeature filteredFeature;
    private int readCount;
    
    /**
     * Creates a new FilteredFeature.
     * @param filteredFeature the feature (gene) which is detected as expressed.
     */
    public FilteredFeature(PersistantFeature filteredFeature) {
        this.filteredFeature = filteredFeature;
    }

    /**
     * @return the feature (gene) which is detected as expressed.
     */
    public PersistantFeature getFilteredFeature() {
        return this.filteredFeature;
    }

    /**
     * @param readCount sets the number of reads detected for this expressed gene
     */
    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    /**
     * @return the number of reads detected for this expressed gene
     */
    public int getReadCount() {
        return this.readCount;
    }
}
