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

package de.cebitec.readxplorer.databackend.dataobjects;


import de.cebitec.readxplorer.api.constants.Colors;
import de.cebitec.readxplorer.api.enums.ReadPairType;
import de.cebitec.readxplorer.utils.SamAlignmentBlock;
import java.awt.Color;


/**
 * Creates a new persistent read pair. If both mappings of the pair are visible
 * the second mapping has to be added separately. TODO persistent objects
 * vereinheitlichen, wo möglich
 * <p>
 * @author Rolf Hilker
 */
public class ReadPair implements ObjectWithId {

    private long readPairID;
    private long mapping2Id;
    private ReadPairType readPairType;
    private int readPairReplicates;
    private Mapping visibleMapping;
    private Mapping visibleMapping2;


    /**
     * Creates a new persistent read pair. If both mappings of the pair are
     * visible the second mapping has to be added separately.
     * <p>
     * @param readPairID         id of the pair, will identify all mappings
     *                           belonging to this pair id
     * @param mapping1ID         id of mapping 1 of this pair
     * @param mapping2ID         id of mapping 2 of this pair
     * @param readPairType       type of the read pair (@see ReadPairClassifier
     *                           constants with values: 0-6)
     * @param readPairReplicates number of replicates of this pair
     * @param visibleMapping     currently visible mapping of the pair
     */
    public ReadPair( long readPairID, long mapping1ID, long mapping2ID, ReadPairType readPairType,
                     int readPairReplicates, Mapping visibleMapping ) {
        this.readPairID = readPairID;
        this.mapping2Id = mapping1ID == visibleMapping.getId() ? mapping2ID : mapping1ID;
        this.readPairType = readPairType;
        this.readPairReplicates = readPairReplicates == 0 ? 1 : readPairReplicates;
        this.visibleMapping = visibleMapping;
    }


    /**
     * Creates a new persistent read pair. If both mappings of the pair are
     * visible the second mapping has to be added separately.
     * <p>
     * @param readPairID         id of the pair, will identify all mappings
     *                           belonging to this pair id
     * @param mapping1ID         id of mapping 1 of this pair
     * @param mapping2ID         id of mapping 2 of this pair
     * @param readPairType       type of the read pair (
     * <p>
     * @see ReadPairClassifier constants with values: 0-6)
     * @param readPairReplicates number of replicates of this pair
     * @param visibleMapping     currently visible mapping of the pair
     * @param mate               the mate of the visibleMapping = other read of
     *                           the pair
     */
    public ReadPair( long readPairID, long mapping1ID, long mapping2ID, ReadPairType readPairType,
                     int readPairReplicates, Mapping visibleMapping, Mapping mate ) {
        this.readPairID = readPairID;
        this.mapping2Id = mapping1ID == visibleMapping.getId() ? mapping2ID : mapping1ID;
        this.readPairType = readPairType;
        this.readPairReplicates = readPairReplicates == 0 ? 1 : readPairReplicates;
        this.visibleMapping = visibleMapping;
        this.visibleMapping2 = mate;
    }


    public long getMapping2Id() {
        return mapping2Id;
    }


    public void setMapping2Id( long mapping2Id ) {
        this.mapping2Id = mapping2Id;
    }


    @Override
    public long getId() {
        return readPairID;
    }


    public void setReadPairID( long readPairID ) {
        this.readPairID = readPairID;
    }


    public int getReadPairReplicates() {
        return readPairReplicates;
    }


    public void setReadPairReplicates( int readPairReplicates ) {
        this.readPairReplicates = readPairReplicates;
    }


    public ReadPairType getReadPairType() {
        return readPairType;
    }


    public void setReadPairType( ReadPairType readPairType ) {
        this.readPairType = readPairType;
    }


    public Mapping getVisibleMapping() {
        return visibleMapping;
    }


    public void setVisibleMapping( Mapping visibleMapping ) {
        this.visibleMapping = visibleMapping;
    }


    /**
     * @return If both mappings of the pair are visible, it returns the second
     *         mapping otherwise it returns null
     */
    public Mapping getVisibleMapping2() {
        return visibleMapping2;
    }


    /**
     * If both mappings of the pair are visible, set the second mapping by using
     * this method
     * <p>
     * @param visiblemapping2 the second mapping of the pair
     */
    public void setVisiblemapping2( Mapping visiblemapping2 ) {
        this.visibleMapping2 = visiblemapping2;
    }


    /**
     * @return start position of the visible mapping or, if both mappings of the
     *         pair are visible, returns the smaller start position among both
     */
    public long getStart() {
        if( visibleMapping2 == null ) {
            return visibleMapping.getStart();
        } else {
            long start1 = visibleMapping.getStart();
            long start2 = visibleMapping2.getStart();
            return start1 < start2 ? start1 : start2;
        }
    }


    /**
     * @return stop position of the visible mapping or, if both mappings of the
     *         pair are visible, returns the larger stop position among both
     */
    public long getStop() {
        if( visibleMapping2 == null ) {
            return this.visibleMapping.getStop();
        } else {
            long stop1 = visibleMapping.getStop();
            long stop2 = visibleMapping2.getStop();
            return stop1 > stop2 ? stop1 : stop2;
        }
    }


    /**
     * Caclulate the read pair fragment length for all alignment blocks.
     *
     * @return The actual length of the fragment
     */
    public int getFragmentLength() {
        int length;
        if( visibleMapping2 == null || visibleMapping2.getStop() == -1 ) {
            length = visibleMapping.getAlignmentBlockLength(); //TODO: actual length of fragment is missing read 2 here
        } else {
            int start = visibleMapping.getStart();
            int start2 = visibleMapping2.getStart();
            int stop = visibleMapping.getStop();
            int stop2 = visibleMapping2.getStop();

            boolean useFst = start < start2;
            boolean overlapping = stop > start2 && start < start2 || stop2 > start && start2 < start;

            if( !overlapping ) {
                length = visibleMapping.getAlignmentBlockLength() + visibleMapping2.getAlignmentBlockLength();

            } else if( useFst ) {
                length = sumAlignmentBlocks( visibleMapping, visibleMapping2 );
            } else {
                length = sumAlignmentBlocks( visibleMapping2, visibleMapping );
            }
        }
        return length;
    }


    /**
     * Sum all alignment blocks to calculate the fragment length.
     *
     * @param mapping  First mapping with regards to reference coordinates
     * @param mapping2 Second mapping with regards to reference coordinates
     *
     * @return The fragment length in bp
     */
    private int sumAlignmentBlocks( Mapping mapping, Mapping mapping2 ) {
        int length = 0;
        if( mapping.getAlignmentBlocks().isEmpty() && mapping2.getAlignmentBlocks().isEmpty() ) {
            length = mapping2.getStart() - mapping.getStart() + mapping2.getLength();
        
        } else if (mapping.getAlignmentBlocks().isEmpty()) {
            length = mapping2.getStart() - mapping.getStart() + mapping2.getAlignmentBlockLength();
        
        } else if (mapping2.getAlignmentBlocks().isEmpty()) {
            length = mapping.getAlignmentBlockLength() + mapping2.getStop() - mapping.getStop();
        
        } else {
            for( SamAlignmentBlock block : mapping.getAlignmentBlocks() ) {
                if( block.getRefStop() < mapping2.getStart() ) {
                    length += block.getLength();
                } else {
                    length += mapping2.getAlignmentBlocks().get( 0 ).getRefStart() - block.getRefStart();
                    for( SamAlignmentBlock block2 : mapping2.getAlignmentBlocks() ) {
                        length += block2.getLength();
                    }
                    break;
                }
            }
        }
        return length;
    }


    /**
     * @return true, if this read pair already has a second visible mapping,
     *         false otherwise
     */
    public boolean hasVisibleMapping2() {
        return this.visibleMapping2 != null;
    }


    /**
     * Determines the color according to the type of a read pair.
     * <p>
     * @param type the type of the read pair
     * <p>
     * @return the color representing this read pair
     */
    public static Color determineReadPairColor( ReadPairType type ) {
        Color blockColor;
        if( type == ReadPairType.PERFECT_PAIR || type == ReadPairType.PERFECT_UNQ_PAIR ) {
            blockColor = Colors.BLOCK_PERFECT;
        } else if( type == ReadPairType.UNPAIRED_PAIR ) {
            blockColor = Colors.BLOCK_UNPAIRED;
        } else {
            blockColor = Colors.BLOCK_DIST_SMALL;
        }
        return blockColor;
    }


}
