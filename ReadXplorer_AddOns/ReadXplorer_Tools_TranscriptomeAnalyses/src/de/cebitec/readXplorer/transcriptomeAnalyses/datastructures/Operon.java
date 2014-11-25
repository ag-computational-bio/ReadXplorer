package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.TrackResultEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author MKD, rhilker
 *
 * Data structure for storing operons. Operons consist of a list of
 * OperonAdjacencies, since each operon can contain more than two genes.
 */
public class Operon extends TrackResultEntry {

    private List<OperonAdjacency> operonAdjacencies;
    private boolean isFwd;
    private boolean isConsidered;
    private int startPositionOfTranscript;
    private boolean markedForUpstreamAnalysis;
    private int minus10MotifWidth, minus35MotifWidth, rbsMotifWidth;
    private boolean hasRbsFeatureAssigned, hasPromtorFeaturesAssigned;
    private String additionalLocus;
    private int promotorSequenceLength, rbsSequenceLength;
    private int startMinus10Motif, startMinus35Motif, startRbsMotif;

    /**
     *
     * @param trackId
     */
    public Operon(int trackId) {
        super(trackId);
        this.operonAdjacencies = new ArrayList<>();
    }

    /**
     * @return the operon adjacencies of this operon
     */
    public List<OperonAdjacency> getOperonAdjacencies() {
        return this.operonAdjacencies;
    }

    /**
     * @param operon the operon adjacencies to associate with this operon
     * object.
     */
    public void setOperonAdjacencies(List<OperonAdjacency> newOperonAdjacencys) {
        this.operonAdjacencies = newOperonAdjacencys;
    }

    /**
     * Remove all operon adjacencies associated with this operon object.
     */
    public void clearOperonAdjacencyList() {
        this.operonAdjacencies.removeAll(this.operonAdjacencies);
    }

    /**
     * Adds the operon adjacency to the list of OperonAdjacencies.
     *
     * @param operonAdjacency
     */
    public void addOperonAdjacency(OperonAdjacency operonAdjacency) {
        this.operonAdjacencies.add(operonAdjacency);
    }

    /**
     * Adds the operon adjacencies to the end of the list of OperonAdjacencies.
     *
     * @param operonAdjacencies
     */
    public void addAllOperonAdjacencies(List<OperonAdjacency> operonAdjacencies) {
        this.operonAdjacencies.addAll(operonAdjacencies);
    }

    /**
     * Concatinates all locus tags from CDS features in operon.
     *
     * @return a composite name separated by hyphens.
     */
    public String toOperonString() {
        String operon = "";

        for (Iterator<OperonAdjacency> it = operonAdjacencies.iterator(); it.hasNext();) {
            OperonAdjacency operonAdjacency = it.next();

            if (it.hasNext()) {
                operon += operonAdjacency.getFeature1().getLocus() + "-";
            } else {
                operon += operonAdjacency.getFeature1().getLocus()
                        + "-" + operonAdjacency.getFeature2().getLocus();
            }
        }

        return operon;
    }

    /**
     * Returns the direction of this Operon.
     *
     * @return <true> if forward direction.
     */
    public boolean isFwd() {
        this.isFwd = false;
        if (operonAdjacencies.isEmpty()) {
            return isFwd;
        } else {
            return isFwd = operonAdjacencies.get(0).getFeature1().isFwdStrand();
        }
    }

    /**
     * Set Direction of Operon.
     *
     * @param isFwd <true> if forward.
     */
    public void setFwd(boolean isFwd) {
        this.isFwd = isFwd;
    }

    /**
     * Return <true> if this operon was finally considered during analysis.
     *
     * @return <true> if marked as considered.
     */
    public boolean isConsidered() {
        return isConsidered;
    }

    /**
     * Set <true> if operon was considered during analysis process.
     *
     * @param isConsidered <true> if considered.
     */
    public void setIsConsidered(boolean isConsidered) {
        this.isConsidered = isConsidered;
    }

    public int getStartPositionOfTranscript() {
        return startPositionOfTranscript;
    }

    public void setStartPositionOfTranscript(int startPositionOfTranscript) {
        this.startPositionOfTranscript = startPositionOfTranscript;
    }

    public boolean isForUpstreamAnalysisMarked() {
        return markedForUpstreamAnalysis;
    }

    public void setForUpstreamAnalysisMarked(boolean forUpstreamAnalysisMarked) {
        this.markedForUpstreamAnalysis = forUpstreamAnalysisMarked;
    }

    public boolean isIsFwd() {
        return isFwd;
    }

    public void setIsFwd(boolean isFwd) {
        this.isFwd = isFwd;
    }

    public int getMinus10MotifWidth() {
        return minus10MotifWidth;
    }

    public void setMinus10MotifWidth(int minus10MotifWidth) {
        this.minus10MotifWidth = minus10MotifWidth;
    }

    public int getMinus35MotifWidth() {
        return minus35MotifWidth;
    }

    public void setMinus35MotifWidth(int minus35MotifWidth) {
        this.minus35MotifWidth = minus35MotifWidth;
    }

    public int getRbsMotifWidth() {
        return rbsMotifWidth;
    }

    public void setRbsMotifWidth(int rbsMotifWidth) {
        this.rbsMotifWidth = rbsMotifWidth;
    }

    public boolean isHasRbsFeatureAssigned() {
        return hasRbsFeatureAssigned;
    }

    public void setRbsFeatureAssigned(boolean hasRbsFeatureAssigned) {
        this.hasRbsFeatureAssigned = hasRbsFeatureAssigned;
    }

    public boolean isHasPromtorFeaturesAssigned() {
        return hasPromtorFeaturesAssigned;
    }

    public void setHasPromtorFeaturesAssigned(boolean hasPromtorFeaturesAssigned) {
        this.hasPromtorFeaturesAssigned = hasPromtorFeaturesAssigned;
    }

    public String getAdditionalLocus() {
        return additionalLocus;
    }

    public void setAdditionalLocus(String additionalLocus) {
        this.additionalLocus = additionalLocus;
    }

    public int getPromotorSequenceLength() {
        return promotorSequenceLength;
    }

    public void setPromotorSequenceLength(int promotorSequenceLength) {
        this.promotorSequenceLength = promotorSequenceLength;
    }

    public int getRbsSequenceLength() {
        return rbsSequenceLength;
    }

    public void setRbsSequenceLength(int rbsSequenceLength) {
        this.rbsSequenceLength = rbsSequenceLength;
    }

    public int getStartMinus10Motif() {
        return startMinus10Motif;
    }

    public void setStartMinus10Motif(int startMinus10Motif) {
        this.startMinus10Motif = startMinus10Motif;
    }

    public int getStartMinus35Motif() {
        return startMinus35Motif;
    }

    public void setStartMinus35Motif(int startMinus35Motif) {
        this.startMinus35Motif = startMinus35Motif;
    }

    public int getStartRbsMotif() {
        return startRbsMotif;
    }

    public void setStartRbsMotif(int startRbsMotif) {
        this.startRbsMotif = startRbsMotif;
    }
}
