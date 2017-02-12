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

package de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer;


import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.ReferenceGap;
import de.cebitec.readxplorer.ui.datavisualisation.GenomeGapManager;
import de.cebitec.readxplorer.utils.SamAlignmentBlock;
import de.cebitec.readxplorer.utils.sequence.GenomicRange;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A block contains detailed information about one Mapping.
 * <p>
 * @author ddoppmei, Rolf Hilker
 */
public class Block implements BlockI, GenomicRange {

    private final int absStart;
    private final int absStop;
    private final Mapping mapping;
    private final GenomeGapManager gapManager;
    private final List<Brick> bricks;


    /**
     * A block contains detailed information about the visible part of one
     * Mapping.
     * <p>
     * @param absStart   start of the block = start of the visible part of the
     *                   mapping (might be larger than start of mapping)
     * @param absStop    stop of the block = stop of the visible part of the
     *                   mapping (might be smaller than stop of mapping)
     * @param mapping    mapping whose detailed information is needed
     * @param gapManager gap manager of the mapping
     */
    public Block( int absStart, int absStop, Mapping mapping, GenomeGapManager gapManager ) {
        this.absStart = absStart;
        this.absStop = absStop;
        this.mapping = mapping;
        this.gapManager = gapManager;
        bricks = new ArrayList<>();
        createBricks();
    }


    /**
     * Each position in the block (visible part of the mapping) gets one brick.
     */
    private void createBricks() {
        if( mapping.getAlignmentBlocks().isEmpty() ) {
            for( int i = absStart; i <= absStop; i++ ) {
                if( (mapping.getStart() > i) || (i > mapping.getStop()) ) {
                    bricks.add( Brick.TRIMMED );
                } else {
                    if( gapManager.hasGapAt( i ) ) {
                        if( mapping.hasGenomeGapAtPosition( i ) ) {
                            this.fillWithOwnGenomeGaps( mapping, i );
                        } else {
                            this.fillWithForeignGaps( gapManager.getNumOfGapsAt( i ) );
                        }
                    }
                    this.addDiffOrMatchBrick( mapping, i );
                }
            }
        } else {
            for( int i = 0; i < mapping.getAlignmentBlocks().size(); ++i ) {
                SamAlignmentBlock alignmentBlock = mapping.getAlignmentBlocks().get( i );
                int start = alignmentBlock.getRefStart() < absStart ? absStart : alignmentBlock.getRefStart();
                int stop = alignmentBlock.getRefStop() > absStop ? absStop : alignmentBlock.getRefStop();
                for( int j = start; j <= stop; j++ ) {
                    if( (mapping.getStart() > j) || (j > mapping.getStop()) ) {
                        bricks.add( Brick.TRIMMED );
                    } else {
                        if( gapManager.hasGapAt( j ) ) {
                            if( mapping.hasGenomeGapAtPosition( j ) ) {
                                this.fillWithOwnGenomeGaps( mapping, j );
                            } else {
                                this.fillWithForeignGaps( gapManager.getNumOfGapsAt( j ) );
                            }
                        }
                        this.addDiffOrMatchBrick( mapping, j );
                    }
                }
                if( i + 1 < mapping.getAlignmentBlocks().size() ) {
                    int nexStart = mapping.getAlignmentBlocks().get( i + 1 ).getRefStart();
                    start = alignmentBlock.getRefStop() < absStart ? absStart : alignmentBlock.getRefStop() + 1;
                    stop = nexStart > absStop ? absStop : nexStart;
                    for( int j = start; j < stop; ++j ) {
                        bricks.add( Brick.SKIPPED );
                    }
                }
            }
        }
    }


    private void fillWithOwnGenomeGaps( Mapping mapping, int position ) {
        // do not only paint one gap, but ALL of them
        for( ReferenceGap gap : mapping.getGenomeGapsAtPosition( position ) ) {
            bricks.add( Brick.determineGapType( gap.getBase() ) );
        }
    }


    private void fillWithForeignGaps( int numberOfForeignGaps ) {
        for( int x = 0; x < numberOfForeignGaps; x++ ) {
            bricks.add( Brick.FOREIGN_GENOMEGAP );
        }
    }


    private void addDiffOrMatchBrick( Mapping mapping, int position ) {
        Brick type;
        if( mapping.hasDiffAtPosition( position ) ) {
            type = Brick.determineDiffType( mapping.getDiffAtPosition( position ) );
        } else {
            type = Brick.MATCH;
        }
        bricks.add( type );
    }


    @Override
    public int compareTo( GenomicRange genomicRange ) {
        return GenomicRange.Utils.compareTo( this, genomicRange );
    }


    @Override
    public Mapping getObjectWithId() {
        return mapping;
    }


    @Override
    public int getStart() {
        return absStart;
    }


    @Override
    public int getStop() {
        return absStop;
    }


    @Override
    public boolean isFwdStrand() {
        return mapping.isFwdStrand();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for( Brick b : bricks ) {
            sb.append( b );
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
