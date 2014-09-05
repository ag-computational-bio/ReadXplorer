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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileHeader;

/**
 * Class for the classification data of a read.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParsedClassification {
    
    private SAMFileHeader.SortOrder sortOrder;
    private int minMismatches;
    private List<Integer> readStarts;
    private Map<Integer, Integer> mismatchCountMap = new HashMap<>();
    //    private List<String> refNames; //TODO: use this when multiple import is enabled

    /**
     * Class for the classification data of a read.
     * @param sortOrder 
     */
    public ParsedClassification(SAMFileHeader.SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        this.minMismatches = Integer.MAX_VALUE;
        this.readStarts = new ArrayList<>();
    }

    /**
     * @return The smallest number of mismatches for the associated read in
     * the current data set.
     */
    public int getMinMismatches() {
        return minMismatches;
    }

    /**
     * Updates the smallest number of mismatches for this read with the given
     * value. The value is only set, if it is smaller than the value already
     * stored here.
     * @param mismatches The number of mismatches for the associated read 
     * at the current mapping position.
     */
    public void updateMinMismatches(int mismatches) {
        if (mismatches < this.minMismatches) {
            this.minMismatches = mismatches;
        }
    }

    /**
     * @return The array of read start positions for this read.
     */
    public List<Integer> getReadStarts() {
        return readStarts;
    }
    
    /**
     * Calculates the next mapping start for the given mapping start. If it is
     * the largest mapping start, then the smallest mapping start is returned to
     * be able to reach all mappings belonging to a read. If only one position
     * is stored in the array, 0 is returned.
     * @param start the start of the current read
     * @return The start position of the next mapping or the smallest mapping
     * position, if this is the largest mapping position in the array. If only
     * one position is stored in the array, 0 is returned.
     */
    public int getNextMappingStart(int start) {
        if (readStarts.size() <= 1) {
            return 0;
        } else if (this.sortOrder == SAMFileHeader.SortOrder.coordinate) {
            return this.getSortedMappingStart(start);
        } else {
            return this.calcNextMappingStart(start);
        }
    }

    /**
     * @param start The start to which the next start should be returned
     * @return The next larger start position of the same read or the smallest
     * mapping position, if this is the largest mapping position for the read
     */
    private int getSortedMappingStart(int start) {
        int index = this.readStarts.indexOf(start) + 1;
        if (index < this.readStarts.size()) {
            return this.readStarts.get(index);
        } else {
            return this.readStarts.get(0);
        }
    }

    /**
     * Calculates the next mapping start for the given mapping start. If it is
     * the largest mapping start, then the smallest mapping start is returned to
     * be able to reach all mappings belonging to a read. If no fitting position
     * is found, 0 is returned. 
     * @param start the start of the current read
     * @return The start position of the next mapping or the smallest mapping
     * position, if this is the largest mapping position in the array. If no 
     * fitting position is found, 0 is returned.
     */
    private int calcNextMappingStart(int start) {
        
        int nextStart = Integer.MAX_VALUE;
        int smallestStart = start;
        for (Integer mapStart : readStarts) {
            if (mapStart > start && mapStart < nextStart) {
                nextStart = mapStart;

            } else if (mapStart < smallestStart) {
                smallestStart = mapStart;
            }
        }
        nextStart = nextStart == Integer.MAX_VALUE && smallestStart < start ? smallestStart : nextStart;

        return nextStart == Integer.MAX_VALUE ? 0 : nextStart;
    }

    /**
     * Adds a mapping start position to the start positions of this read in the
     * current data set.
     * @param mappingStart The mapping start position of this read to add to
     * the list
     */
    public void addReadStart(int mappingStart) {
        this.readStarts.add(mappingStart);
    }
    
    /**
     * @return The number of occurrences of this read in the data set.
     */
    public int getNumberOccurrences() {
        return this.readStarts.size();
    }

    /**
     * @return The map of a number of mismatches to their count = how often
     * has this number of mismatches been observed in total.
     */
    public Map<Integer, Integer> getMismatchCountMap() {
        return mismatchCountMap;
    }
    
    /**
     * Increases the entry of the given noMismatches by one.
     * @param noMismatches The number of mismatches entry to increase by one
     */
    public void updateMismatchCountMap(int noMismatches) {
        if (!mismatchCountMap.containsKey(noMismatches)) {
            mismatchCountMap.put(noMismatches, 0);
        }
        mismatchCountMap.put(noMismatches, mismatchCountMap.get(noMismatches) + 1);
    }

    /**
     * Checks if the current common match mapping with the given number of 
     * differences is a single common match mapping (<code>true</code>), or has multiple
     * common match mappings (<code>false</code>).
     * @param differences Number of differences of a mapping already classified
     * as common match
     * @return <code>true</code>, if this is the only common match mapping of 
     * the read from which the number of differences originates, <code>false</code>, 
     * if there are multiple common match mappings for this read.
     */
    public boolean isSingleCommonMatch(int differences) {
        boolean isSingleCommonMatch = false;
        int count = 0;
        for (Map.Entry<Integer, Integer> mismatchCount : mismatchCountMap.entrySet()) {
            if (minMismatches < mismatchCount.getValue()) {
                if (isSingleCommonMatch = ++count > 1) { break; }
            }
        }
        return isSingleCommonMatch;
    }
    
}
