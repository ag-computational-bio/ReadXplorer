package de.cebitec.readXplorer.transcriptionAnalyses.dataStructures;

import de.cebitec.readXplorer.databackend.dataObjects.TrackResultEntry;

/**
 * Data structure for storing a gene start with position, strand, initial coverage
 * (coverage directly before predicted gene start) and start coverage (coverage 
 * at predicted gene start).
 *
 * @author -Rolf Hilker-
 */
public class TranscriptionStart extends TrackResultEntry {
    
    private int pos;
    private boolean isFwdStrand;
    private int readStarts;
    private DetectedFeatures detFeatures;
    private int percentIncrease;
    private int coverageIncrease;
    
    /**
     * Data structure for storing a gene start.
     * @param pos The position at which the gene start was detected
     * @param isFwdStrand true, if the transcript start was detected on the fwd strand, false otherwise.
     * @param readStarts The number of read starts at the detected tss position
     * @param percentIncrease The coverage at the detected gene start position
     * (getPos()).
     * @param coverageIncrease the coverage increase from the position before
     * the TSS to the detected TSS position
     * @param detFeatures object containing the features associated to this predicted gene start
     */
    public TranscriptionStart(int pos, boolean isFwdStrand, int readStarts, int percentIncrease,
            int coverageIncrease, DetectedFeatures detFeatures, int trackId) {
        super(trackId);
        this.pos = pos;
        this.isFwdStrand = isFwdStrand;
        this.readStarts = readStarts;
        this.percentIncrease = percentIncrease;
        this.coverageIncrease = coverageIncrease;
        this.detFeatures = detFeatures;
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
     * @return The number of read starts at the detected gene start
     */
    public int getReadStartsAtPos() {
        return this.readStarts;
    }

    /**
     * @return The object containing the features associated to this predicted gene start.
     */
    public DetectedFeatures getDetFeatures() {
        return this.detFeatures;
    }

    /**
     * @return The coverage at the detected gene start position
     * (getPos()).
     */
    public int getPercentIncrease() {
        return this.percentIncrease;
    }

    /**
     * @return The coverage increase from the position before
     * the TSS to the detected TSS position
     */
    public int getCoverageIncrease() {
        return this.coverageIncrease;
    }
}
