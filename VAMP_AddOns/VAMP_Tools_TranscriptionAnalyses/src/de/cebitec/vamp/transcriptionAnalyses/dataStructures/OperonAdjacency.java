package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;

/**
 *
 * @author MKD
 */
public class OperonAdjacency {

    private PersistantAnnotation operonAnnotation1;
    private PersistantAnnotation operonAnnotation2;
    private int readsGene1 = 0;
    private int spanningReads = 0;
    private int readsGene2 = 0;
    private int internalReads = 0;

    public OperonAdjacency(PersistantAnnotation operonAnnotation1, PersistantAnnotation operonAnnotation2) {
        this.operonAnnotation1 = operonAnnotation1;
        this.operonAnnotation2 = operonAnnotation2;

    }

    /**
     * @return the operonAnnotation
     */
    public PersistantAnnotation getOperonAnnotation() {
        return operonAnnotation1;
    }

    /**
     * @param operonAnnotation the operonAnnotation to set
     */
    public void setOperonAnnotation(PersistantAnnotation operonAnnotation) {
        this.operonAnnotation1 = operonAnnotation;
    }

    /**
     * @return the operonAnnotation2
     */
    public PersistantAnnotation getOperonAnnotation2() {
        return operonAnnotation2;
    }

    /**
     * @return the readsGene1
     */
    public int getReadsGene1() {
        return readsGene1;
    }

    /**
     * @param readsGene1 the readsGene1 to set
     */
    public void setReadsGene1(int readsGene1) {
        this.readsGene1 = readsGene1;
    }

    /**
     * @return the spanningReads
     */
    public int getSpanningReads() {
        return spanningReads;
    }

    /**
     * @param spanningReads the spanningReads to set
     */
    public void setSpanningReads(int spanningReads) {
        this.spanningReads = spanningReads;
    }

    /**
     * @return the readsGene2
     */
    public int getReadsGene2() {
        return readsGene2;
    }

    /**
     * @param readsGene2 the readsGene2 to set
     */
    public void setReadsGene2(int readsGene2) {
        this.readsGene2 = readsGene2;
    }

    /**
     * @return the internalReads
     */
    public int getInternalReads() {
        return internalReads;
    }

    /**
     * @param internalReads the internalReads to set
     */
    public void setInternalReads(int internalReads) {
        this.internalReads = internalReads;
    }
}
