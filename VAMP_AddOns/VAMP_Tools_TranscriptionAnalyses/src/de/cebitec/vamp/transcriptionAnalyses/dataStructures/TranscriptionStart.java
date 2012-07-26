package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

/**
 * @author -Rolf Hilker-
 * 
 * Data structure for storing a gene start with position, strand, initial coverage
 * (coverage directly before predicted gene start) and start coverage (coverage 
 * at predicted gene start).
 */
public class TranscriptionStart {
    
    private int pos;
    private boolean isFwdStrand;
    private int initialCoverage;
    private int startCoverage;
    private DetectedAnnotations detAnnotations;
    
    /**
     * Data structure for storing a gene start.
     * @param pos The position at which the gene start was detected
     * @param isFwdStrand true, if the transcript start was detected on the fwd strand, false otherwise.
     * @param initialCoverage The coverage directly before the detected gene start
     * @param startCoverage The coverage at the detected gene start position (getPos()).
     * @param detAnnotations object containing the annotations associated to this predicted gene start
     */
    public TranscriptionStart(int pos, boolean isFwdStrand, int initialCoverage, int startCoverage, DetectedAnnotations detAnnotations) {
        this.pos = pos;
        this.isFwdStrand = isFwdStrand;
        this.initialCoverage = initialCoverage;
        this.startCoverage = startCoverage;
        this.detAnnotations = detAnnotations;
    }

    /**
     * @return The position at which the gene start was detected
     */
    public int getPos() {
        return this.pos;
    }

    /**
     * @return true, if the transcript start was detected on the fwd strand, false otherwise.
     */
    public boolean isFwdStrand() {
        return this.isFwdStrand;
    }

    /**
     * @return The coverage directly before the detected gene start
     */
    public int getInitialCoverage() {
        return this.initialCoverage;
    }

    /**
     * @return The coverage at the detected gene start position (getPos()).
     */
    public int getStartCoverage() {
        return this.startCoverage;
    }

    /**
     * @return The object containing the annotations associated to this predicted gene start.
     */
    public DetectedAnnotations getDetAnnotations() {
        return this.detAnnotations;
    }
    
    
}
