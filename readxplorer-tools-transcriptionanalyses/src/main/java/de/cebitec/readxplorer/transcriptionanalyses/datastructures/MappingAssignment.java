/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.datastructures;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.Map;



/**
 * An interface for classes assigning features to a mapping according to a
 * certain assignment model.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface MappingAssignment {


    /**
     * Computes if the feature can be assigned to the mapping or not.
     * @param featStart The start of the feature, always smaller than featStop
     * @param featStop The stop of the feature, always larger than featStart
     * @param feature The feature to check
     * @return <code>true</code> if the mapping should be counted for the
     *         current feature, <code>false</code> otherwise.
     */
    public boolean checkAssignment( int featStart, int featStop, PersistentFeature feature );

    
    /**
     * Allows calculating fractions of the read count of the mapping for all
     * features associated to this mapping according to the implemented model.
     * @param featureReadCount The map containing the read counts for each
     * feature (accessible via it's id)
     */
    public void fractionAssignmentCheck( Map<Integer, NormalizedReadCount> featureReadCount );

}
