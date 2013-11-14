package de.cebitec.readXplorer.transcriptomeAnalyses.datastructure;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;

/**
 * A putative operon is a data structure for storing two neighboring features,
 * which might form an operon. It also contains the read counts only associated to feature1/2,
 * the spanning and the internal read counts.
 *
 * @author MKD, rhilker
 */
public class OperonAdjacency {

    private PersistantFeature feature1;
    private PersistantFeature feature2;
    private int readsFeature1;
    private int spanningReads;
    private int readsFeature2;
    private int internalReads;
  
    /**
     * A putative operon is a data structure for storing two neighboring
     * features, which might form an operon. It also contains the read counts
     * only associated to feature1/2, the spanning and the internal read
     * counts.
     */
    public OperonAdjacency(PersistantFeature feature1, PersistantFeature feature2) {
        this.feature1 = feature1;
        this.feature2 = feature2;
    }

    /**
     * @return the first feature of the operon
     */
    public PersistantFeature getFeature1() {
        return feature1;
    }

    /**
     * @return the second feature of the operon
     */
    public PersistantFeature getFeature2() {
        return feature2;
    }

    /**
     * @return the reads associated with Feature1
     */
    public int getReadsFeature1() {
        return readsFeature1;
    }

    /**
     * @param readsFeature1 the reads associated with Feature1
     */
    public void setReadsFeature1(int readsFeature1) {
        this.readsFeature1 = readsFeature1;
    }

    /**
     * @return the number of reads spanning from feature 1 into 2
     */
    public int getSpanningReads() {
        return spanningReads;
    }

    /**
     * @param spanningReads set the number of reads spanning from feature 1 into 2
     */
    public void setSpanningReads(int spanningReads) {
        this.spanningReads = spanningReads;
    }

    /**
     * @return the readsFeature2
     */
    public int getReadsFeature2() {
        return readsFeature2;
    }

    /**
     * @param readsFeature2 the readsFeature2 to set
     */
    public void setReadsFeature2(int readsFeature2) {
        this.readsFeature2 = readsFeature2;
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
