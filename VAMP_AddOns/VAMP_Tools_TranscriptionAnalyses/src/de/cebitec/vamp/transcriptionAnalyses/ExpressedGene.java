package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 * @author -Rolf Hilker-
 * 
 * Data structure for storing a feature (gene), which is detected as expressed
 * and its corresponding data.
 */
public class ExpressedGene {

    private PersistantFeature expressedFeature;
    private int readCount;
    
    /**
     * Creates a new ExpressedGene.
     * @param expressedFeature the feature (gene) which is detected as expressed.
     */
    public ExpressedGene(PersistantFeature expressedFeature) {
        this.expressedFeature = expressedFeature;
    }

    /**
     * @return the feature (gene) which is detected as expressed.
     */
    public PersistantFeature getExpressedFeature() {
        return this.expressedFeature;
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
