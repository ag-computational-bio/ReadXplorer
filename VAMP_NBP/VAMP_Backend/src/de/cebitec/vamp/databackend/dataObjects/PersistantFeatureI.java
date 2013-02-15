package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 *
 * @author rhilker
 */
public interface PersistantFeatureI {
        
    /**
     * @return start of the feature. Always the smaller value among start and stop.
     */
    public int getStart();
    
    /**
     * @return stop of the feature. Always the larger value among start and stop.
     */
    public int getStop();
    
    /**
     * @return type of the feature among FeatureTypes.
     */
    public FeatureType getType();
    
}
