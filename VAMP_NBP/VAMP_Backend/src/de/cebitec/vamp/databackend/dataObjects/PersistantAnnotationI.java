package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 *
 * @author rhilker
 */
public interface PersistantAnnotationI {
        
    /**
     * @return start of the annotation. Always the smaller value among start and stop.
     */
    public int getStart();
    
    /**
     * @return stop of the annotation. Always the larger value among start and stop.
     */
    public int getStop();
    
    /**
     * @return type of the annotation among FeatureTypes.
     */
    public FeatureType getType();
    
}
