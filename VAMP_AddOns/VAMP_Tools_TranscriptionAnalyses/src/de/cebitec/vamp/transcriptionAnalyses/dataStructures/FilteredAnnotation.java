package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;

/**
 * Data structure for storing a annotation (gene), which is detected as expressed
 * and its corresponding data.
 * 
 * @author -Rolf Hilker-
 */
public class FilteredAnnotation {

    private PersistantAnnotation expressedAnnotation;
    private int readCount;
    
    /**
     * Creates a new FilteredAnnotation.
     * @param expressedAnnotation the annotation (gene) which is detected as expressed.
     */
    public FilteredAnnotation(PersistantAnnotation expressedAnnotation) {
        this.expressedAnnotation = expressedAnnotation;
    }

    /**
     * @return the annotation (gene) which is detected as expressed.
     */
    public PersistantAnnotation getFilteredAnnotation() {
        return this.expressedAnnotation;
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
