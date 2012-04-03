/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MKD
 */
public class Operon {
    private List<OperonAdjacency> operon;
   
    public Operon(){
        this.operon=new ArrayList<OperonAdjacency>();
    
    }

    /**
     * @return the operon
     */
    public List<OperonAdjacency> getOperon() {
       
        return this.operon;
    }

    /**
     * @param operon the operon to set
     */
    public void setOperon(List newOperonAdjacencys) {
         this.operon=newOperonAdjacencys;
         
            
    }
    public void clearList(){
    
    operon.removeAll(operon);
    }
}
