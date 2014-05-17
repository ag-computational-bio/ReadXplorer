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
package de.cebitec.readXplorer.databackend;

import de.cebitec.readXplorer.util.FeatureType;
import java.util.Set;

/**
 * Creates a parameters set which contains all parameters concerning the usage
 * of ReadXplorer's feature types.
 * 
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersFeatureTypes {
    private Set<FeatureType> selFeatureTypes;

    /**
     * Creates a parameters set which contains all parameters concerning the
     * usage of ReadXplorer's feature types.
     * @param selFeatureTypes the set of selected feature types 
     */
    public ParametersFeatureTypes(Set<FeatureType> selFeatureTypes) {
        this.selFeatureTypes = selFeatureTypes;
    }

    /**
     * @return the set of selected feature types 
     */
    public Set<FeatureType> getSelFeatureTypes() {
        return selFeatureTypes;
    }

    /**
     * @param selFeatureTypes the set of selected feature types 
     */
    public void setSelFeatureTypes(Set<FeatureType> selFeatureTypes) {
        this.selFeatureTypes = selFeatureTypes;
    }
}
