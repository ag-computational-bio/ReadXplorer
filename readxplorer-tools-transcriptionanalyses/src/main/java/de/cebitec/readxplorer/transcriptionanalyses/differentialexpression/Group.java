/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;


/**
 *
 * @author kstaderm
 */
public class Group {

    private final int[] integerRepresentation;
    private final int id;
    private final String stringRepresentation;

    private static int nextUnusedID = 0;


    public Group( final int[] integerRepresentation, final String stringRepresentation ) {
        id = nextUnusedID++;
        this.integerRepresentation = integerRepresentation;
        this.stringRepresentation = stringRepresentation;
    }


    public int getId() {
        return id;
    }


    public int getGnuRID() {
        return (id + 1);
    }


    public int[] getIntegerRepresentation() {
        return integerRepresentation;
    }


    public String getNormalizedStringRepresentation() {
        if (this.integerRepresentation.length == 0){
            return "";
        }
        StringBuilder sb = new StringBuilder( integerRepresentation.length );
        char smallestGroupCharacter = 'A';
        int smallestGroupNumber = integerRepresentation[0];
        for( int i = 0; i < integerRepresentation.length; i++ ) {
            if( smallestGroupNumber > integerRepresentation[i] ) {
                smallestGroupNumber = integerRepresentation[i];
            }
        }
        for( int i = 0; i < integerRepresentation.length; i++ ) {
            sb.append( (char)(smallestGroupCharacter + integerRepresentation[i] - smallestGroupNumber) );
            sb.append( ',' );
        }
        sb.deleteCharAt( sb.length()-1 );
        return sb.toString();
    }


    @Override
    public String toString() {
        return stringRepresentation;
    }


}
