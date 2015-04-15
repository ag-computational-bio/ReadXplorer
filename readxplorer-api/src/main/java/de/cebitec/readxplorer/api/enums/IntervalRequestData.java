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
 * Global enumeration for all necessary IntervalRequest data.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum IntervalRequestData {

    ReadPairs( 1, "ReadPairs" ),

    /**
     * Standard value, if all data is needed (0).
     */
    Normal( 0, "Normal"),

    /**
     * Value for all reduced mappings (8).
     */
    ReducedMappings( 8, "Reduced Mappings" ),

    /**
     * Value for obtaining read starts instead of coverage (9).
     */
    ReadStarts( 9, "Read Starts"),

    Track1( 4, "Track 1" ),

    Track2( 5, "Track 2" );


    private final int typeInt;
    private final String typeString;


    private IntervalRequestData( int typeInt, String typeString ) {
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


    public static IntervalRequestData fromType( final int type ) {

        for( IntervalRequestData irData : values() ) {
            if( irData.getType() == type ) {
                return irData;
            }
        }

        return null;

    }


    public static IntervalRequestData fromString( final String string ) {

        for( IntervalRequestData irData : values() ) {
            if( irData.toString().equalsIgnoreCase( string ) ) {
                return irData;
            }
        }

        return null;

    }

}
