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

package de.cebitec.readxplorer.utils;


import java.util.List;


/**
 * Contains all generally useful mathematical methods.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class MathUtils {


    private MathUtils() {
    }


    /**
     * Calculates the quantile border of the given quantile for a given list of
     * Doubles.
     *
     * @param quantile Quantile whose border value is needed
     * @param list List representing some empirical series of measurements
     * @return The quantile border value calculated from the series of
     * measurements.
     */
    public static double getQuantileBorder( double quantile, List<Double> list ) {
        double quantileIndex = quantile * list.size();
        boolean isWholeNumber = quantileIndex == Math.floor( quantileIndex );
        double quantileValue;
        if( isWholeNumber ) {
            int indexInt = (int) Math.floor( quantileIndex );
            quantileValue = 0.5 * (list.get( indexInt ) + list.get( indexInt + 1 ));
        } else {
            quantileValue = list.get( (int) Math.floor( quantileIndex ) );
        }
        return quantileValue;
    }


}
