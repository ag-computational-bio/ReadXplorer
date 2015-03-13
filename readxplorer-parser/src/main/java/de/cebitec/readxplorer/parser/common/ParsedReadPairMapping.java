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

package de.cebitec.readxplorer.parser.common;


import de.cebitec.readxplorer.utils.ReadPairType;


/**
 * Container for a parsed sequence pair mapping. The seqPairID allows for
 * identifying all other mappings of this sequence pair.
 * <p>
 * @author Rolf Hilker
 */
public class ParsedReadPairMapping {

    private long id;
    private long seqPairID;
    private final long mappingId1;
    private final long mappingId2;
    private final ReadPairType type;
    private int nbReplicates;
    private final int distance;


    /**
     * A parsed sequence pair needs: Parsed mapping references have to be
     * passed, because the mapping id is not set yet. It will be set when
     * storing the data to the db.
     * <p>
     * @param seqPairID interim id of the sequence pair
     * @param type type of the sequence pair (0-5 = perfect, distance too small,
     * distance too large, orientation wrong orient wrong and dist too small,
     * orient wrong and dist too large)
     * @param mappingId1 id of fst mapping of the pair
     * @param mappingId2 id of scnd mapping of the pair
     * @param distance the sequence pair distance
     */
    public ParsedReadPairMapping( long mappingId1, long mappingId2, long seqPairID, ReadPairType type, int distance ) {
        this.mappingId1 = mappingId1;
        this.mappingId2 = mappingId2;
        this.seqPairID = seqPairID;
        this.type = type;
        this.distance = distance;
        this.nbReplicates = 1;
    }


    /**
     * @return Unique integer representing the id of this sequence pair mapping.
     */
    public long getId() {
        return this.id;
    }


    /**
     * @return id of this sequence pair. most important id. Searching among all
     * sequence pair mappings for this id will return all mappings of this
     * sequence pair along the genome. Each of them is stored in another
     * ParsedReadPairMapping.
     */
    public long getSequencePairID() {
        return this.seqPairID;
    }


    /**
     * @return Mapping id of the fst pair
     */
    public long getMappingId1() {
        return this.mappingId1;
    }


    /**
     * @return Mapping id of the second pair
     */
    public long getMappingId2() {
        return this.mappingId2;
    }


    /**
     * @return PERFECT_PAIR = 0, DISTANCE_TOO_LARGE_PAIR = 1,
     * DISTANCE_TOO_SMALL_PAIR = 2, ORIENTATION_WRONG_PAIR = 3,
     * ORIENTATION_AND_DIST_TOO_LARGE_PAIR = 4,
     * ORIENTATION_AND_DIST_TOO_SMALL_PAIR = 5, UNPAIRED_PAIR = 6
     */
    public ReadPairType getType() {
        return this.type;
    }


    /**
     * @param id unique integer representing the id of this sequence pair
     * mapping.
     */
    public void setID( long id ) {
        this.id = id;
    }


    /**
     * @param sequencePairID of this sequence pair. most important id. Searching
     * among all sequence pair mappings for this id will return all mappings of
     * this sequence pair along the genome. Each of them is stored in another
     * ParsedReadPairMapping.
     */
    public void setSequencePairID( long sequencePairID ) {
        this.seqPairID = sequencePairID;
    }


    /**
     * Increases the count of replicates by one.
     */
    public void addReplicate() {
        ++this.nbReplicates;
    }


    /**
     * @return The number of replicates of this pair.
     */
    public int getReplicates() {
        return this.nbReplicates;
    }

    /**
     * @return The read pair distance
     */
    public int getDistance() {
        return distance;
    }


    @Override
    public String toString() {
        return "Pair: " + id + ", PairID: " + seqPairID + ", MID1: " + mappingId1 + ", MID2: " + mappingId2 + ", Type: " + type + ", nbReplicates: " + nbReplicates;
    }


}
