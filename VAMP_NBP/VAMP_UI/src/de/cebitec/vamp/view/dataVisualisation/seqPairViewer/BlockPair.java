package de.cebitec.vamp.view.dataVisualisation.seqPairViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantObject;
import de.cebitec.vamp.databackend.dataObjects.PersistantSeqPairGroup;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.BlockI;
import de.cebitec.vamp.view.dataVisualisation.alignmentViewer.Brick;
import java.util.Iterator;

/**
 * A BlockPair is a block that contains detailed information about 
 * one sequence pair id = all corresponding mappings in the currently visible interval.
 * 
 * @author rhilker
 */
public class BlockPair implements BlockI {

    private int absStart;
    private int absStop;
    private PersistantSeqPairGroup seqPairGroup;

    /**
     * A block is a block that contains detailed information about one sequence pair id = all corresponding mappings.
     * @param absStart start of the block as sequence position (might be larger than start of mapping, when not in visible interval)
     * @param absStop stop of the block as sequence position (might be smaller than stop of mapping, when not in visible interval)
     * @param seqPairGroup seq pair group of this block
     */
    public BlockPair(int absStart, int absStop, PersistantSeqPairGroup seqPairGroup){
        this.absStart = absStart;
        this.absStop = absStop;
        this.seqPairGroup = seqPairGroup;
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
        return this.seqPairGroup.getId();
    }

    
    @Override
    public String toString() {
        //TODO: implement to string for BlockPair
        return "";
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

    /**
     * @return The associated seq pair group.
     */
    @Override
    public PersistantObject getPersistantObject() {
        return this.seqPairGroup;
    }
}
