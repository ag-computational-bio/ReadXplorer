package de.cebitec.vamp.databackend;

/**
 * @author -Rolf Hilker-
 * 
 * Interface to use in thread context. Provides methods for handling genome requests
 * in connection with a thread.
 */
public interface RequestThreadI {
    
    /**
     * Adds a request to the request queue.
     * @param request the request to add to the queue.
     */
    public void addRequest(GenomeRequest request);
    
}
