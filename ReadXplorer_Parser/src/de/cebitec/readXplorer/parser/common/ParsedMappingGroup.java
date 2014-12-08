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

package de.cebitec.readXplorer.parser.common;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The dna sequence of all parsed mappings in this group is always identical,
 * but
 * positions and direction deviate.
 * <p>
 * @author ddoppmeier
 */
public class ParsedMappingGroup {

    private ArrayList<ParsedMapping> mappings;
    private int minError;
    private boolean bestMappingTagged;


    public ParsedMappingGroup() {
        mappings = new ArrayList<ParsedMapping>();
        minError = Integer.MAX_VALUE;
        bestMappingTagged = true;
    }


    public void addParsedMapping( ParsedMapping mapping ) {
        // if mapping already existed, increase the count of it
        if( mappings.contains( mapping ) ) {
            mappings.get( mappings.lastIndexOf( mapping ) ).increaseCounter();
        }
        else {
            // otherwise just add it
            mappings.add( mapping );
            bestMappingTagged = false;
            if( mapping.getErrors() < minError ) {
                minError = mapping.getErrors();
            }
        }
    }


    private void tagBestMatches() {
        Iterator<ParsedMapping> it = mappings.iterator();
        while( it.hasNext() ) {
            ParsedMapping m = it.next();
            if( m.getErrors() == minError ) {
                m.setIsBestMapping( true );
            }
            else {
                m.setIsBestMapping( false );
            }

        }
        bestMappingTagged = true;
    }


    public List<ParsedMapping> getMappings() {
        if( !bestMappingTagged ) {
            tagBestMatches();
        }
        return mappings;
    }


}
