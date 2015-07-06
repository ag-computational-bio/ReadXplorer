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
import java.io.Serializable;


/**
 * Contains the basic functionality of an analysis result.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IntervalRequest request;


    /**
     * Common functionality of of an analysis result.
     * <p>
     * @param request the interval request for which the result was created
     */
    public AnalysisResult( IntervalRequest request ) {
        this.request = request;
    }


    /**
     * a parameterless constructor is needed to enable deserialization
     * of child classed
     */
    public AnalysisResult() {
        request = new IntervalRequest( -1, -1, -1, null, false );
    }


    /**
     * @return the interval request for which the result was created
     */
    public IntervalRequest getRequest() {
        return request;
    }


}
