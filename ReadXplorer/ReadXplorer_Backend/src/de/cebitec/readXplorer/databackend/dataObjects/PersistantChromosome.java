/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    private final int id;
    private final int chromNumber;
    private final int refGenID;
    private final int chromLength;
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
     * @return 
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
