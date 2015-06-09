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
 * Global enumeration for all strand related information.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum Strand {


    /**
     * Indicates that something is located on the forward strand (1).
     */
    Forward( 1, "Fwd" ),

    /**
     * Indicates that something is located on the reverse strand (-1).
     */
    Reverse( -1, "Rev"),

    /**
     * 1 = Feature/analysis strand option.
     */
    Feature( 2, "Feature"),

    /**
     * 2 = Opposite strand option.
     */
    Opposite( 3, "Opposite"),

    /**
     * Indicates that something uses both strands (0).
     */
    Both( 0, "Both" ),

    /**
     * Combine data of both strands option and treat them as if they were
     * originating from forward strand (3).
     */
    BothForward( 4, "Both Fwd" ),

    /**
     * Combine data of both strands option and treat them as if they were
     * originating from reverse strand (4).
     */
    BothReverse( 5, "Both Rev" );


    private final int typeInt;
    private final String typeString;


    private Strand( int typeInt, String typeString ) {
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


    public static Strand fromType( final int type ) {

        for( Strand strand : values() ) {
            if( strand.getType() == type ) {
                return strand;
            }
        }

        return null;

    }


    public static Strand fromString( final String string ) {

        for( Strand strand : values() ) {
            if( strand.toString().equalsIgnoreCase( string ) ) {
                return strand;
            }
        }

        return null;

    }

}
