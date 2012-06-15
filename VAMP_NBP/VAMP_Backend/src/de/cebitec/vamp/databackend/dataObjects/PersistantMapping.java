package de.cebitec.vamp.databackend.dataObjects;

import java.util.*;

/**
 * Data structure for storing a mapping on a reference genome.
 * 
 * @author ddoppmeier, rhilker
 */
public class PersistantMapping implements PersistantObject {

    private int id;
    private int start;
    private int trackId;
    private int stop;
    private byte strand;
    private int count;
    private Map<Integer, PersistantDiff> diffs;
    private TreeMap<Integer, TreeSet<PersistantReferenceGap>> gaps;
    private int differences;
    private int sequenceID;
    private boolean isBestMatch;

    /**
     * Data structure for storing a mapping on a reference genome.
     * @param id
     * @param start
     * @param stop
     * @param trackId
     * @param direction
     * @param count
     * @param errors
     * @param sequenceID
     * @param isBestMapping 
     */
    public PersistantMapping(int id, int start, int stop, int trackId, byte direction, int count, int errors, int sequenceID, boolean isBestMapping){
        this.id = id;
        this.start = start;
        this.stop = stop;
        this.count = count;
        this.trackId = trackId;
        this.strand = direction;
        this.diffs = new HashMap<Integer, PersistantDiff>();
        this.gaps = new TreeMap<Integer, TreeSet<PersistantReferenceGap>>();
        this.differences = errors;
        this.sequenceID = sequenceID;
        this.isBestMatch = isBestMapping;
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
    public byte getStrand() {
        return strand;
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

}
