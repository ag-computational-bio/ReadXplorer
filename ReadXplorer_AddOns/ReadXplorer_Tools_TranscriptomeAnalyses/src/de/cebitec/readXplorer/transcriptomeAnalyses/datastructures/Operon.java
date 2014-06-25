package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.TrackResultEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author MKD, rhilker, edit by jritter
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
    private boolean hasRbsFeatureAssigned, hasPromtorFeaturesAssigned, falsPositive;
    private String additionalLocus;
    private int promotorSequenceLength, rbsSequenceLength;
    private int startMinus10Motif, startMinus35Motif, startRbsMotif;
    private ArrayList<Integer> tsSites;
    private ArrayList<Integer> utRegions;
    private int[] rbsStartStop;
    private HashMap<Integer, ArrayList<Integer[]>> tssToPromotor;

    /**
     *
     * @param trackId The track ID of the track on which the analysis has taken
     * place.
     */
    public Operon(int trackId) {
        super(trackId);
        this.operonAdjacencies = new CopyOnWriteArrayList<>();
        this.tsSites = new ArrayList<>();
        this.utRegions = new ArrayList<>();
        this.rbsStartStop = new int[2];
        this.tssToPromotor = new HashMap<>();
    }

    /**
     * Returns the number of genes the operon consists of.
     *
     * @return the number of genes
     */
    public int getNbOfGenes() {
        return this.operonAdjacencies.size() + 1;
    }

    /**
     * @return the operon adjacencies
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

        if (operonAdjacencies.get(0).getFeature1().getLocus().equals("BMMGA3_00365")) {
            System.out.println("");
        }
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
     * Set direction of this operon.
     *
     * @param isFwd <true> if forward.
     */
    public void setFwdDirection(boolean isFwd) {
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

    /**
     * Returns putative transcription start of this opreron if known, else the
     * start position of first gene in detected operon.
     *
     * @return putative transcription start of this opreron
     */
    public int getStartPositionOfOperonTranscript() {
        return startPositionOfTranscript;
    }

    public int getStopPositionOfOperonTranscript() {
        if (isFwd) {
            return getOperonAdjacencies().get(getOperonAdjacencies().size() - 1).getFeature2().getStop();
        } else {
            return getOperonAdjacencies().get(0).getFeature1().getStart();
        }
    }

    /**
     * Sets putative transcription start of this opreron.
     *
     * @param startPositionOfTranscript
     */
    public void setStartPositionOfTranscript(int startPositionOfTranscript) {
        this.startPositionOfTranscript = startPositionOfTranscript;
    }

    /**
     *
     * @return <true> if was marked for upstream analyses else <false>
     */
    public boolean isMarkedForUpstreamAnalysis() {
        return markedForUpstreamAnalysis;
    }

    /**
     *
     * @param markedForUpstreamAnalysis
     */
    public void setMarkedForUpstreamAnalysis(boolean markedForUpstreamAnalysis) {
        this.markedForUpstreamAnalysis = markedForUpstreamAnalysis;
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

    public boolean isFalsPositive() {
        return falsPositive;
    }

    public void setFalsPositive(boolean falsPositive) {
        this.falsPositive = falsPositive;
    }

    public void removeAdjaceny(OperonAdjacency adj) {
        this.operonAdjacencies.remove(adj);
    }

    public ArrayList<Integer> getTsSites() {
        return tsSites;
    }

    public void setTsSites(ArrayList<Integer> tsSites) {
        this.tsSites = tsSites;
    }

    public ArrayList<Integer> getUtRegions() {
        return utRegions;
    }

    public void setUtRegions(ArrayList<Integer> utRegions) {
        this.utRegions = utRegions;
    }

    public int[] getRbsStartStop() {
        return rbsStartStop;
    }

    public void setRbsStartStop(int[] rbsStartStop) {
        this.rbsStartStop = rbsStartStop;
    }

    public void addTss(int tss) {
        this.tsSites.add(tss);
    }

    public void addRbs(int start, int stop) {
        this.rbsStartStop[0] = start;
        this.rbsStartStop[1] = stop;
    }

    public void addUtrs(Integer utr) {
        this.utRegions.add(utr);
    }

    public HashMap<Integer, ArrayList<Integer[]>> getTssToPromotor() {
        return tssToPromotor;
    }

    public void setTssToPromotor(HashMap<Integer, ArrayList<Integer[]>> tssToPromotor) {
        this.tssToPromotor = tssToPromotor;
    }

    public void addTssToPromotor(Integer tss, Integer[] minus35, Integer[] minus10) {
        List<Integer[]> list = new ArrayList<>();
        list.add(minus35);
        list.add(minus10);
        this.tssToPromotor.put(tss, (ArrayList<Integer[]>) list);
    }

    public ArrayList<Integer[]> getPromotor(Integer tss) {
        return this.tssToPromotor.get(tss);
    }
}
