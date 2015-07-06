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

package de.cebitec.readxplorer.ui.datavisualisation.readpairviewer;


import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.BlockI;
import de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer.LayerI;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Rolf Hilker
 * <p>
 * Contains a single layer of blocks belonging to one sequence pair.
 */
public class LayerPair implements LayerI {

    private final ArrayList<BlockI> blocks;


    /**
     * Contains a single layer of blocks belonging to one sequence pair.
     */
    public LayerPair() {
        blocks = new ArrayList<>();
    }


    @Override
    public void addBlock( BlockI block ) {
        blocks.add( block );
    }


    @Override
    public String toString() {

        //start und stop hier
        StringBuilder sb = new StringBuilder();
        for( BlockI b : blocks ) {
            sb.append( b.toString() );
        }

        return sb.toString();
    }


    @Override
    public Iterator<BlockI> getBlockIterator() {
        return blocks.iterator();
    }


}
