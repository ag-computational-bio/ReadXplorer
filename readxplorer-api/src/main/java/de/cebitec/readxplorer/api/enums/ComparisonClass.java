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

package de.cebitec.readxplorer.api.enums;

import de.cebitec.readxplorer.api.Classification;



/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public enum ComparisonClass implements Classification {

    /**
     * Classification for the coverage difference between two tracks.
     */
    DIFF_COVERAGE( 1, "Coverage Difference" ),

    /**
     * Classification for the coverage of track 1 of a track comparison.
     */
    TRACK1_COVERAGE( 2, "Coverage Track 1" ),

    /**
     * Classification for the coverage of track 2 of a track comparison.
     */
    TRACK2_COVERAGE( 3, "Coverage Track 2" );

    private final int type;
    private final String string;


    private ComparisonClass( int type, String string ) {
        this.type = type;
        this.string = string;
    }


    /**
     * @return the byte value of the type of the current comparison
     * classification.
     */
    @Override
    public int getType() {
        return type;
    }


    /**
     * @return the string representation of the current comparison
     * classification.
     */
    @Override
    public String toString() {
        return string;
    }


    /**
     * @return the desired ComparisonClass for a given integer of a valid
     * classification type.
     * <p>
     * @param type Type of ComparisonClass to return. If the type does not match
     * a classification type, null is returned.
     */
    public static ComparisonClass getFeatureType( int type ) {

        for( ComparisonClass cc : values() ) {
            if( cc.getType() == type ) {
                return cc;
            }
        }

        return null;

    }


}
