package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;

/**
 *
 * @author MKD
 */
public class PutativeOperon {

    private PersistantAnnotation gene1;
    private PersistantAnnotation gene2;
    private int readsGene1;
    private int spanningReads;
    private int readsGene2;
    private int internalReads;
    public PutativeOperon(PersistantAnnotation gene1, PersistantAnnotation gene2) {
        this.gene1 = gene1;
        this.gene2 = gene2;
    }

    /**
     * @return the gene1
     */
    public PersistantAnnotation getGene1() {
        return gene1;
    }

    /**
     * @return the gene2
     */
    public PersistantAnnotation getGene2() {
        return gene2;
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
