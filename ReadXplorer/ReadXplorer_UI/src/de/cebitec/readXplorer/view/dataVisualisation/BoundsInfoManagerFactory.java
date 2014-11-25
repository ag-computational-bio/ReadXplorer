package de.cebitec.readXplorer.view.dataVisualisation;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import java.util.HashMap;

/**
 * This class keeps track of created BoundsInfoManager-Instances and 
 * ensures that there is only one BoundsInfoManager per genome
 * 
 * This is to ensure, that the panel navigation (i.e. "Jump to") still
 * works even if there are multiple genome object instances in use
 * @author Evgeny Anisiforov
 */
public class BoundsInfoManagerFactory {
    private HashMap<Integer, BoundsInfoManager> data;
    
    public BoundsInfoManagerFactory() {
        this.data = new HashMap<>();
    }
    
    public BoundsInfoManager get(PersistantReference genome) {
        if (data.containsKey(genome.getId())) { 
            return data.get(genome.getId());
        } else {
            BoundsInfoManager boundsManager = new BoundsInfoManager(genome);
            data.put(genome.getId(), boundsManager);
            return boundsManager;
        }
    }
    
    public void clear() {
        data.clear();
    }
    
}
