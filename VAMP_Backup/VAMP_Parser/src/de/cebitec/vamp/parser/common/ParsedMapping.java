package de.cebitec.vamp.parser.common;

import java.util.List;

/**
 *
 * @author ddoppmeier
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
    private int count;

    public ParsedMapping(int start, int stop, byte direction, List<ParsedDiff> diffs, List<ParsedReferenceGap> gaps, int errors){
        this.start = start;
        this.stop = stop;
        this.direction = direction;
        this.diffs = diffs;
        this.gaps = gaps;
        this.errors = errors;
        bestMapping = false;
        count = 1;
    }

    public void setIsBestmapping(boolean b){
        bestMapping = b;
    }

    public boolean isBestMapping(){
        return bestMapping;
    }

    public int getCount(){
        return count;
    }

    public void increaseCounter(){
        count++;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public byte getDirection(){
        return direction;
    }

    public List<ParsedDiff> getDiffs(){
        return diffs;
    }

    public boolean hasDiffs(){
        if(diffs.isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    public List<ParsedReferenceGap> getGenomeGaps(){
        return gaps;
    }

    public boolean hasGenomeGaps(){
        if(gaps.isEmpty()){
            return false;
        } else {
            return true;
        }
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
        if (this.start != other.start) {
            return false;
        }
        if (this.stop != other.stop) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        if (this.errors != other.errors) {
            return false;
        }
        if (this.diffs != other.diffs && (this.diffs == null || !this.diffs.equals(other.diffs))) {
            return false;
        }
        if (this.gaps != other.gaps && (this.gaps == null || !this.gaps.equals(other.gaps))) {
            return false;
        }
        if (this.bestMapping != other.bestMapping) {
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
        this.count = count;
    }

}
