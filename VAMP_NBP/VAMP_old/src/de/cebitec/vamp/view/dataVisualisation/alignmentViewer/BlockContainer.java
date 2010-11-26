package de.cebitec.vamp.view.dataVisualisation.alignmentViewer;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author ddoppmei
 */
public class BlockContainer {
    
    private TreeMap<Integer, TreeSet<BlockI>> sortedMappings;

    public BlockContainer(){
        sortedMappings = new TreeMap<Integer, TreeSet<BlockI>>();
    }

    public void addBlock(BlockI block){
        int start = block.getAbsStart();
        if(!sortedMappings.containsKey(start)){
            sortedMappings.put(start, new TreeSet<BlockI>(new BlockComparator()));
        }
        sortedMappings.get(start).add(block);
    }

    public BlockI getNextByPositionAndRemove(int pos){
        Integer key = sortedMappings.ceilingKey(pos);
        if(key != null){
            TreeSet<BlockI> set = sortedMappings.get(key);
            BlockI b = set.pollFirst();
            if(set.isEmpty()){
                sortedMappings.remove(key);
            }
            return b;
        } else {
            return null;
        }
    }

    public boolean isEmpty(){
        return sortedMappings.isEmpty();
    }

    private class BlockComparator implements Comparator<BlockI>{

        @Override
        public int compare(BlockI o1, BlockI o2) {
            // order by start of block
            if(o1.getAbsStart() < o2.getAbsStart()){
                return -1;
            } else if(o1.getAbsStart() > o2.getAbsStart()){
                return 1;
            } else {
                // if blocks start at identical position use stop position
                if(o1.getAbsStop() < o2.getAbsStop()){
                    return -1;
                } else if (o1.getAbsStop() > o2.getAbsStop()){
                    return 1;
                } else {
                    // stop position are identical, too
                    // use mapping id to distinguish and order
                    if(o1.getMapping().getId() < o2.getMapping().getId()){
                        return -1;
                    } else if(o1.getMapping().getId() > o2.getMapping().getId()){
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

}
