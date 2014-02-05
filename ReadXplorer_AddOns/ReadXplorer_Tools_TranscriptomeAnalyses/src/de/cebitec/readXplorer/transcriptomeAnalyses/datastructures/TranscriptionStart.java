package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;

/**
 * Data structure for storing a transcription start site.
 *
 * @author -Rolf Hilker-, modified by -jritter-
 */
public class TranscriptionStart extends TrackChromResultEntry {

    private int startPosition;
    private boolean isFwdStrand;
    private int readStarts;
    private double relCount;
    private PersistantFeature detectedGene;
    private int offset;
    private int dist2start, dist2stop;
    private PersistantFeature nextDownstreamFeature;
    private int nextOffset;
    private boolean leaderless, cdsShift;
    private String detectedFeatStart, detectedFeatStop;
    private boolean internalTSS;
    private boolean putativeAntisense;
    private boolean selected;
    private boolean falsePositive;
    private String additionalIdentyfier = null;
    private int promotorSequenceLength, rbsSequenceLength;
    private int startMinus10Motif, startMinus35Motif, startRbsMotif;
    private int minus10MotifWidth, minus35MotifWidth, rbsMotifWidth;
    private boolean hasRbsFeatureAssigned, hasPromtorFeaturesAssigned;
    private boolean isConsideredTSS;
    private boolean isIntergenicAntisense;
    /**
     * A comment the user can set in the table during the analysis.
     */
    private String comment;

    /**
     * Data structure for storing a gene start.
     *
     * @param tssStartPosition The position at which the gene start was detected
     * @param isFwdStrand true, if the transcript start was detected on the fwd
     * strand, false otherwise.
     * @param readStarts The number of read starts at the detected tss position
     * @param relCount
     * @param detectedGene feature in downstream direction rel. to the
     * transcription start site with offset > 0.
     * @param offset the distance between transcription start site and detected
     * feature.
     * @param dist2start if a transcription start site is in between an accupied
     * feature region, than this is the distance to the features start position.
     * @param dist2stop if a transcription start site is in between an accupied
     * feature region, than this is the distance to the features stop position.
     * @param nextDownstreamFeature if a transcription start site is in between
     * an accupied feature region, than this is the next feature in downstream
     * direction.
     * @param offsetToNextDownstreamFeature if a transcription start site is in
     * between an accupied feature region, than this is the offset to the next
     * feature lying in downstream direction.
     * @param promotorSequence Sequence in upstream direction rel. to the
     * transcription start site.
     * @param leaderless
     * @param cdsShift
     * @param detectedFeatStart
     * @param detectedFeatStop
     * @param isInternal
     * @param putAS
     * @param trackId Track ID.
     * @param chromId Chromosome ID.
     *
     */
    public TranscriptionStart(int tssStartPosition, boolean isFwdStrand, int readStarts, double relCount, PersistantFeature detectedGene, int offset, int dist2start, int dist2stop, PersistantFeature nextDownstreamFeature,
            int offsetToNextDownstreamFeature, boolean leaderless, boolean cdsShift,
            String detectedFeatStart, String detectedFeatStop, boolean isInternal, boolean putAS, int chromId, int trackId) {
        super(trackId, chromId);
        this.startPosition = tssStartPosition;
        this.isFwdStrand = isFwdStrand;
        this.readStarts = readStarts;
        this.relCount = relCount;
        this.detectedGene = detectedGene;
        this.offset = offset;
        this.dist2start = dist2start;
        this.dist2stop = dist2stop;
        this.nextDownstreamFeature = nextDownstreamFeature;
        this.nextOffset = offsetToNextDownstreamFeature;
        this.leaderless = leaderless;
        this.cdsShift = cdsShift;
        this.detectedFeatStart = detectedFeatStart;
        this.detectedFeatStop = detectedFeatStop;
        this.internalTSS = isInternal;
        this.putativeAntisense = putAS;
    }

    /**
     * @return The position at which the gene start was detected
     */
    public int getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return true, if the transcript start was detected on the fwd strand,
     * false otherwise.
     */
    public boolean isFwdStrand() {
        return this.isFwdStrand;
    }

    /**
     *
     * @return true if TSS is on forward strand.
     */
    public boolean isIsFwdStrand() {
        return isFwdStrand;
    }

    /**
     * Set Strand of TSS.
     *
     * @param isFwdStrand
     */
    public void setIsFwdStrand(boolean isFwdStrand) {
        this.isFwdStrand = isFwdStrand;
    }

    /**
     *
     * @return number of read starts for this TSS.
     */
    public int getReadStarts() {
        return readStarts;
    }

    /**
     *
     * @return <true> if this TSS is in putative antisense location.
     */
    public boolean isPutativeAntisense() {
        return putativeAntisense;
    }

    public boolean isIntergenicAntisense() {
        return isIntergenicAntisense;
    }

    public void setIntergenicAntisense(boolean isIntergenicAntisense) {
        this.isIntergenicAntisense = isIntergenicAntisense;
    }
    
    

    /**
     * Set whether this TSS is in antisense location or not.
     *
     * @param putativeAntisense <true> if this TSS is in putative location.
     */
    public void setPutativeAntisense(boolean putativeAntisense) {
        this.putativeAntisense = putativeAntisense;
    }

    /**
     *
     * @param readStarts
     */
    public void setReadStarts(int readStarts) {
        this.readStarts = readStarts;
    }

    /**
     *
     * @return relative count.
     */
    public double getRelCount() {
        return relCount;
    }

    /**
     *
     * @param relCount
     */
    public void setRelCount(double relCount) {
        this.relCount = relCount;
    }

    /**
     *
     * @return <true> if transcription start site is leaderless.
     */
    public boolean isLeaderless() {
        return leaderless;
    }

    /**
     *
     * @return
     */
    public boolean isCdsShift() {
        return cdsShift;
    }

    public void setCdsShift(boolean cdsShift) {
        this.cdsShift = cdsShift;
    }

    /**
     *
     * @return
     */
    public PersistantFeature getDetectedGene() {
        return detectedGene;
    }

    /**
     *
     * @param detectedGene
     */
    public void setDetectedGene(PersistantFeature detectedGene) {
        this.detectedGene = detectedGene;
    }

    /**
     * Gets the distance between transcriptions start site and translation start
     * site, which is the start of an CDS feature.
     *
     * @return the offset length.
     */
    public int getOffset() {
        return offset;
    }

    /**
     *
     * @param offset distance to translation start site.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     *
     * @return
     */
    public int getDist2start() {
        return dist2start;
    }

    /**
     *
     * @param dist2start
     */
    public void setDist2start(int dist2start) {
        this.dist2start = dist2start;
    }

    /**
     *
     * @return
     */
    public int getDist2stop() {
        return dist2stop;
    }

    /**
     *
     * @param dist2stop
     */
    public void setDist2stop(int dist2stop) {
        this.dist2stop = dist2stop;
    }

    /**
     *
     * @return
     */
    public PersistantFeature getNextGene() {
        return nextDownstreamFeature;
    }

    /**
     *
     * @param nextGene
     */
    public void setNextGene(PersistantFeature nextGene) {
        this.nextDownstreamFeature = nextGene;
    }

    /**
     *
     * @return
     */
    public int getNextOffset() {
        return nextOffset;
    }

    /**
     *
     * @param nextOffset
     */
    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }

    /**
     *
     * @return
     */
    public int getPromotorSequenceLength() {
        return promotorSequenceLength;
    }

    /**
     *
     * @param sequence
     */
    public void setPromotorSequenceLength(int sequence) {
        this.promotorSequenceLength = sequence;
    }

    /**
     *
     * @return
     */
    public String getDetectedFeatStart() {
        return detectedFeatStart;
    }

    /**
     *
     * @return
     */
    public String getDetectedFeatStop() {
        return detectedFeatStop;
    }

    /**
     *
     */
    public boolean isInternalTSS() {
        return internalTSS;
    }

    /**
     *
     * @return
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     *
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     *
     * @return
     */
    public PersistantFeature getAssignedFeature() {
        if (getDetectedGene() != null) {
            return getDetectedGene();
        } else {
            return getNextGene();
        }
    }

    /**
     *
     * @return
     */
    public int getStartMinus10Motif() {
        return startMinus10Motif;
    }

    /**
     *
     * @param startMinus10Motif
     */
    public void setStartMinus10Motif(int startMinus10Motif) {
        this.startMinus10Motif = startMinus10Motif;
    }

    /**
     *
     * @return
     */
    public int getStartMinus35Motif() {
        return startMinus35Motif;
    }

    /**
     *
     * @param startMinus35Motif
     */
    public void setStartMinus35Motif(int startMinus35Motif) {
        this.startMinus35Motif = startMinus35Motif;
    }

    /**
     *
     * @return
     */
    public int getStartRbsMotif() {
        return startRbsMotif;
    }

    /**
     *
     * @param startRbsMotif
     */
    public void setStartRbsMotif(int startRbsMotif) {
        this.startRbsMotif = startRbsMotif;
    }

    /**
     *
     * @return
     */
    public int getRbsSequenceLength() {
        return rbsSequenceLength;
    }

    /**
     *
     * @param rbsSequenceLength
     */
    public void setRbsSequenceLength(int rbsSequenceLength) {
        this.rbsSequenceLength = rbsSequenceLength;
    }

    /**
     * Get the width for the expected motif width for the -10 promotor box.
     *
     * @return -10 motif width.
     */
    public int getMinus10MotifWidth() {
        return minus10MotifWidth;
    }

    /**
     *
     * @param minus10MotifWidth
     */
    public void setMinus10MotifWidth(int minus10MotifWidth) {
        this.minus10MotifWidth = minus10MotifWidth;
    }

    /**
     *
     * @return
     */
    public int getMinus35MotifWidth() {
        return minus35MotifWidth;
    }

    /**
     *
     * @param minus35MotifWidth
     */
    public void setMinus35MotifWidth(int minus35MotifWidth) {
        this.minus35MotifWidth = minus35MotifWidth;
    }

    /**
     *
     * @return
     */
    public int getRbsMotifWidth() {
        return rbsMotifWidth;
    }

    /**
     *
     * @param rbsMotifWidth
     */
    public void setRbsMotifWidth(int rbsMotifWidth) {
        this.rbsMotifWidth = rbsMotifWidth;
    }

    /**
     *
     * @return
     */
    public String getAdditionalIdentyfier() {
        return additionalIdentyfier;
    }

    /**
     *
     * @param additionalIdentyfier
     */
    public void setAdditionalIdentyfier(String additionalIdentyfier) {
        this.additionalIdentyfier = additionalIdentyfier;
    }

    /**
     *
     * @return
     */
    public boolean hasRbsFeatureAssigned() {
        return this.hasRbsFeatureAssigned;
    }

    /**
     *
     * @param isAssigned
     */
    public void setRbsFeatureAssigned(boolean isAssigned) {
        this.hasRbsFeatureAssigned = isAssigned;
    }

    /**
     *
     * @return
     */
    public boolean hasPromotorFeaturesAssigned() {
        return this.hasPromtorFeaturesAssigned;
    }

    /**
     *
     * @param isAssigned
     */
    public void setPromotorFeaturesAssigned(boolean isAssigned) {
        this.hasPromtorFeaturesAssigned = isAssigned;
    }

    public boolean isConsideredTSS() {
        return isConsideredTSS;
    }

    public void setIsconsideredTSS(boolean isconsideredTSS) {
        this.isConsideredTSS = isconsideredTSS;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(boolean falsePositive) {
        this.falsePositive = falsePositive;
    }
}
