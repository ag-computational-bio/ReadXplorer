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
    private int numReplicates;
    private Map<Integer, PersistantDiff> diffs;
    private TreeMap<Integer, TreeSet<PersistantReferenceGap>> gaps;
    private int differences;
    private int sequenceID;
    private boolean isBestMatch;
    private int numMappingsForRead;
    private String originalSequence = null;

    /**
     * Data structure for storing a mapping on a reference genome.
     * @param id
     * @param start
     * @param stop
     * @param trackId
     * @param isFwdStrand
     * @param numReplicates
     * @param mismatches
     * @param sequenceID
     * @param isBestMapping 
     * @param numMappingsForRead number of mappings for the read of this mapping
     */
    public PersistantMapping(int id, int start, int stop, int trackId, boolean isFwdStrand, 
            int numReplicates, int mismatches, int sequenceID, boolean isBestMapping, int numMappingsForRead){
        this.id = id;
        this.start = start;
        this.stop = stop;
        this.numReplicates = numReplicates;
        this.trackId = trackId;
        this.isFwdStrand = isFwdStrand;
        this.diffs = new HashMap<>();
        this.gaps = new TreeMap<>();
        this.differences = mismatches;
        this.sequenceID = sequenceID;
        this.isBestMatch = isBestMapping;
        this.numMappingsForRead = numMappingsForRead;
    }
    
    /**
     * Data structure for storing a mapping on a reference genome, in case no information is 
     * given about the number of mappings for the read.
     * @param id
     * @param start
     * @param stop
     * @param trackId
     * @param isFwdStrand
     * @param numReplicates
     * @param mismatches
     * @param sequenceID
     * @param isBestMapping 
     */
    public PersistantMapping(int id, int start, int stop, int trackId, boolean isFwdStrand, 
            int numReplicates, int mismatches, int sequenceID, boolean isBestMapping) {
        this(id, start, stop, trackId, isFwdStrand, numReplicates, mismatches, sequenceID, isBestMapping, -1);
    }
    
    /*
     * A minimal version of the mapping class. It is used to collect the count
     * data. For this only start, stop and direction are needed. Everything else
     * isn't needed and can be left out in order to save some memory
     */
    public PersistantMapping(int start, int stop, boolean isFwdStrand, int numReplicates) {
        this.start = start;
        this.stop = stop;
        this.isFwdStrand = isFwdStrand;
        this.numReplicates = numReplicates;
    }

    /**
     * @return The number of replicates of this mapping
     */
    public int getNbReplicates() {
        return numReplicates;
    }
    
    /**
     * @return The complete Map of differences to the reference for this mapping
     */
    public Map<Integer, PersistantDiff> getDiffs() {
        return diffs;
    }
    
    /**
     * @return The complete TreeMap of genome gaps for this mapping
     */
    public TreeMap<Integer, TreeSet<PersistantReferenceGap>> getGenomeGaps(){
        return gaps;
    }

    /**
     * @return The unique id of this mapping.
     */
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

    /**
     * @return The track id of this mapping.
     */
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
     * @return The number of differences of this mapping to the reference.
     */
    public int getDifferences() {
        return differences;
    }

    /**
     * Sets the number of differences of this mapping to the reference.
     * @param differences the number of differences of this mapping to the reference
     */
    public void setDifferences(int differences) {
        this.differences = differences;
    }

    /**
     * @return The ID for this mapping sequence, not for the mapping itself!
     */
    public int getSequenceID() {
        return sequenceID;
    }

    /**
     * @return <tt>true</tt>, if this mapping belongs to the best match class
     */
    public boolean isBestMatch() {
        return isBestMatch;
    }
    
    /**
     * @param position the position which should be checked for reference gaps
     * @return <tt>true</tt>, if reference gaps are stored for the given 
     * position, <tt>false</tt> otherwise
     */
    public boolean hasGenomeGapAtPosition(int position) {
        return gaps.containsKey(position);
    }

    /**
     * @param position the position which should be checked for reference gaps
     * @return A TreeSet containing the reference gaps for the given position.
     * If no gaps are stored for the position <tt>null</tt> is returned
     */
    public TreeSet<PersistantReferenceGap> getGenomeGapsAtPosition(int position) {
        return gaps.get(position);
    }

    /**
     * @param position the position which should be checked for a difference
     * to the reference
     * @return <tt>true</tt>, if a difference is stored for the given position,
     * <tt>false</tt> otherwise
     */
    public boolean hasDiffAtPosition(int position){
        return diffs.containsKey(position);
    }

    /**
     * @param position the position whose difference to the reference should be
     * returned.
     * @return The character deviating from the reference at the given position
     * Returns <tt>null</tt>, if no diff is stored for the given position
     */
    public Character getDiffAtPosition(int position){
        return diffs.get(position).getBase();
    }

    /**
     * Adds a genome gap for a position of this mapping.
     * @param gap the gap to add to this mapping
     */
    public void addGenomeGap(PersistantReferenceGap gap) {
        if (!gaps.containsKey(gap.getPosition())) {
            gaps.put(gap.getPosition(), new TreeSet<PersistantReferenceGap>());
        }
        gaps.get(gap.getPosition()).add(gap);
    }

    /**
     * Adds a difference to the reference for a position of this mapping.
     * @param diff the difference to add to this mapping
     */
    public void addDiff(PersistantDiff diff){
        diffs.put(diff.getPosition(), diff);
    }

    /**
     * Compares two mappings based on their start position. '0' is returned for
     * equal start positions, 1, if the start position of the other is larger
     * and -1, if the start position of this mapping is larger.
     * @param mapping mapping to compare to this mapping 
     * @return '0' for equal start positions, 1, if the start
     * position of the other is larger and -1, if the start position of this
     * mapping is larger.
     */
    @Override
    public int compareTo(PersistantMapping mapping) {
        int result = 0;
        if (this.start < mapping.getStart()) {
            result = -1;
        } else if (this.start > mapping.getStart()) {
            result = 1;
        }
        return result;
    }
    
    /**
     * is the mapping unique?
     * @return true if the mapping is unique
     */
    public boolean isUnique() {
        return this.numMappingsForRead == 1;
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

    /**
     * @return The number of mappings for the read of this mapping. -1 Means that 
     * this information is not available.
     */
    public int getNumMappingsForRead() {
        return this.numMappingsForRead;
    }
   

}
