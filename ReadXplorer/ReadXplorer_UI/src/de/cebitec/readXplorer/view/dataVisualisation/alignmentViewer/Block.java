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
package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import de.cebitec.readXplorer.databackend.dataObjects.Mapping;
import de.cebitec.readXplorer.databackend.dataObjects.ReferenceGap;
import de.cebitec.readXplorer.view.dataVisualisation.GenomeGapManager;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A block contains detailed information about one Mapping.
 * 
 * @author ddoppmei, Rolf Hilker
 */
public class Block implements BlockI {

    private int absStart;
    private int absStop;
    private Mapping mapping;
    private GenomeGapManager gapManager;
    private ArrayList<Brick> bricks;

    /**
     * A block contains detailed information about one Mapping.
     * @param absStart start of the block (might be larger than start of mapping)
     * @param absStop stop of the block (might be smaller than stop of mapping)
     * @param mapping mapping whose detailed information is needed
     * @param gapManager gap manager of the mapping
     */
    public Block(int absStart, int absStop, Mapping mapping, GenomeGapManager gapManager) {
        this.absStart = absStart;
        this.absStop = absStop;
        this.mapping = mapping;
        this.gapManager = gapManager;
        this.bricks = new ArrayList<>();
        this.createBricks();
    }

    /**
     * Each position in the block gets one brick.
     */
    private void createBricks() {
        for (int i = absStart; i <= absStop; i++) {
            if ((mapping.getStart() > i) || (i > mapping.getStop())) {
                bricks.add(Brick.TRIMMED);
            } else {
                if (gapManager.hasGapAt(i)) {
                    if (mapping.hasGenomeGapAtPosition(i)) {
                        this.fillWithOwnGenomeGaps(mapping, i);
                    } else {
                        this.fillWithForeignGaps(gapManager.getNumOfGapsAt(i));
                    }
                }
                this.addDiffOrMatchBrick(mapping, i);
            }
        }
    }

    private void fillWithOwnGenomeGaps(Mapping mapping, int position) {
        // do not only paint one gap, but ALL of them
        for (ReferenceGap gap : mapping.getGenomeGapsAtPosition(position)) {
            bricks.add(Brick.determineGapType(gap.getBase()));
        }
    }
    
    private void fillWithForeignGaps(int numberOfForeignGaps) {
        for (int x = 0; x < numberOfForeignGaps; x++) {
            bricks.add(Brick.FOREIGN_GENOMEGAP);
        }
    }

    private void addDiffOrMatchBrick(Mapping mapping, int position) {
        Brick type;
        if (mapping.hasDiffAtPosition(position)) {
            type = Brick.determineDiffType(mapping.getDiffAtPosition(position));
        } else {
            type = Brick.MATCH;
        }
        bricks.add(type);
    }

    @Override
    public int getAbsStart() {
        return absStart;
    }

    @Override
    public int getAbsStop() {
        return absStop;
    }

    @Override
    public Mapping getObjectWithId() {
        return mapping;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Brick b : bricks) {
            sb.append(b);
        }
        return sb.toString();
    }

    @Override
    public Iterator<Brick> getBrickIterator() {
        return bricks.iterator();
    }

    @Override
    public int getNumOfBricks() {
        return bricks.size();
    }

}
