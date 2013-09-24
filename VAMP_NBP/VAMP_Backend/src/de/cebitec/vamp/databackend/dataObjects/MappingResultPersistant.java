package de.cebitec.vamp.databackend.dataObjects;

import java.util.List;

/**
 * Able to store the result for mapping calls. Called persistant,
 * because it needs the persistant data types from its own package.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class MappingResultPersistant extends PersistantResult {
    
    private static final long serialVersionUID = 1L;
    
    private List<PersistantMapping> mappings;

    /**
     * Data storage for mappings.
     * @param mappings the list of mappings to store
     * @param lowerBound the lower bound of the requested interval
     * @param upperBound the upper bound of the requested interval
     */
    public MappingResultPersistant(List<PersistantMapping> mappings, int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        this.mappings = mappings;
    }

    /**
     * @return the mappings stored in this result
     */
    public List<PersistantMapping> getMappings() {
        return this.mappings;
    }
    
}
