package de.cebitec.vamp.databackend.dataObjects;

import java.util.*;

/**
 * Data structure for storing a mapping on a reference genome.
 * 
 * @author ddoppmeier, rhilker
 */
public class PersistantMapping implements PersistantObject, Comparable<PersistantMapping> {

    private int id;
    private int start;
    private int trackId;
    private int stop;
    private boolean isFwdStrand;
    private int count;
    private Map<Integer, PersistantDiff> diffs;
    private TreeMap<Integer, TreeSet<PersistantReferenceGap>> gaps;
    private int differences;
    private int sequenceID;
    private boolean isBestMatch;
    private Boolean unique;
    private String originalSequence = null;

    /**
     * Data structure for storing a mapping on a reference genome.
     * @param id
     * @param start
     * @param stop
     * @param trackId
     * @param isFwdStrand
     * @param count
     * @param errors
     * @param sequenceID
     * @param isBestMapping 
     */
    public PersistantMapping(int id, int start, int stop, int trackId, boolean isFwdStrand, int count, int errors, int sequenceID, boolean isBestMapping){
        this.id = id;
        this.start = start;
        this.stop = stop;
        this.count = count;
        this.trackId = trackId;
        this.isFwdStrand = isFwdStrand;
        this.diffs = new HashMap<>();
        this.gaps = new TreeMap<>();
        this.differences = errors;
        this.sequenceID = sequenceID;
        this.isBestMatch = isBestMapping;
        this.unique = false;
    }
    
    /*
     * A minimal version of the mapping class. It is used to collect the count
     * data. For this only start, stop and direction are needed. Everything else
     * isn't needed and can be left out in order to save some memory
     */
    public PersistantMapping(int start, int stop, boolean isFwdStrand){
        this.start = start;
        this.stop = stop;
        this.isFwdStrand = isFwdStrand;
        this.unique = false;
    }

    public int getNbReplicates() {
        return count;
    }
    
    public Map<Integer, PersistantDiff> getDiffs() {
        return diffs;
    }
    
    
    public TreeMap<Integer, TreeSet<PersistantReferenceGap>> getGenomeGaps(){
        return gaps;
    }

    @Override
    public long getId() {
        return id;
    }

    /**
     * @return direction of the mapping: 1 for fwd and -1 for rev
     */
    public boolean isFwdStrand() {
        return isFwdStrand;
    }

    public int getTrackId() {
        return trackId;
    }

    /**
     * @return the absolute start position in genome coordinates.
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the absolute stop position in genome coordinates.
     */
    public int getStop() {
        return stop;
    }

    /**
     * @return the number of differences of this mapping to the reference.
     */
    public int getDifferences(){
        return differences;
    }

    /**
     * Sets the number of differences of this mapping to the reference.
     * @param differences the number of differences of this mapping to the reference
     */
    public void setDifferences(int differences) {
        this.differences = differences;
    }

    public int getSequenceID(){
        return sequenceID;
    }

    public boolean isBestMatch(){
        return isBestMatch;
    }

    public boolean hasGenomeGapAtPosition(int position){
        if(gaps.containsKey(position)){
            return true;
        } else {
            return false;
        }
    }

    public TreeSet<PersistantReferenceGap> getGenomeGapsAtPosition(int position){
        return gaps.get(position);
    }

    public boolean hasDiffAtPosition(int position){
        if(diffs.containsKey(position)){
            return true;
        } else {
            return false;
        }
    }

    public Character getDiffAtPosition(int position){
        return diffs.get(position).getBase();
    }

    public void addGenomeGap(PersistantReferenceGap gap){
        if(!gaps.containsKey(gap.getPosition())){
            gaps.put(gap.getPosition(), new TreeSet<PersistantReferenceGap>());
        }
        gaps.get(gap.getPosition()).add(gap);
    }

    public void addDiff(PersistantDiff d){
        diffs.put(d.getPosition(), d);
    }

    @Override
    public int compareTo(PersistantMapping o) {
        int ret = 0;
        if(this.start < o.getStart()){
            ret = -1;
        }
        if(this.start > o.getStop()){
            ret = 1;
        }
        return ret;
    }
    
    /**
     * sets the unique info of the mapping
     * @param b true if the mapping is unique
     */
    public void setUnique(boolean b) {
        this.unique = b;
    }
    
    /**
     * is the mapping unique?
     * @return true if the mapping is unique
     */
    public Boolean getUnique() {
        return this.unique;
    }

    /**
     * @return the original sequence of the read
     * this info is used only if the rna trim module has been used
     * and the corresponding custom tag is contained in the sam/bam file
     */
    public String getOriginalSequence() {
        return originalSequence;
    }

    /**
     * @param originalSequence the originalSequence to set
     */
    public void setOriginalSequence(String originalSequence) {
        this.originalSequence = originalSequence;
    }
    
    private int trimmedFromLeft = 0;
    private int trimmedFromRight = 0;

    /**
     * @return the trimmedFromLeft
     */
    public int getTrimmedFromLeft() {
        return trimmedFromLeft;
    }

    /**
     * @param trimmedFromLeft the trimmedFromLeft to set
     */
    public void setTrimmedFromLeft(int trimmedFromLeft) {
        this.trimmedFromLeft = trimmedFromLeft;
    }

    /**
     * @return the trimmedFromRight
     */
    public int getTrimmedFromRight() {
        return trimmedFromRight;
    }

    /**
     * @param trimmedFromRight the trimmedFromRight to set
     */
    public void setTrimmedFromRight(int trimmedFromRight) {
        this.trimmedFromRight = trimmedFromRight;
    }
   

}
