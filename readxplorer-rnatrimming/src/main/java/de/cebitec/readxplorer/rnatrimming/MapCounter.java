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

package de.cebitec.readxplorer.rnatrimming;


import java.util.HashMap;


/**
 * A simple enchancement to the hashmap class to allow counting of values
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class MapCounter<T> extends HashMap<T, Integer> {

    private static final long serialVersionUID = 1L;


    public Integer incrementCount( T key ) {
        Integer counter = this.get( key );
        if( counter == null ) {
            counter = 0;
        }
        counter++;
        this.put( key, counter );
        return counter;
    }


}
