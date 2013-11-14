package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.util.FeatureType;

/**
 * Interface for all persistant features.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
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
