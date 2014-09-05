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
package de.cebitec.readXplorer.view.dataVisualisation.readPairViewer;

import de.cebitec.readXplorer.databackend.dataObjects.ObjectWithId;
import de.cebitec.readXplorer.databackend.dataObjects.ReadPairGroup;
import de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer.BlockI;
import de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer.Brick;
import java.util.Iterator;

/**
 * A BlockPair is a block that contains detailed information about 
 * one read pair id = all corresponding mappings in the currently visible interval.
 * 
 * @author rhilker
 */
public class BlockPair implements BlockI {

    private int absStart;
    private int absStop;
    private ReadPairGroup readPairGroup;

    /**
     * A block is a block that contains detailed information about one read pair id = all corresponding mappings.
     * @param absStart start of the block as sequence position (might be larger than start of mapping, when not in visible interval)
     * @param absStop stop of the block as sequence position (might be smaller than stop of mapping, when not in visible interval)
     * @param readPairGroup read pair group of this block
     */
    public BlockPair(int absStart, int absStop, ReadPairGroup readPairGroup){
        this.absStart = absStart;
        this.absStop = absStop;
        this.readPairGroup = readPairGroup;
    }


    @Override
    public int getAbsStart() {
        return this.absStart;
    }

    @Override
    public int getAbsStop() {
        return this.absStop;
    }
    
    
    public long getSeqPairId(){
        return this.readPairGroup.getId();
    }

    
    @Override
    public String toString() {
        //TODO: implement to string for BlockPair
        return "";
    }

   /**
     * @return null, because it is not supported for BlockPairs!
     */
    @Override
    public Iterator<Brick> getBrickIterator() {
        return null;
    }

    /**
     * @return -1, because it is not supported for BlockPairs!
     */
    @Override
    public int getNumOfBricks() {
        return -1;
    }

    /**
     * @return The associated seq pair group.
     */
    @Override
    public ObjectWithId getObjectWithId() {
        return this.readPairGroup;
    }
}
