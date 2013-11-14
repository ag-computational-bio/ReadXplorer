/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.view.dataVisualisation;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import java.util.HashMap;
import java.util.HashSet;

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
        this.data = new HashMap<Integer, BoundsInfoManager>();
    }
    
    public BoundsInfoManager get(PersistantReference genome) {
        if (data.containsKey(genome.getId())) return data.get(genome.getId());
        else {
            BoundsInfoManager boundsManager = new BoundsInfoManager(genome);
            data.put(genome.getId(), boundsManager);
            return boundsManager;
        }
    }
    
    public void clear() {
        data.clear();
    }
    
}
