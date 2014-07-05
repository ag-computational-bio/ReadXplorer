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
