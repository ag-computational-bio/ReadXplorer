package de.cebitec.readXplorer.parser.common;

import de.cebitec.readXplorer.parser.mappings.ParserCommonMethods;
import de.cebitec.readXplorer.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMTag;

/**
 * Class for the classification data of a read.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ParsedClassification {
    
    private SAMFileHeader.SortOrder sortOrder;
    private int minMismatches;
    private List<Integer> readStarts;
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
    
}
