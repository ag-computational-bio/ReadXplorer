/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

import de.cebitec.readXplorer.util.FeatureType;

/**
 * A PersistantSubFeature is a sub feature of a PersistantFeature. 
 * Thus, it contains only a start and stop position, its parent and the type.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class PersistantSubFeature implements PersistantFeatureI {

    private int parentId;
    private int start;
    private int stop;
    private FeatureType type;

    /**
     * Creates a new sub feature. A PersistantSubFeature is a sub feature of a 
     * PersistantFeature. Thus, it contains only a start and stop position, 
     * its parent and the type.
     * @param parentId the id of the parent feature
     * @param start absolute start of the sub feature in regard to the reference genome
     * @param stop absolute stop of the sub feature in regard to the reference genome
     * @param type the {@link FeatureType} of the subfeature 
     */
    public PersistantSubFeature(int parentId, int start, int stop, FeatureType type) {
        this.parentId = parentId;
        this.start = start;
        this.stop = stop;
        this.type = type;
        
    }

    /**
     * @return the id of the parent feature
     */
    public int getParentId() {
        return this.parentId;
    }
    

    @Override
    public int getStart() {
        return this.start;
    }


    @Override
    public int getStop() {
        return this.stop;
    }

    /**
     * @return the {@link FeatureType} of the subfeature 
     */
    @Override
    public FeatureType getType() {
        return type;
    }    
    
}
