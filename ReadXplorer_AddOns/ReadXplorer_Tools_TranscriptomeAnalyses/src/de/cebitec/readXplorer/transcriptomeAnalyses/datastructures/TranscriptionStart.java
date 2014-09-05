package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;
import de.cebitec.readXplorer.util.classification.FeatureType;

/**
 * Data structure for storing a transcription start site.
 *
 * @author -Rolf Hilker-, modified by -jritter-
 */
public class TranscriptionStart extends TrackChromResultEntry {

    private final int startPosition;
    private final boolean isFwdStrand;
    private int readStarts;
    private double relCount;
    private PersistentFeature detectedGene;
    private int offset;
    private int dist2start, dist2stop;
    private PersistentFeature nextDownstreamFeature;
    private int offsetToNextDownstrFeature;
    private boolean leaderless, cdsShift;
    private String detectedFeatStart, detectedFeatStop;
    private boolean intragenicTSS, intergenicTSS;
    private boolean putativeAntisense;
    private boolean selected;
    private boolean falsePositive;
    private String additionalIdentyfier = null;
    private int promotorSequenceLength, rbsSequenceLength;
    private int startMinus10Motif, startMinus35Motif, startRbsMotif;
    private int minus10MotifWidth, minus35MotifWidth, rbsMotifWidth;
    private boolean hasRbsFeatureAssigned, hasPromtorFeaturesAssigned;
    private boolean isConsideredTSS;
    private boolean isIntragenicAntisense;
    private FeatureType assignedFeatureType;
    private boolean is3PrimeUtrAntisense, is5PrimeUtrAntisense, assignedToStableRNA;
    private String comment;

    /**
     * Data structure for storing a gene start.
     *
     * @param tssStartPosition The position at which the gene start was detected
     * @param isFwdStrand <true>, if the transcript start was detected on the
     * fwd strand, <false> otherwise.
     * @param readStarts The number of read starts at the detected tss position
     * @param relCount readstarts/mappings per million
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
     * @param leaderless <true> if transcript is leaderless else <false>
     * @param cdsShift <true> if putative CDS-shift was detected else <false>
     * @param isInternal internal location
     * @param putAS putative antisense
     * @param trackId Track ID.
     * @param chromId Chromosome ID.
     *
     */
    public TranscriptionStart(int tssStartPosition, boolean isFwdStrand, int readStarts,
            double relCount, PersistentFeature detectedGene, int offset, int dist2start,
            int dist2stop, PersistentFeature nextDownstreamFeature,
            int offsetToNextDownstreamFeature, boolean leaderless, boolean cdsShift,
            boolean isInternal, boolean putAS, int chromId, int trackId) {
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
        this.offsetToNextDownstrFeature = offsetToNextDownstreamFeature;
        this.leaderless = leaderless;
        this.cdsShift = cdsShift;
        this.intragenicTSS = isInternal;
        this.putativeAntisense = putAS;
    }

    public TranscriptionStart(int tssStartPosition, boolean isFwdStrand, int readStarts,
            double relCount, PersistentFeature detectedGene, int offset, int dist2start,
            int dist2stop, int offsetToNextDownstreamFeature, boolean leaderless, boolean cdsShift,
            boolean isInternal, boolean putAS, int chromId, int trackId) {
        super(trackId, chromId);
        this.startPosition = tssStartPosition;
        this.isFwdStrand = isFwdStrand;
        this.readStarts = readStarts;
        this.relCount = relCount;
        this.detectedGene = detectedGene;
        this.offset = offset;
        this.dist2start = dist2start;
        this.dist2stop = dist2stop;
        this.offsetToNextDownstrFeature = offsetToNextDownstreamFeature;
        this.leaderless = leaderless;
        this.cdsShift = cdsShift;
        this.intragenicTSS = isInternal;
        this.putativeAntisense = putAS;
    }

    public TranscriptionStart(int tssStartPosition, boolean isFwdStrand, int readStarts,
            double relCount, int offset, int dist2start,
            int dist2stop, PersistentFeature nextDownstreamFeature, int offsetToNextDownstreamFeature, boolean leaderless, boolean cdsShift,
            boolean isInternal, boolean putAS, int chromId, int trackId) {
        super(trackId, chromId);
        this.startPosition = tssStartPosition;
        this.isFwdStrand = isFwdStrand;
        this.readStarts = readStarts;
        this.relCount = relCount;
        this.offset = offset;
        this.dist2start = dist2start;
        this.dist2stop = dist2stop;
        this.offsetToNextDownstrFeature = offsetToNextDownstreamFeature;
        this.leaderless = leaderless;
        this.cdsShift = cdsShift;
        this.intragenicTSS = isInternal;
        this.putativeAntisense = putAS;
        this.nextDownstreamFeature = nextDownstreamFeature;
    }

    /**
     * Alternative Constructor.
     *
     * @param startPosition The position at which the gene start was detected
     * @param isFwdStrand <true>, if the transcript start was detected on the
     * fwd strand, <false> otherwise.
     * @param chromosomeId Chromosome ID.
     * @param trackId Track ID.
     */
    public TranscriptionStart(int startPosition, boolean isFwdStrand, int chromosomeId, int trackId) {
        super(trackId, chromosomeId);
        this.startPosition = startPosition;
        this.isFwdStrand = isFwdStrand;
    }

    /**
     * @return the position of transcriptions start site.
     */
    public int getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return <true> if the transcript start was detected on the fwd strand,
     * <false> otherwise.
     */
    public boolean isFwdStrand() {
        return this.isFwdStrand;
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

    /**
     *
     * @return <true> if transcript starts intragenic antisense else <false>
     */
    public boolean isIntragenicAntisense() {
        return isIntragenicAntisense;
    }

    /**
     * Set wether this transcript is intragenic antisense or not.
     *
     * @param isIntergenicAntisense <true> if transcript is intragenic antisense
     * else <false>
     */
    public void setIntragenicAntisense(boolean isIntergenicAntisense) {
        this.isIntragenicAntisense = isIntergenicAntisense;
    }

    /**
     * Set whether this transcript is in antisense location to another annotated
     * feature or not.
     *
     * @param putativeAntisense <true> if this TSS is in putative location.
     */
    public void setPutativeAntisense(boolean putativeAntisense) {
        this.putativeAntisense = putativeAntisense;
    }

    /**
     * Set the number of read starts of this transcription start site.
     *
     * @param readStarts the number of read starts
     */
    public void setReadStarts(int readStarts) {
        this.readStarts = readStarts;
    }

    /**
     *
     * @return the relative count of read starts.
     */
    public double getRelCount() {
        return relCount;
    }

    /**
     * Sets the relative count of read starts.
     *
     * @param relCount the relative number of read starts
     */
    public void setRelCount(double relCount) {
        this.relCount = relCount;
    }

    /**
     *
     * @return <true> if transcript is leaderless
     */
    public boolean isLeaderless() {
        return leaderless;
    }

    /**
     *
     * @return <true> if cds shift occur else <false>
     */
    public boolean isCdsShift() {
        return cdsShift;
    }

    /**
     *
     * @param cdsShift
     */
    public void setCdsShift(boolean cdsShift) {
        this.cdsShift = cdsShift;
    }

    /**
     *
     * @return
     */
    public PersistentFeature getDetectedGene() {
        return detectedGene;
    }

    /**
     *
     * @param detectedGene
     */
    public void setDetectedGene(PersistentFeature detectedGene) {
        this.detectedGene = detectedGene;
    }

    /**
     * Returns the distance between transcriptions start site and translation
     * start site, which is the start of an CDS feature.
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
     * Returns the distance from transcription start site and the translation
     * start site of the feauter.
     *
     * @return the distance to start
     */
    public int getDist2start() {
        return dist2start;
    }

    /**
     * Sets the distance from transcription start site to translation start
     * site.
     *
     * @param dist2start the distance to translation start site
     */
    public void setDist2start(int dist2start) {
        this.dist2start = dist2start;
    }

    /**
     *
     * @return the distance to translation stop of the assigned feature
     */
    public int getDist2stop() {
        return dist2stop;
    }

    /**
     *
     * @param dist2stop the distance to stop position of the assigned feature
     */
    public void setDist2stop(int dist2stop) {
        this.dist2stop = dist2stop;
    }

    /**
     *
     * @return
     */
    public PersistentFeature getNextGene() {
        return nextDownstreamFeature;
    }

    /**
     *
     * @param nextGene
     */
    public void setNextGene(PersistentFeature nextGene) {
        this.nextDownstreamFeature = nextGene;
    }

    /**
     *
     * @return
     */
    public int getOffsetToNextDownstrFeature() {
        return offsetToNextDownstrFeature;
    }

    /**
     *
     * @param offsetToNextDownstrFeature
     */
    public void setOffsetToNextDownstrFeature(int offsetToNextDownstrFeature) {
        this.offsetToNextDownstrFeature = offsetToNextDownstrFeature;
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

    public void setDetectedFeatStart(String detectedFeatStart) {
        this.detectedFeatStart = detectedFeatStart;
    }

    public void setDetectedFeatStop(String detectedFeatStop) {
        this.detectedFeatStop = detectedFeatStop;
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
     * @return <true> if transcript starts intragenic else <false>
     */
    public boolean isIntragenicTSS() {
        return intragenicTSS;
    }

    /**
     *
     * @return <true> if is selected for upstream analyses else <false>
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
    public PersistentFeature getAssignedFeature() {
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

    /**
     * 
     * @return 
     */
    public boolean isConsideredTSS() {
        return isConsideredTSS;
    }

    /**
     * 
     * @param isconsideredTSS 
     */
    public void setIsconsideredTSS(boolean isconsideredTSS) {
        this.isConsideredTSS = isconsideredTSS;
    }

    /**
     * 
     * @return 
     */
    public String getComment() {
        return comment;
    }

    /**
     * 
     * @param comment 
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 
     * @return 
     */
    public boolean isFalsePositive() {
        return falsePositive;
    }

    /**
     * 
     * @param falsePositive 
     */
    public void setFalsePositive(boolean falsePositive) {
        this.falsePositive = falsePositive;
    }

    /**
     * 
     * @return 
     */
    public boolean isIntergenicTSS() {
        return intergenicTSS;
    }

    /**
     * 
     * @param intergenicTSS 
     */
    public void setIntergenicTSS(boolean intergenicTSS) {
        this.intergenicTSS = intergenicTSS;
    }

    /**
     * 
     * @param leaderless 
     */
    public void setLeaderless(boolean leaderless) {
        this.leaderless = leaderless;
    }

    /**
     * 
     * @param intragenicTSS 
     */
    public void setIntragenicTSS(boolean intragenicTSS) {
        this.intragenicTSS = intragenicTSS;
    }

    /**
     * 
     * @return 
     */
    public boolean isIs3PrimeUtrAntisense() {
        return is3PrimeUtrAntisense;
    }

    /**
     * 
     * @param is3PrimeUtrAntisense 
     */
    public void setIs3PrimeUtrAntisense(boolean is3PrimeUtrAntisense) {
        this.is3PrimeUtrAntisense = is3PrimeUtrAntisense;
    }

    /**
     * 
     * @return 
     */
    public boolean isIs5PrimeUtrAntisense() {
        return is5PrimeUtrAntisense;
    }

    /**
     * 
     * @param is5PrimeUtrAntisense 
     */
    public void setIs5PrimeUtrAntisense(boolean is5PrimeUtrAntisense) {
        this.is5PrimeUtrAntisense = is5PrimeUtrAntisense;
    }

    /**
     * 
     * @return 
     */
    public FeatureType getAssignedFeatureType() {
        return assignedFeatureType;
    }

    /**
     * 
     * @param assignedFeatureType 
     */
    public void setAssignedFeatureType(FeatureType assignedFeatureType) {
        this.assignedFeatureType = assignedFeatureType;
    }

    /**
     * 
     * @return 
     */
    public boolean isAssignedToStableRNA() {
        return assignedToStableRNA;
    }

    /**
     * 
     * @param assignedToStableRNA 
     */
    public void setAssignedToStableRNA(boolean assignedToStableRNA) {
        this.assignedToStableRNA = assignedToStableRNA;
    }

    /**
     * 
     * @return 
     */
    public int getOffsetToAssignedFeature() {
        if (detectedGene != null) {
            return this.offset;
        } else if (nextDownstreamFeature != null) {
            return this.offsetToNextDownstrFeature;
        } else {
            return 0;
        }
    }
}
