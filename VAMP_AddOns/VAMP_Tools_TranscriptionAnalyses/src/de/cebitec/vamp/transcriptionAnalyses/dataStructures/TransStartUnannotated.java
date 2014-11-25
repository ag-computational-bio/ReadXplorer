package de.cebitec.vamp.transcriptionAnalyses.dataStructures;

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
     * @param initialCoverage The coverage directly before the detected gene start
     * @param startCoverage The coverage at the detected gene start position (getPos()).
     * @param detFeatures object containing the features associated to this predicted gene start
     * @param detectedStop The stop position of the predicted unannotated transcript
     */
    public TransStartUnannotated(int pos, boolean isFwdStrand, int initialCoverage, int startCoverage, 
            DetectedFeatures detFeatures, int detectedStop, int trackId) {
        super(pos, isFwdStrand, initialCoverage, startCoverage, detFeatures, trackId);
        this.detectedStop = detectedStop;
    }

    /**
     * @return The stop position of the predicted unannotated transcript.
     */
    public int getDetectedStop() {
        return this.detectedStop;
    }
    
}
