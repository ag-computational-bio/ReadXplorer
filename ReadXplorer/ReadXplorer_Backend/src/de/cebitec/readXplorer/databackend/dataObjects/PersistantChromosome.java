package de.cebitec.readXplorer.databackend.dataObjects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data holder for a chromosome.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class PersistantChromosome {

    private int id;
    private final int chromNumber;
    private int refGenID;
    private int chromLength;
    private final String name;

    /**
     * Data holder for a chromosome. 
     * @param id The id of the chromosome
     * @param chromNumber The chromosome number (1 until x) in this reference.
     * @param refGenID The id of the reference.
     * @param name the name of this chromosome
     * @param chromLength length of this chromosome
     */
    public PersistantChromosome(int id, int chromNumber, int refGenID, String name, int chromLength) {
        this.id = id;
        this.chromNumber = chromNumber;
        this.refGenID = refGenID;
        this.name = name;
        this.chromLength = chromLength;
    }

    /**
     * @return The database id of the reference.
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * @return The chromosome number (1 until x) in this reference.
     */
    public int getChromNumber() {
        return this.chromNumber;
    }
    
    /**
     * @return The id of the reference.
     */
    public int getRefGenID() {
        return this.refGenID;
    }
    
    /**
     * @return The name of this chromosome.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the length of the chromosome sequence
     */
    public int getLength() {
        return chromLength;
    }

    /**
     * @return The name of the chromosome.
     */
    @Override
    public String toString() {
        return this.name;
    }
    
    /**
     * Checks if the given chromosome is equal to this one.
     * @param object object to compare to this object
     */
    @Override
    public boolean equals(Object object) {

        if (object instanceof PersistantChromosome) {
            PersistantChromosome other = (PersistantChromosome) object;
            return     other.getName().equals(this.name)
                    && other.getId() == this.id
                    && other.getRefGenID() == this.refGenID
                    && other.getLength() == this.chromLength;
        } else {
            return super.equals(object);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.id;
        hash = 19 * hash + this.chromNumber;
        hash = 19 * hash + this.refGenID;
        hash = 19 * hash + Objects.hashCode(this.name);
        hash = 19 * hash + this.chromLength;
        return hash;
    }
    
    /**
     * Creates a mapping of the chromosome names to the chromosome.
     * @param chroms chromosome list to transform into the chromosome name map
     * @return The mapping of chromosome name to chromosome
     */
    public static Map<String, PersistantChromosome> getChromNameMap(Collection<PersistantChromosome> chroms) {
        Map<String, PersistantChromosome> chromMap = new HashMap<>();
        for (PersistantChromosome chrom : chroms) {
            chromMap.put(chrom.getName(), chrom);
        }
        return chromMap;
    }
}
