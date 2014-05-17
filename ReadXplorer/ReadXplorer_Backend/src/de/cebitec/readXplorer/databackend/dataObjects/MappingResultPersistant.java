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
