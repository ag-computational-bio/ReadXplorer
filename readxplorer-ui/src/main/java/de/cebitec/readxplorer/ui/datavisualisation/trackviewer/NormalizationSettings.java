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

package de.cebitec.readxplorer.ui.datavisualisation.trackviewer;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author jstraube, rhilker
 */
public class NormalizationSettings {


    private final Map<Integer, Double[]> idToValue;


    public NormalizationSettings( List<Integer> trackIDs, List<Boolean> isLogNormList, List<Double> factors, List<Boolean> hasNormFactor ) {
        idToValue = new HashMap<>();
        int j = 0;
        for( int i : trackIDs ) {
            Double[] d = new Double[3];
            d[0] = isLogNormList.get( j ) ? 1.0 : 0.0;
            d[1] = factors.get( j );
            d[2] = hasNormFactor.get( j ) ? 1.0 : 0.0;
            idToValue.put( i, d );
            j++;
        }
    }


    public Map<Integer, Double[]> getIdToValue() {
        return Collections.unmodifiableMap( idToValue );
    }


    public void setIdToValue( Map<Integer, Double[]> idToValue ) {
        this.idToValue.clear();
        this.idToValue.putAll( idToValue );
    }


    public double getFactors( int trackID ) {
        return idToValue.get( trackID )[1];
    }


    public void setFactors( double factors, int trackID ) {
        Double[] d = idToValue.get( trackID );
        d[1] = factors;
        idToValue.put( trackID, d );
    }


    public Boolean getIsLogNorm( int trackID ) {
        return idToValue.get( trackID )[0] == 1.0;
    }


    public void setIsLogNorm( Boolean isLogNorm, int trackID ) {
        Double[] d = idToValue.get( trackID );
        d[0] = (isLogNorm ? 1.0 : 0.0);
        idToValue.put( trackID, d );
    }


    public Boolean getHasNormFac( int trackID ) {
        return idToValue.get( trackID )[2] == 1.0;
    }


    public void setHasNormFac( boolean hasNorm, int trackID ) {
        Double[] d = idToValue.get( trackID );
        d[2] = (hasNorm ? 1.0 : 0.0);
        idToValue.put( trackID, d );
    }


}
