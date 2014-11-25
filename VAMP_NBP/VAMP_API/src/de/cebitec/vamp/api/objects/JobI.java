package de.cebitec.vamp.api.objects;

/**
 * Interface for jobs having a known number of requests and also storing how 
 * many requests were already carried out.
 *
 * @author -Rolf Hilker-
 */
public interface JobI {
    
    /**
     * @return The number of requests already carried out.
     */
    public int getNbCarriedOutRequests();
    
    /**
     * @return The total number of requests within this job.
     */
    public int getNbTotalRequests();
    
}
