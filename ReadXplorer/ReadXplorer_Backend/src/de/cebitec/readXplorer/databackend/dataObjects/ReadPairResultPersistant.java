package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.databackend.IntervalRequest;
import java.util.Collection;

/**
 * Able to store the result for read pair calls. Called persistant, because it
 * needs the persistant data types from its own package.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ReadPairResultPersistant extends PersistantResult {
    
    private static final long serialVersionUID = 1L;
    
    private final Collection<PersistantReadPairGroup> readPairs;

    /**
     * Able to store the result for read pair calls. Called persistant, because it
     * needs the persistant data types from its own package.
     * @param readPairs colleaction of read pairs to store
     * @param request the request for which the result was generated 
     */
    public ReadPairResultPersistant(Collection<PersistantReadPairGroup> readPairs, IntervalRequest request) {
        super(request);
        this.readPairs = readPairs;
    }

    /**
     * @return the collection of read pairs
     */
    public Collection<PersistantReadPairGroup> getReadPairs() {
        return readPairs;
    }
    
}
