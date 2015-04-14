/*
 * Copyright (C) 2015 Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
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
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum Distribution {


    /**
     * Value for read start distribution = 5.
     */
    ReadStartDistribution( 5, "Read Start" ),

    /**
     * Value for coverage increase in percent distribution = 6.
     */
    CoverageIncPercentDistribution( 6, "CoverageIncPercent" ),

    /**
     * Value for read length distribution = 3.
     */
    ReadLengthDistribution( 3, "ReadLengthDistribution" ),

    /**
     * Value for seq pair size distribution = 4.
     */
    ReadPairSizeDistribution( 4, "ReadPairSize" );


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
