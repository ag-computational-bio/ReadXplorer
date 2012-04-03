package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;

/**
 * @author -Rolf Hilker-
 * 
 * Data structure for storing the annotations belonging to a certain position. E.g.
 * the annotations belonging to a gene start position. It can contain three different
 * types of annotations: A correctStartAnnotation, starting at the position belonging to
 * this element. An upstream- and a downstreamAnnotation.
 */
public class DetectedAnnotations {
    
    private PersistantAnnotation upstreamAnnotation;
    private PersistantAnnotation correctStartAnnotation;
    private PersistantAnnotation downstreamAnnotation;

    public PersistantAnnotation getDownstreamAnnotation() {
        return this.downstreamAnnotation;
    }

    public void setDownstreamAnnotation(PersistantAnnotation downstreamAnnotation) {
        this.downstreamAnnotation = downstreamAnnotation;
    }

    public PersistantAnnotation getCorrectStartAnnotation() {
        return this.correctStartAnnotation;
    }

    public void setCorrectStartAnnotation(PersistantAnnotation correctStartAnnotation) {
        this.correctStartAnnotation = correctStartAnnotation;
    }

    public PersistantAnnotation getUpstreamAnnotation() {
        return this.upstreamAnnotation;
    }

    public void setUpstreamAnnotation(PersistantAnnotation upstreamAnnotation) {
        this.upstreamAnnotation = upstreamAnnotation;
    }
    
    
    
    
}
