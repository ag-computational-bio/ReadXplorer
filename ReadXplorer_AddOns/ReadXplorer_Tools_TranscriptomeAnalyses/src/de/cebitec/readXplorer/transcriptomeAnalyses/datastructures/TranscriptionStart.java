package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;

/**
 * Data structure for storing a gene start with position, strand, initial
 * coverage (coverage directly before predicted gene start) and start coverage
 * (coverage at predicted gene start).
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

    /**
     * Data structure for storing a gene start.
     *
     * @param pos The position at which the gene start was detected
     * @param isFwdStrand true, if the transcript start was detected on the fwd
     * strand, false otherwise.
     * @param readStarts The number of read starts at the detected tss position
     * @param percentIncrease The coverage at the detected gene start position
     * (getStartPosition()).
     * @param coverageIncrease the coverage increase from the position before
     * the TSS to the detected TSS position
     * @param detFeatures object containing the features associated to this
     * predicted gene start
     */
    public TranscriptionStart(int pos, boolean isFwdStrand, int readStarts, 
            DetectedFeatures detFeatures, int trackId, int chromId) {
        super(trackId, chromId);
        this.startPosition = pos;
        this.isFwdStrand = isFwdStrand;
        this.readStarts = readStarts;
    }

    /**
     * Data structure for storing a gene start.
     *
     * @param tssStartPosition The position at which the gene start was detected
     * @param isFwdStrand true, if the transcript start was detected on the fwd
     * strand, false otherwise.
     * @param readStarts The number of read starts at the detected tss position
     * @param relCount
     * @param detectedGene
     * @param offset
     * @param dist2start
     * @param dist2stop
     * @param nextDownstreamGene 
     * @param offsetToNextDownstreamGene
     * @param promotorSequence
     * @param trackId
     * @param chromId 
     */
    public TranscriptionStart(int pos, boolean isFwdStrand, int readStarts, double relCount, PersistantFeature detectedGene, int offset, int dist2start, int dist2stop, PersistantFeature nextDownstreamGene, 
            int offsetToNextDownstreamGene, String promotorSequence, boolean leaderless, boolean cdsShift, 
            String detectedFeatStart, String detectedFeatStop, boolean isInternal, boolean putAS, int trackId, int chromId) {
        super(trackId, chromId);
        this.startPosition = pos;
        this.isFwdStrand = isFwdStrand;
        this.readStarts = readStarts;
        this.relCount = relCount;
        this.detectedGene = detectedGene;
        this.offset = offset;
        this.dist2start = dist2start;
        this.dist2stop = dist2stop;
        this.nextDownstreamFeature = nextDownstreamGene;
        this.nextOffset = offsetToNextDownstreamGene;
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

    public boolean isIsFwdStrand() {
        return isFwdStrand;
    }

    public void setIsFwdStrand(boolean isFwdStrand) {
        this.isFwdStrand = isFwdStrand;
    }

    public int getReadStarts() {
        return readStarts;
    }

    public boolean isPutativeAntisense() {
        return putativeAntisense;
    }

    public void setPutativeAntisense(boolean putativeAntisense) {
        this.putativeAntisense = putativeAntisense;
    }

    
    public void setReadStarts(int readStarts) {
        this.readStarts = readStarts;
    }

    public double getRelCount() {
        return relCount;
    }

    public void setRelCount(double relCount) {
        this.relCount = relCount;
    }

    public boolean isLeaderless() {
        return leaderless;
    }

    public boolean isCdsShift() {
        return cdsShift;
    }

    public PersistantFeature getDetectedGene() {
        return detectedGene;
    }

    public void setDetectedGene(PersistantFeature detectedGene) {
        this.detectedGene = detectedGene;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getDist2start() {
        return dist2start;
    }

    public void setDist2start(int dist2start) {
        this.dist2start = dist2start;
    }

    public int getDist2stop() {
        return dist2stop;
    }

    public void setDist2stop(int dist2stop) {
        this.dist2stop = dist2stop;
    }

    public PersistantFeature getNextGene() {
        return nextDownstreamFeature;
    }

    public void setNextGene(PersistantFeature nextGene) {
        this.nextDownstreamFeature = nextGene;
    }

    public int getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getDetectedFeatStart() {
        return detectedFeatStart;
    }

    public String getDetectedFeatStop() {
        return detectedFeatStop;
    }

    public boolean isInternalTSS() {
        return internalTSS;
    }
    
    

    @Override
    public String toString() {

        if (isFwdStrand) {
            return this.startPosition + "\t" + "fwd\t" + this.readStarts + "\t" + this.relCount + "\t" + this.detectedGene.getFeatureName() + "\t" + this.offset + "\t" + this.dist2start + "\t" + this.dist2stop + "\t" + this.nextDownstreamFeature + "\t" + this.nextOffset + "\t" + this.sequence + "\t" + 0;

        } else {
            return this.startPosition + "\t" + "rev\t" + this.readStarts + "\t" + this.relCount + "\t" + this.detectedGene.getFeatureName() + "\t" + this.offset + "\t" + this.dist2start + "\t" + this.dist2stop + "\t" + this.nextDownstreamFeature + "\t" + this.nextOffset + "\t" + this.sequence + "\t" + 0;

        }
    }
}
