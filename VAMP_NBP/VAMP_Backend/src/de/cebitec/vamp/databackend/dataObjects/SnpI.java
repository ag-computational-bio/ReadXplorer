package de.cebitec.vamp.databackend.dataObjects;

/**
 * Interface to use for different SNP implementations. Provides three essential
 * methods.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface SnpI extends Comparable<SnpI> {
    
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
