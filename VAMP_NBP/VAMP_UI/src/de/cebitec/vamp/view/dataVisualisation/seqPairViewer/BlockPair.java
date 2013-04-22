package de.cebitec.vamp.view.dataVisualisation.seqPairViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantMapping;
import de.cebitec.vamp.databackend.dataObjects.PersistantObject;
import de.cebitec.vamp.databackend.dataObjects.PersistantSequencePair;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.BlockI;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.Brick;
import java.util.Iterator;
import java.util.List;

/**
 * A BlockPair is a block that contains detailed information about 
 * one sequence pair id = all corresponding mappings in the currently visible interval.
 * 
 * @author rhilker
 */
public class BlockPair implements BlockI {

    private int absStart;
    private int absStop;
    private List<PersistantSequencePair> seqPairs;
    private List<PersistantMapping> singleMappings;
    private long seqPairId;

    /**
     * A block is a block that contains detailed information about one sequence pair id = all corresponding mappings.
     * @param absStart start of the block as sequence position (might be larger than start of mapping, when not in visible interval)
     * @param absStop stop of the block as sequence position (might be smaller than stop of mapping, when not in visible interval)
     * @param seqPairs sequence pair whose detailed information is needed
     * @param singleMappings single mappings of this block
     * @param seqPairId sequence pair id
     */
    public BlockPair(int absStart, int absStop, List<PersistantSequencePair> seqPairs, 
            List<PersistantMapping> singleMappings, long seqPairId){
        this.absStart = absStart;
        this.absStop = absStop;
        this.seqPairs = seqPairs;
        this.singleMappings = singleMappings;
        this.seqPairId = seqPairId;
    }


    @Override
    public int getAbsStart() {
        return this.absStart;
    }

    @Override
    public int getAbsStop() {
        return this.absStop;
    }
    
    
    public long getSeqPairId(){
        return this.seqPairId;
    }

    
    public List<PersistantSequencePair> getSeqPairs(){
        return this.seqPairs;
    }

    
    public List<PersistantMapping> getSingleMappings() {
        return this.singleMappings;
    }

    
    @Override
    public String toString(){
        return "TODO: implement to string for BlockPair";
    }

   /**
     * @return null, because it is not supported for BlockPairs!
     */
    @Override
    public Iterator<Brick> getBrickIterator() {
        return null;
    }

    /**
     * @return -1, because it is not supported for BlockPairs!
     */
    @Override
    public int getNumOfBricks() {
        return -1;
    }

    
    @Override
    public PersistantObject getPersistantObject() {
        if (this.seqPairs.size() > 0) { //TODO: this is not ideal, common mapping can be followed by perfect!
            return this.seqPairs.get(0);
        } else {
            return (PersistantObject) this.singleMappings.get(0);
        }
    }


}
