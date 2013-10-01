package de.cebitec.vamp.transcriptomeAnalyses.datastructure;

import de.cebitec.vamp.databackend.dataObjects.TrackResultEntry;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MKD, rhilker
 * 
 * Data structure for storing operons. Operons consist of a list of OperonAdjacencies, since
 * each operon can contain more than two genes.
 */
public class Operon extends TrackResultEntry {
    
    private List<OperonAdjacency> operonAdjacencies;
   
    public Operon(int trackId) {
        super(trackId);
        this.operonAdjacencies = new ArrayList<>();
    }

    /**
     * @return the operon adjacencies of this operon
     */
    public List<OperonAdjacency> getOperonAdjacencies() {
        return this.operonAdjacencies;
    }

    /**
     * @param operon the operon adjacencies to associate with this operon object.
     */
    public void setOperonAdjacencies(List<OperonAdjacency> newOperonAdjacencys) {
         this.operonAdjacencies = newOperonAdjacencys;
    }
    
    /**
     * Remove all operon adjacencies associated with this operon object.
     */
    public void clearOperonAdjacencyList() {
        this.operonAdjacencies.removeAll(this.operonAdjacencies);
    }

    /**
     * Adds the operon adjacency to the list of OperonAdjacencies.
     * @param operonAdjacency 
     */
    public void addOperonAdjacency(OperonAdjacency operonAdjacency) {
        this.operonAdjacencies.add(operonAdjacency);
    }
    
    /**
     * Adds the operon adjacencies to the end of the list of OperonAdjacencies.
     * @param operonAdjacencies 
     */
    public void addAllOperonAdjacencies(List<OperonAdjacency> operonAdjacencies) {
        this.operonAdjacencies.addAll(operonAdjacencies);
    }
}
