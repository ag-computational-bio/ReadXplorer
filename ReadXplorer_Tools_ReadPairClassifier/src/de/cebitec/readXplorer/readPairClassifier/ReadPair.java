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

package de.cebitec.readXplorer.readPairClassifier;


import de.cebitec.readXplorer.util.ReadPairType;
import net.sf.samtools.SAMRecord;


/**
 * A read pair of a track containing two sam records. the id, the classification
 * and the distance of the pair.
 *
 * @author -Rolf Hilker-
 */
public class ReadPair {

    private final SAMRecord record1;
    private final SAMRecord record2;
    private final int readPairId;
    private ReadPairType type;
    private final int distance;


    /**
     * A read pair of a track.
     * <p>
     * @param record1    first sam record of the pair
     * @param record2    second sam record of the pair
     * @param readPairId the read pair id of this pair
     * @param type       the type of this pair (select among
     *                   ReadPairType.PERFECT_PAIR, ...)
     * @param distance   The distance of the two mappings of the pair
     */
    public ReadPair( SAMRecord record1, SAMRecord record2, int readPairId, ReadPairType type, int distance ) {
        this.record1 = record1;
        this.record2 = record2;
        this.readPairId = readPairId;
        this.type = type;
        this.distance = distance;
    }


    /**
     * @return the first sam record of the pair
     */
    public SAMRecord getRecord1() {
        return this.record1;
    }


    /**
     * @return the second sam record of the pair
     */
    public SAMRecord getRecord2() {
        return this.record2;
    }


    /**
     * @return the read pair id of this pair
     */
    public int getReadPairId() {
        return this.readPairId;
    }


    /**
     * @return the type of this pair (among ReadPairType.PERFECT_PAIR, ...)
     */
    public ReadPairType getType() {
        return this.type;
    }


    /**
     * @return The distance of the two mappings of the pair.
     */
    public int getDistance() {
        return distance;
    }


    /**
     * Update the read pair type, if it changes.
     * <p>
     * @param readPairType the new read pair type
     */
    public void setType( ReadPairType readPairType ) {
        this.type = readPairType;
    }


}
