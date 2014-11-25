package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.databackend.IntervalRequest;
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
     * Able to store the result for mapping calls. Called persistant, because it
     * needs the persistant data types from its own package.
     * @param mappings the list of mappings to store
     * @param request the request for which the result was generated 
     */
    public MappingResultPersistant(List<PersistantMapping> mappings, IntervalRequest request) {
        super(request);
        this.mappings = mappings;
    }

    /**
     * @return the mappings stored in this result
     */
    public List<PersistantMapping> getMappings() {
        return this.mappings;
    }
    
}
