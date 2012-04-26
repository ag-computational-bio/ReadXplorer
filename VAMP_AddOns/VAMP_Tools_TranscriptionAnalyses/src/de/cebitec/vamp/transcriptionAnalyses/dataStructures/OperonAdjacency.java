package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;

/**
 * @author MKD, rhilker
 * 
 * A putative operon is a data structure for storing two neighboring annotations,
 * which might form an operon. It also contains the read counts only associated to annotation1/2,
 * the spanning and the internal read counts.
 */
public class OperonAdjacency {

    private PersistantAnnotation annotation1;
    private PersistantAnnotation annotation2;
    private int readsAnnotation1;
    private int spanningReads;
    private int readsAnnotation2;
    private int internalReads;
  
    /**
     * A putative operon is a data structure for storing two neighboring
     * annotations, which might form an operon. It also contains the read counts
     * only associated to annotation1/2, the spanning and the internal read
     * counts.
     */
    public OperonAdjacency(PersistantAnnotation annotation1, PersistantAnnotation annotation2) {
        this.annotation1 = annotation1;
        this.annotation2 = annotation2;
    }

    /**
     * @return the first annotation of the operon
     */
    public PersistantAnnotation getAnnotation1() {
        return annotation1;
    }

    /**
     * @return the second annotation of the operon
     */
    public PersistantAnnotation getAnnotation2() {
        return annotation2;
    }

    /**
     * @return the reads associated with Annotation1
     */
    public int getReadsAnnotation1() {
        return readsAnnotation1;
    }

    /**
     * @param readsAnnotation1 the reads associated with Annotation1
     */
    public void setReadsAnnotation1(int readsAnnotation1) {
        this.readsAnnotation1 = readsAnnotation1;
    }

    /**
     * @return the number of reads spanning from annotation 1 into 2
     */
    public int getSpanningReads() {
        return spanningReads;
    }

    /**
     * @param spanningReads set the number of reads spanning from annotation 1 into 2
     */
    public void setSpanningReads(int spanningReads) {
        this.spanningReads = spanningReads;
    }

    /**
     * @return the readsAnnotation2
     */
    public int getReadsAnnotation2() {
        return readsAnnotation2;
    }

    /**
     * @param readsAnnotation2 the readsAnnotation2 to set
     */
    public void setReadsAnnotation2(int readsAnnotation2) {
        this.readsAnnotation2 = readsAnnotation2;
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
