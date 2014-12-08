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
package de.cebitec.readXplorer.parser.reference;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Contains common methods for reference parsers.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CommonsRefParser {

    private CommonsRefParser() {

    }


    /**
     * Generates a mapping of the "toString()" result of each item in the given
     * list
     * to the item itself.
     * <p>
     * @param mapToTransform the list of items to transform into a map with the
     *                       toString key and the item as value
     * <p>
     * @return the map of the toString result to the item
     */
    public static <E> Map<String, E> generateStringMap( Collection<E> mapToTransform ) {
        Map<String, E> stringMap = new HashMap<>();
        for( Iterator<E> itemIt = mapToTransform.iterator(); itemIt.hasNext(); ) {
            E item = itemIt.next();
            stringMap.put( item.toString(), item );
        }
        return stringMap;
    }


}
