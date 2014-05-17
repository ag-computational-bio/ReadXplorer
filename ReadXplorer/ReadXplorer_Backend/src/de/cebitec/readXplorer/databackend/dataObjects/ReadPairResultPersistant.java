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
