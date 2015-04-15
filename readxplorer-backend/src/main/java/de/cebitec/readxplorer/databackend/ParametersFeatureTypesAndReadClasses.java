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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readxplorer.api.enums.FeatureType;
import java.util.Set;


/**
 * Creates a parameter set which contains all parameters concerning the usage
 * of ReadXplorer's feature types and read class parameters.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersFeatureTypesAndReadClasses extends ParametersFeatureTypes {

    private final ParametersReadClasses parametersReadClasses;


    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's feature types and read class parameters.
     * <p>
     * @param featureStartOffset    The start offset making genomic features
     *                              start further upstream
     * @param featureStopOffset     The stop offset making genomic features end
     *                              further downstream
     * @param selFeatureTypes       The set of selected feature types
     * @param parametersReadClasses The read classification parameters
     */
    public ParametersFeatureTypesAndReadClasses( int featureStartOffset, int featureStopOffset,
                                                                         Set<FeatureType> selFeatureTypes,
                                                                         ParametersReadClasses parametersReadClasses ) {
        super( selFeatureTypes, featureStartOffset, featureStopOffset );
        this.parametersReadClasses = parametersReadClasses;
    }

    
    /**
     * Creates a parameter set which contains all parameters concerning the
     * usage of ReadXplorer's feature types and read class parameters.
     * Convenience constructor for analyses not using the feature offsets -
     * setting the offset to 0.
     * <p>
     * @param selFeatureTypes       The set of selected feature types
     * @param parametersReadClasses The read classification parameters
     */
    public ParametersFeatureTypesAndReadClasses( Set<FeatureType> selFeatureTypes,
                                                 ParametersReadClasses parametersReadClasses ) {
        this( 0, 0, selFeatureTypes, parametersReadClasses );
    }


    /**
     * @return The read class parameters associated to this parameter set.
     */
    public ParametersReadClasses getReadClassParams() {
        return parametersReadClasses;
    }

//    public void setParametersReadClasses(ParametersReadClasses parametersReadClasses) {
//        this.parametersReadClasses = parametersReadClasses;
//    }

}
