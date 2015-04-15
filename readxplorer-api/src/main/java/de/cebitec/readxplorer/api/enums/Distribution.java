/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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


/**
 * Global Distribution enumeration.
 * <p>
 * @author Oliver Schwengers
 * <oliver.schwengers@computational.bio.uni-giessen.de>, rhilker
 */
public enum Distribution {


    /**
     * Value for read length distribution = 3.
     */
    ReadLengthDistribution( 3, "Read Length Distribution" ),
    /**
     * Value for read pair size distribution = 4.
     */
    ReadPairSizeDistribution( 4, "Read Pair Size Distribution" ),
    /**
     * Value for a standard read start distribution and to request any of the
     * other read start distributions = 5.
     */
    ReadStartDistribution( 5, "Read Start Distribution" ),
    /**
     * Value for a standard coverage increase in percent distribution and to
     * request any of the other coverage increase in percent distributions = 6.
     */
    CoverageIncPercentDistribution( 6, "CoverageIncrease Percent Distribution" ),
    /**
     * Value for read starts on the feature strand distribution = 7.
     */
    ReadStartFeatStrandDistribution( 7, "Read Start Feature Strand Distribution" ),
    /**
     * Value for read starts on the opposite strand of features distribution =
     * 8.
     */
    ReadStartOppStrandDistribution( 8, "Read Start Opposite Strand Distribution" ),
    /**
     * Value for read starts combined for both strands in fwd direction
     * distribution = 9.
     */
    ReadStartBothFwdStrandDistribution( 9, "Read Start Combine Strands Fwd Distribution" ),
    /**
     * Value for read starts combined for both strands in rev direction
     * distribution = 10.
     */
    ReadStartBothRevStrandDistribution( 10, "Read Start Combine Strands Rev Distribution" ),
    /**
     * Value for coverage increase in percent on the feature strand distribution
     * = 11.
     */
    CovIncPercentFeatStrandDistribution( 11, "Coverage Increase Percent Feature Strand Distribution" ),
    /**
     * Value for coverage increase in percent on the opposite strand of features
     * distribution = 12.
     */
    CovIncPercentOppStrandDistribution( 12, "Coverage Increase Percent Opposite Strand Distribution" ),
    /**
     * Value for coverage increase in percent combined for both strands in fwd
     * direction distribution = 13.
     */
    CovIncPercentBothFwdStrandDistribution( 13, "Coverage Increase Percent Combine Strands Fwd Distribution" ),
    /**
     * Value for coverage increase in percent combined for both strands in rev
     * direction distribution = 14.
     */
    CovIncPercentBothRevStrandDistribution( 14, "Coverage Increase Percent Combine Strands Rev Distribution" );


    private final int typeInt;
    private final String typeString;


    private Distribution( int typeInt, String typeString ) {
        this.typeInt = typeInt;
        this.typeString = typeString;
    }


    public int getType() {
        return typeInt;
    }


    @Override
    public String toString() {
        return typeString;
    }


    public static Distribution fromType( final int type ) {

        for( Distribution dist : values() ) {
            if( dist.getType() == type ) {
                return dist;
            }
        }

        return null;

    }


    public static Distribution fromString( final String string ) {

        for( Distribution dist : values() ) {
            if( dist.toString().equalsIgnoreCase( string ) ) {
                return dist;
            }
        }

        return null;

    }


}
