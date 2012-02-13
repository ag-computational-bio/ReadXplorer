package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 * @author -Rolf Hilker-
 * 
 * Data structure for storing the features belonging to a certain position. E.g.
 * the features belonging to a gene start position. It can contain three different
 * types of features: A correctStartFeature, starting at the position belonging to
 * this element. An upstream- and a downstreamFeature.
 */
public class DetectedFeatures {
    
    private PersistantFeature upstreamFeature;
    private PersistantFeature correctStartFeature;
    private PersistantFeature downstreamFeature;

    public PersistantFeature getDownstreamFeature() {
        return this.downstreamFeature;
    }

    public void setDownstreamFeature(PersistantFeature downstreamFeature) {
        this.downstreamFeature = downstreamFeature;
    }

    public PersistantFeature getCorrectStartFeature() {
        return this.correctStartFeature;
    }

    public void setCorrectStartFeature(PersistantFeature correctStartFeature) {
        this.correctStartFeature = correctStartFeature;
    }

    public PersistantFeature getUpstreamFeature() {
        return this.upstreamFeature;
    }

    public void setupstreamFeature(PersistantFeature upstreamFeature) {
        this.upstreamFeature = upstreamFeature;
    }
    
    
    
    
}
