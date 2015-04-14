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
 * SAMRecord tag enumeration.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum SAMRecordTag {


    /**
     * 'Yc' = Tag for read classification in one of the three readxplorer
     * classes.
     */
    ReadClass( 1, "Yc" ),

    /**
     * 'Yt' = Tag for number of positions a sequence maps to in a reference.
     */
    MapCount( 2, "Yt"),

    /**
     * 'Yi' = Tag for the read pair id.
     */
    ReadPairId( 3, "Yi"),

    /**
     * 'Ys' = Tag for the read pair type.
     */
    ReadPairType( 4, "Ys");


    private final int typeInt;
    private final String typeString;


    private SAMRecordTag( int typeInt, String typeString ) {
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


    public static SAMRecordTag fromType( final int type ) {

        for( SAMRecordTag srTag : values() ) {
            if( srTag.getType() == type ) {
                return srTag;
            }
        }

        return null;

    }


    public static SAMRecordTag fromString( final String string ) {

        for( SAMRecordTag srTag : values() ) {
            if( srTag.toString().equalsIgnoreCase( string ) ) {
                return srTag;
            }
        }

        return null;

    }

}
