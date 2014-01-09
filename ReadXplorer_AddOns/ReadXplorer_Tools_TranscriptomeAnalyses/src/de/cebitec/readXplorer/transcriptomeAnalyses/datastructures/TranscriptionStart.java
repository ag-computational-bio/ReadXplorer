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
    private String sequence;
    private boolean leaderless, cdsShift;
    private String detectedFeatStart, detectedFeatStop;
    private boolean internalTSS;
    private boolean putativeAntisense;
    private boolean selected;

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
            int offsetToNextDownstreamFeature, String promotorSequence, boolean leaderless, boolean cdsShift,
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
        this.sequence = promotorSequence;
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
     * @return true if this TSS is in putative antisense location. 
     */
    public boolean isPutativeAntisense() {
        return putativeAntisense;
    }

    /**
     * Set whether this TSS is in antisense location or not. 
     * @param putativeAntisense true if this TSS is in putative location.
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
     * @return 
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
     * @return 
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
     * 
     * @return 
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 
     * @param offset 
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
    public String getSequence() {
        return sequence;
    }

    /**
     * 
     * @param sequence 
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
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

    @Override
    public String toString() {

        if (isFwdStrand) {
            return this.startPosition + "\t" + "fwd\t" + this.readStarts + "\t" + this.relCount + "\t" + this.detectedGene.getFeatureName() + "\t" + this.offset + "\t" + this.dist2start + "\t" + this.dist2stop + "\t" + this.nextDownstreamFeature + "\t" + this.nextOffset + "\t" + this.sequence + "\t" + 0;

        } else {
            return this.startPosition + "\t" + "rev\t" + this.readStarts + "\t" + this.relCount + "\t" + this.detectedGene.getFeatureName() + "\t" + this.offset + "\t" + this.dist2start + "\t" + this.dist2stop + "\t" + this.nextDownstreamFeature + "\t" + this.nextOffset + "\t" + this.sequence + "\t" + 0;

        }
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
}
