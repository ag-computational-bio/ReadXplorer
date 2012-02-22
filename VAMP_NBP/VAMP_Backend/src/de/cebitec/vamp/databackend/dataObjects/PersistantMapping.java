package de.cebitec.vamp.databackend.dataObjects;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantMapping implements PersistantObject {

    private int id;
    private int start;
    private int trackId;
    private int stop;
    private byte strand;
    private Map<Integer, PersistantDiff> diffs;
    private int count;
    private TreeMap<Integer, TreeSet<PersistantReferenceGap>> gaps;
    private int errors;
    private int sequenceID;
    private boolean isBestMatch;

    public PersistantMapping(int id, int start, int stop, int trackId, byte direction, int count, int errors, int sequenceID, boolean isBestMapping){
        this.id = id;
        this.start = start;
        this.stop = stop;
        this.count = count;
        this.trackId = trackId;
        strand = direction;
        diffs = new HashMap<Integer, PersistantDiff>();
        gaps = new TreeMap<Integer, TreeSet<PersistantReferenceGap>>();
        this.errors = errors;
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

    public byte getStrand() {
        return strand;
    }

    public int getTrackId() {
        return trackId;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public int getErrors(){
        return errors;
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
