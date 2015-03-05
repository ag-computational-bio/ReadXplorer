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


import de.cebitec.readxplorer.utils.classification.FeatureType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Creates a parameters set which contains all parameters concerning the usage
 * of ReadXplorer's feature types.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParametersFeatureTypes {

    private final Set<FeatureType> selFeatureTypes;
    private int featureStartOffset;
    private int featureStopOffset;


    /**
     * Creates a parameters set which contains all parameters concerning the
     * usage of ReadXplorer's feature types.
     * <p>
     * @param selFeatureTypes    The set of selected feature types
     * @param featureStartOffset The start offset making genomic features start
     *                           further upstream
     * @param featureStopOffset  The stop offset making genomic features end
     *                           further downstream
     */
    public ParametersFeatureTypes( Set<FeatureType> selFeatureTypes, int featureStartOffset,
                                                                     int featureStopOffset ) {
        this.selFeatureTypes = new HashSet<>( selFeatureTypes );
        this.featureStartOffset = featureStartOffset;
        this.featureStopOffset = featureStopOffset;
    }


    /**
     * @return The start offset making genomic features start further upstream.
     */
    public int getFeatureStartOffset() {
        return featureStartOffset;
    }


    /**
     * @param featureStartOffset The start offset making genomic features start
     *                           further upstream.
     */
    public void setFeatureStartOffset( int featureStartOffset ) {
        this.featureStartOffset = featureStartOffset;
    }


    /**
     * @return The stop offset making genomic features end further downstream.
     */
    public int getFeatureStopOffset() {
        return featureStopOffset;
    }


    /**
     * @param featureStopOffset The stop offset making genomic features end
     *                          further downstream.
     */
    public void setFeatureStopOffset( int featureStopOffset ) {
        this.featureStopOffset = featureStopOffset;
    }


    /**
     * @return the set of selected feature types
     */
    public Set<FeatureType> getSelFeatureTypes() {
        return Collections.unmodifiableSet( selFeatureTypes );
    }


    /**
     * @param selFeatureTypes the set of selected feature types
     */
    public void setSelFeatureTypes( Set<FeatureType> selFeatureTypes ) {
        this.selFeatureTypes.clear();
        this.selFeatureTypes.addAll( selFeatureTypes );
    }


}
