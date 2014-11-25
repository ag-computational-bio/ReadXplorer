package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.util.ReadPairType;
import java.awt.Color;

/**
 * Creates a new persistant sequence pair. If both mappings of the pair are
 * visible the second mapping has to be added separately.
 * TODO: persistant objects vereinheitlichen, wo möglich
 * 
 * @author Rolf Hilker
 */
public class PersistantReadPair implements PersistantObject {

    private long seqPairID;
    private long mapping2Id;
    private ReadPairType seqPairType;
    private int seqPairReplicates;
    private PersistantMapping visibleMapping;
    private PersistantMapping visiblemapping2;
    
    
    /**
     * Creates a new persistant sequence pair. If both mappings of the pair are
     * visible the second mapping has to be added separately.
     * @param seqPairID id of the pair, will identify all mappings belonging to this pair id
     * @param mapping1ID id of mapping 1 of this pair
     * @param mapping2ID id of mapping 2 of this pair
     * @param seqPairType type of the sequence pair (@see SeqPairClassifier constants with values: 0-6)
     * @param seqPairReplicates number of replicates of this pair
     * @param visibleMapping currently visible mapping of the pair
     */
    public PersistantReadPair(long seqPairID, long mapping1ID, long mapping2ID, ReadPairType seqPairType, 
            int seqPairReplicates, PersistantMapping visibleMapping) {
        this.seqPairID = seqPairID;
        this.mapping2Id = mapping1ID == visibleMapping.getId() ? mapping2ID : mapping1ID;
        this.seqPairType = seqPairType;
        this.seqPairReplicates = seqPairReplicates == 0 ? 1 : seqPairReplicates;
        this.visibleMapping = visibleMapping;
    }
    
    /**
     * Creates a new persistant sequence pair. If both mappings of the pair are
     * visible the second mapping has to be added separately.
     * @param seqPairID id of the pair, will identify all mappings belonging to
     * this pair id
     * @param mapping1ID id of mapping 1 of this pair
     * @param mapping2ID id of mapping 2 of this pair
     * @param seqPairType type of the sequence pair (
     * @see SeqPairClassifier constants with values: 0-6)
     * @param seqPairReplicates number of replicates of this pair
     * @param visibleMapping currently visible mapping of the pair
     * @param mate the mate of the visibleMapping = other read of the pair 
     */
    public PersistantReadPair(long seqPairID, long mapping1ID, long mapping2ID, ReadPairType seqPairType,
            int seqPairReplicates, PersistantMapping visibleMapping, PersistantMapping mate) {
        this.seqPairID = seqPairID;
        this.mapping2Id = mapping1ID == visibleMapping.getId() ? mapping2ID : mapping1ID;
        this.seqPairType = seqPairType;
        this.seqPairReplicates = seqPairReplicates == 0 ? 1 : seqPairReplicates;
        this.visibleMapping = visibleMapping;
        this.visiblemapping2 = mate;
    }

    public long getMapping2Id() {
        return mapping2Id;
    }

    public void setMapping2Id(long mapping2Id) {
        this.mapping2Id = mapping2Id;
    }

    @Override
    public long getId() {
        return seqPairID;
    }

    public void setSeqPairID(long seqPairID) {
        this.seqPairID = seqPairID;
    }

    public int getSeqPairReplicates() {
        return seqPairReplicates;
    }

    public void setSeqPairReplicates(int seqPairReplicates) {
        this.seqPairReplicates = seqPairReplicates;
    }

    public ReadPairType getReadPairType() {
        return seqPairType;
    }

    public void setSeqPairType(ReadPairType seqPairType) {
        this.seqPairType = seqPairType;
    }

    public PersistantMapping getVisibleMapping() {
        return visibleMapping;
    }

    public void setVisibleMapping(PersistantMapping visibleMapping) {
        this.visibleMapping = visibleMapping;
    }

    /**
     * @return If both mappings of the pair are visible, it returns the second mapping
     * otherwise it returns null
     */
    public PersistantMapping getVisibleMapping2() {
        return visiblemapping2;
    }

    /**
     * If both mappings of the pair are visible, set the second mapping by using this method
     * @param visiblemapping2  the second mapping of the pair
     */
    public void setVisiblemapping2(PersistantMapping visiblemapping2) {
        this.visiblemapping2 = visiblemapping2;
    }
    
    /**
     * @return start position of the visible mapping or, if both mappings of the
     * pair are visible, returns the smaller start position among both
     */
    public long getStart(){
        if (this.visiblemapping2 == null){
            return this.visibleMapping.getStart();
        } else {
            long start1 = this.visibleMapping.getStart();
            long start2 = this.visiblemapping2.getStart();
            return start1 < start2 ? start1 : start2;
        }
    }
    
    /**
     * @return stop position of the visible mapping or, if both mappings of the
     * pair are visible, returns the larger stop position among both
     */
    public long getStop(){
        if (this.visiblemapping2 == null){
            return this.visibleMapping.getStop();
        } else {
            long stop1 = this.visibleMapping.getStop();
            long stop2 = this.visiblemapping2.getStop();
            return stop1 > stop2 ? stop1 : stop2;
        }
    }

    /**
     * @return true, if this sequence pair already has a second visible mapping, false otherwise
     */
    public boolean hasVisibleMapping2() {
        return this.visiblemapping2 != null;
    }
    
    /**
     * Determines the color according to the type of a read pair.
     * @param type the type of the read pair
     * @return the color representing this read pair
     */
    public static Color determineReadPairColor(ReadPairType type) {
        Color blockColor;
        if (type == ReadPairType.PERFECT_PAIR || type == ReadPairType.PERFECT_UNQ_PAIR) {
            blockColor = ColorProperties.BLOCK_PERFECT;
        } else if (type == ReadPairType.UNPAIRED_PAIR) {
            blockColor = ColorProperties.BLOCK_UNPAIRED;
        } else {
            blockColor = ColorProperties.BLOCK_DIST_SMALL;
        }
        return blockColor;
    }
    
}
