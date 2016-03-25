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

package de.cebitec.readxplorer.databackend.dataobjects;


import de.cebitec.readxplorer.databackend.IntervalRequest;
import java.util.Collections;
import java.util.List;


/**
 * Able to store the result for mapping calls. Called persistent,
 * because it needs the persistent data types from its own package.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class MappingResult extends AnalysisResult {

    private static final long serialVersionUID = 1L;

    private final List<Mapping> mappings;


    /**
     * Able to store the result for mapping calls. Called persistent, because it
     * needs the persistent data types from its own package.
     * <p>
     * @param mappings the list of mappings to store
     * @param request  the request for which the result was generated
     */
    public MappingResult( List<Mapping> mappings, IntervalRequest request ) {
        super( request );
        this.mappings = mappings;
    }


    /**
     * @return the mappings stored in this result
     */
    public List<Mapping> getMappings() {
        return Collections.unmodifiableList( mappings );
    }


}
