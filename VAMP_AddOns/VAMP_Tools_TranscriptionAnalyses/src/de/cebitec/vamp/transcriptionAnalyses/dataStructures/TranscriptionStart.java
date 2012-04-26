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
    private byte strand;
    private int initialCoverage;
    private int startCoverage;
    private DetectedAnnotations detAnnotations;
    
    /**
     * Data structure for storing a gene start.
     * @param pos The position at which the gene start was detected
     * @param strand The strand on which the gene start was detected.
     * @param initialCoverage The coverage directly before the detected gene start
     * @param startCoverage The coverage at the detected gene start position (getPos()).
     * @param detAnnotations object containing the annotations associated to this predicted gene start
     */
    public TranscriptionStart(int pos, byte strand, int initialCoverage, int startCoverage, DetectedAnnotations detAnnotations) {
        this.pos = pos;
        this.strand = strand;
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
     * @return The strand on which the gene start was detected.
     */
    public byte getStrand() {
        return this.strand;
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
