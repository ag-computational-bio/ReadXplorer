package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 * @author rhilker
 * 
 * A PersistantSubfeature is a subfeature of a PersistantFeature. 
 * Thus, it contains only a start and stop position.
 */
public class PersistantSubfeature implements PersistantFeatureI {

    private int parentId;
    private int start;
    private int stop;
    private FeatureType type;

    /**
     * Creates a new subfeature.
     * @param parentId the id of the parent feature
     * @param start absolute start of the subfeature in regard to the reference genome
     * @param stop absolute stop of the subfeature in regard to the reference genome
     */
    public PersistantSubfeature(int parentId, int start, int stop, FeatureType type) {
        this.parentId = parentId;
        this.start = start;
        this.stop = stop;
        this.type = type;
        
    }

    /**
     * @return the id of the parent feature
     */
    public int getParentId() {
        return this.parentId;
    }
    

    @Override
    public int getStart() {
        return this.start;
    }


    @Override
    public int getStop() {
        return this.stop;
    }

    
    @Override
    public FeatureType getType() {
        return type;
    }    
    
}
