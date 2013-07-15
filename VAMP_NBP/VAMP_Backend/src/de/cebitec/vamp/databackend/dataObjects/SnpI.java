package de.cebitec.vamp.databackend.dataObjects;

/**
 * @author rhilker
 * 
 * Interface to use for different SNP implementations.
 * Provides three essential methods.
 */
public interface SnpI {
    
    /**
     * @return The base of this snp which deviates from the reference sequence.
     */
    public String getBase();
    
    /**
     * @return The coverage of this snp which deviates from the reference sequence.
     */
    public int getCoverage();
    
    /**
     * @return The position of this snp.
     */
    public int getPosition();
    
}
