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

import java.util.List;

/**
 * Container for a parsed mapping. It contains all data a mapping should have.
 * ID, start, stop (start is always the smaller value), direction (1 for fwd and -1 for rev), 
 * errors, diffs, gaps, bestmapping and number of replicates. Also the read sequence can be stored here, 
 * but should be removed when not needed anymore.
 *
 * @author ddoppmeier, rhilker
 */
public class ParsedMapping {

    private long id;
    private int start;
    private int stop;
    private byte direction;
    private int errors;
    private List<ParsedDiff> diffs;
    private List<ParsedReferenceGap> gaps;
    private boolean bestMapping;
    private int numOfReplicates;

    /**
     * Standard constructor for a parsed mapping.
     * @param start start of the mapping
     * @param stop end of the mapping
     * @param direction direction of the mapping: 1 for fwd and -1 for rev
     * @param diffs the list of diffs between the reference and the mapping
     * @param gaps list of gaps between the reference and the mapping
     * @param errors number of errors
     */
    public ParsedMapping(int start, int stop, byte direction, List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps, int errors){
        this.start = start;
        this.stop = stop;
        this.direction = direction;
        this.diffs = diffs;
        this.gaps = gaps;
        this.errors = errors;
        this.bestMapping = false;
        this.numOfReplicates = 1;
    }

    /**
     * Sets if this is the best mapping for the given read in the complete 
     * reference.
     * @param isBestMapping <cc>true</cc> if this is the best mapping for the
     * read, <cc>false</cc> otherwise
     */
    public void setIsBestMapping(boolean isBestMapping){
        this.bestMapping = isBestMapping;
    }

    /**
     * @return <cc>true</cc> if this is the best mapping for the read, 
     * <cc>false</cc> otherwise
     */
    public boolean isBestMapping(){
        return this.bestMapping;
    }

    public int getNumReplicates(){
        return numOfReplicates;
    }

    public void increaseCounter(){
        ++numOfReplicates;
    }

    /**
     * @return Start position of this mapping. Always the smaller value among start and stop.
     */
    public int getStart() {
        return start;
    }

    /**
     * @return Stop position of this mapping. Always the larger value among start and stop.
     */
    public int getStop() {
        return stop;
    }

    /**
     * @return direction of the mapping: 1 for fwd and -1 for rev
     */
    public byte getDirection(){
        return direction;
    }

    
    public List<ParsedDiff> getDiffs(){
        return diffs;
    }

    
    public boolean hasDiffs(){
        return !diffs.isEmpty();
    }

    
    public List<ParsedReferenceGap> getGenomeGaps(){
        return gaps;
    }

    
    public boolean hasGenomeGaps(){
        return !gaps.isEmpty();
    }

    
    public int getErrors(){
        return errors;
    }

    
    public int getNumOfDiffs(){
        return this.getDiffs().size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParsedMapping other = (ParsedMapping) obj;
        if (this.start != other.getStart()) {
            return false;
        }
        if (this.stop != other.getStop()) {
            return false;
        }
        if (this.direction != other.getDirection()) {
            return false;
        }
        if (this.errors != other.getErrors()) {
            return false;
        }
        if (this.diffs != other.getDiffs() && (this.diffs == null || !this.diffs.equals(other.getDiffs()))) {
            return false;
        }
        if (this.gaps != other.getGenomeGaps() && (this.gaps == null || !this.gaps.equals(other.getGenomeGaps()))) {
            return false;
        }
        if (this.bestMapping != other.isBestMapping()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.start;
        hash = 79 * hash + this.stop;
        hash = 79 * hash + this.direction;
        hash = 79 * hash + this.errors;
        hash = 79 * hash + (this.diffs != null ? this.diffs.hashCode() : 0);
        hash = 79 * hash + (this.gaps != null ? this.gaps.hashCode() : 0);
        hash = 79 * hash + (this.bestMapping ? 1 : 0);
        return hash;
    }

    
    public void setID(long mappingID) {
        this.id = mappingID;
    }

    
    public long getID(){
        return id;
    }

    
    public void setCount(int count){
        this.numOfReplicates = count;
    }

}
