package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.databackend.IntervalRequest;
import java.io.Serializable;

/**
 * Contains the basic functionality of a persistant result.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class PersistantResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private IntervalRequest request;

    /**
     * Common functionality of a persistant result.
     * @param request the interval request for which the result was created
     */
    public PersistantResult(IntervalRequest request) {
        this.request = request;
    }
    
    /** a parameterless constructor is needed to enable deserialization 
     *  of child classed */
    public PersistantResult() {
        this.request = new IntervalRequest(-1, -1, -1, null, false);
    }
    
    /**
     * @return the interval request for which the result was created
     */
    public IntervalRequest getRequest() {
        return this.request;
    }
    
}
