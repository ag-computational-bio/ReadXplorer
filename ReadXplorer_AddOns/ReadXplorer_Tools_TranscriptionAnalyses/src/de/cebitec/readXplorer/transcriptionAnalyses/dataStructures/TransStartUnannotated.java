package de.cebitec.readXplorer.transcriptionAnalyses.dataStructures;

/**
 * An extension to the classic TranscriptionStart data structure, adding data
 * fields for storage of unannotated transcript information.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransStartUnannotated extends TranscriptionStart {

    private int detectedStop;
    
    /**
     * An extension to the classic TranscriptionStart data structure, adding
     * data fields for storage of unannotated gene information.
     * @param pos The position at which the gene start was detected
     * @param isFwdStrand true, if the gene start was detected on the fwd strand, false otherwise.
     * @param readStarts The number of read starts at the detected tss position
     * @param percentIncrease The coverage at the detected gene start position (getPos()).
     * @param coverageIncrease the coverage increase from the position before the TSS to
     * the detected TSS position
     * @param detFeatures object containing the features associated to this predicted gene start
     * @param detectedStop The stop position of the predicted unannotated transcript
     * @param trackId The id of the track to which the TSS belongs
     * @param chromId The id of the chromosome to which the TSS belongs
     */
    public TransStartUnannotated(int pos, boolean isFwdStrand, int readStarts, int percentIncrease,
            int coverageIncrease, DetectedFeatures detFeatures, int detectedStop, int trackId, int chromId) {
        super(pos, isFwdStrand, readStarts, percentIncrease, coverageIncrease, detFeatures, trackId, chromId);
        this.detectedStop = detectedStop;
    }

    /**
     * @return The stop position of the predicted unannotated transcript.
     */
    public int getDetectedStop() {
        return this.detectedStop;
    }
    
}
