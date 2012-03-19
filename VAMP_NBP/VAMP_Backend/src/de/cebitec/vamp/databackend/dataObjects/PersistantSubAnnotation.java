package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.api.objects.FeatureType;

/**
 * @author rhilker
 * 
 * A PersistantSubAnnotation is a sub annotation of a PersistantAnnotation. 
 * Thus, it contains only a start and stop position.
 */
public class PersistantSubAnnotation implements PersistantAnnotationI {

    private int parentId;
    private int start;
    private int stop;
    private FeatureType type;

    /**
     * Creates a new sub annotation.
     * @param parentId the id of the parent annotation
     * @param start absolute start of the sub annotation in regard to the reference genome
     * @param stop absolute stop of the sub annotation in regard to the reference genome
     */
    public PersistantSubAnnotation(int parentId, int start, int stop, FeatureType type) {
        this.parentId = parentId;
        this.start = start;
        this.stop = stop;
        this.type = type;
        
    }

    /**
     * @return the id of the parent annotation
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
