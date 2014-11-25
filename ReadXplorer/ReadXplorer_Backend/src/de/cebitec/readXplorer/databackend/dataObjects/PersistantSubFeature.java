package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.util.FeatureType;

/**
 * A PersistantSubFeature is a sub feature of a PersistantFeature. 
 * Thus, it contains only a start and stop position, its parent and the type.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class PersistantSubFeature implements PersistantFeatureI {

    private int parentId;
    private int start;
    private int stop;
    private FeatureType type;

    /**
     * Creates a new sub feature. A PersistantSubFeature is a sub feature of a 
     * PersistantFeature. Thus, it contains only a start and stop position, 
     * its parent and the type.
     * @param parentId the id of the parent feature
     * @param start absolute start of the sub feature in regard to the reference genome
     * @param stop absolute stop of the sub feature in regard to the reference genome
     * @param type the {@link FeatureType} of the subfeature 
     */
    public PersistantSubFeature(int parentId, int start, int stop, FeatureType type) {
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

    /**
     * @return the {@link FeatureType} of the subfeature 
     */
    @Override
    public FeatureType getType() {
        return type;
    }    
    
}
