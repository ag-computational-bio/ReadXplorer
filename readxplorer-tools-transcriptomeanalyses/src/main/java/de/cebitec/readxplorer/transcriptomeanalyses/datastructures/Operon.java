
package de.cebitec.readxplorer.transcriptomeanalyses.datastructures;


import de.cebitec.readxplorer.databackend.dataObjects.TrackResultEntry;
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
     *                place.
     */
    public Operon( int trackId ) {
        super( trackId );
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
     *               object.
     */
    public void setOperonAdjacencies( List<OperonAdjacency> newOperonAdjacencys ) {
        this.operonAdjacencies = newOperonAdjacencys;
    }


    /**
     * Remove all operon adjacencies associated with this operon object.
     */
    public void clearOperonAdjacencyList() {
        this.operonAdjacencies.removeAll( this.operonAdjacencies );
    }


    /**
     * Adds the operon adjacency to the list of OperonAdjacencies.
     *
     * @param operonAdjacency
     */
    public void addOperonAdjacency( OperonAdjacency operonAdjacency ) {
        this.operonAdjacencies.add( operonAdjacency );
    }


    /**
     * Adds the operon adjacencies to the end of the list of OperonAdjacencies.
     *
     * @param operonAdjacencies
     */
    public void addAllOperonAdjacencies( List<OperonAdjacency> operonAdjacencies ) {
        this.operonAdjacencies.addAll( operonAdjacencies );
    }


    /**
     * Concatinates all locus tags from CDS features in operon.
     *
     * @return a composite name separated by hyphens.
     */
    public String toOperonString() {
        String operon = "";

        if( operonAdjacencies.get( 0 ).getFeature1().getLocus().equals( "BMMGA3_00365" ) ) {
            System.out.println( "" );
        }
        for( Iterator<OperonAdjacency> it = operonAdjacencies.iterator(); it.hasNext(); ) {
            OperonAdjacency operonAdjacency = it.next();

            if( it.hasNext() ) {
                operon += operonAdjacency.getFeature1().getLocus() + "-";
            }
            else {
                operon += operonAdjacency.getFeature1().getLocus()
                          + "-" + operonAdjacency.getFeature2().getLocus();
            }
        }

        return operon;
    }


    /**
     * Returns the direction of this Operon.
     *
     * @return true if forward direction.
     */
    public boolean isFwd() {
        this.isFwd = false;
        if( operonAdjacencies.isEmpty() || operonAdjacencies.get( 0 ).getFeature1() == null ) {
            return isFwd;
        }
        else {
            return isFwd = operonAdjacencies.get( 0 ).getFeature1().isFwdStrand();
        }
    }


    /**
     * Set direction of this operon.
     *
     * @param isFwd <true> if forward.
     */
    public void setFwdDirection( boolean isFwd ) {
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
    public void setIsConsidered( boolean isConsidered ) {
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
        if( isFwd ) {
            return getOperonAdjacencies().get( getOperonAdjacencies().size() - 1 ).getFeature2().getStop();
        }
        else {
            return getOperonAdjacencies().get( 0 ).getFeature1().getStart();
        }
    }


    /**
     * Sets putative transcription start of this opreron.
     *
     * @param startPositionOfTranscript
     */
    public void setStartPositionOfTranscript( int startPositionOfTranscript ) {
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
    public void setMarkedForUpstreamAnalysis( boolean markedForUpstreamAnalysis ) {
        this.markedForUpstreamAnalysis = markedForUpstreamAnalysis;
    }


    /**
     *
     * @return the width of -10 motif
     */
    public int getMinus10MotifWidth() {
        return minus10MotifWidth;
    }


    /**
     * Set -10 motif width.
     *
     * @param minus10MotifWidth
     */
    public void setMinus10MotifWidth( int minus10MotifWidth ) {
        this.minus10MotifWidth = minus10MotifWidth;
    }


    /**
     *
     * @return the width of the -35 motif.
     */
    public int getMinus35MotifWidth() {
        return minus35MotifWidth;
    }


    /**
     * Set -35 motif width.
     *
     * @param minus35MotifWidth
     */
    public void setMinus35MotifWidth( int minus35MotifWidth ) {
        this.minus35MotifWidth = minus35MotifWidth;
    }


    /**
     * Get width of ribosome binding site motif.
     *
     * @return width of ribosome binding site motif
     */
    public int getRbsMotifWidth() {
        return rbsMotifWidth;
    }


    /**
     *
     * @param rbsMotifWidth
     */
    public void setRbsMotifWidth( int rbsMotifWidth ) {
        this.rbsMotifWidth = rbsMotifWidth;
    }


    /**
     *
     * @return
     */
    public boolean isHasRbsFeatureAssigned() {
        return hasRbsFeatureAssigned;
    }


    /**
     *
     * @param hasRbsFeatureAssigned
     */
    public void setRbsFeatureAssigned( boolean hasRbsFeatureAssigned ) {
        this.hasRbsFeatureAssigned = hasRbsFeatureAssigned;
    }


    /**
     *
     * @return
     */
    public boolean isHasPromtorFeaturesAssigned() {
        return hasPromtorFeaturesAssigned;
    }


    /**
     *
     * @param hasPromtorFeaturesAssigned
     */
    public void setHasPromtorFeaturesAssigned( boolean hasPromtorFeaturesAssigned ) {
        this.hasPromtorFeaturesAssigned = hasPromtorFeaturesAssigned;
    }


    /**
     *
     * @return
     */
    public String getAdditionalLocus() {
        return additionalLocus;
    }


    /**
     *
     * @param additionalLocus
     */
    public void setAdditionalLocus( String additionalLocus ) {
        this.additionalLocus = additionalLocus;
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
     * @param promotorSequenceLength
     */
    public void setPromotorSequenceLength( int promotorSequenceLength ) {
        this.promotorSequenceLength = promotorSequenceLength;
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
    public void setRbsSequenceLength( int rbsSequenceLength ) {
        this.rbsSequenceLength = rbsSequenceLength;
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
    public void setStartMinus10Motif( int startMinus10Motif ) {
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
    public void setStartMinus35Motif( int startMinus35Motif ) {
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
    public void setStartRbsMotif( int startRbsMotif ) {
        this.startRbsMotif = startRbsMotif;
    }


    /**
     *
     * @return
     */
    public boolean isFalsPositive() {
        return falsPositive;
    }


    /**
     *
     * @param falsPositive
     */
    public void setFalsPositive( boolean falsPositive ) {
        this.falsPositive = falsPositive;
    }


    /**
     *
     * @param adj
     */
    public void removeAdjaceny( OperonAdjacency adj ) {
        this.operonAdjacencies.remove( adj );
    }


    /**
     *
     * @return
     */
    public ArrayList<Integer> getTsSites() {
        return tsSites;
    }


    /**
     *
     * @param tsSites
     */
    public void setTsSites( ArrayList<Integer> tsSites ) {
        this.tsSites = tsSites;
    }


    /**
     *
     * @return
     */
    public ArrayList<Integer> getUtRegions() {
        return utRegions;
    }


    /**
     *
     * @param utRegions
     */
    public void setUtRegions( ArrayList<Integer> utRegions ) {
        this.utRegions = utRegions;
    }


    /**
     *
     * @return
     */
    public int[] getRbsStartStop() {
        return rbsStartStop;
    }


    /**
     *
     * @param rbsStartStop
     */
    public void setRbsStartStop( int[] rbsStartStop ) {
        this.rbsStartStop = rbsStartStop;
    }


    /**
     *
     * @param tss
     */
    public void addTss( int tss ) {
        this.tsSites.add( tss );
    }


    /**
     *
     * @param start
     * @param stop
     */
    public void addRbs( int start, int stop ) {
        this.rbsStartStop[0] = start;
        this.rbsStartStop[1] = stop;
    }


    /**
     *
     * @param utr
     */
    public void addUtrs( Integer utr ) {
        this.utRegions.add( utr );
    }


    /**
     *
     * @return
     */
    public HashMap<Integer, ArrayList<Integer[]>> getTssToPromotor() {
        return tssToPromotor;
    }


    /**
     *
     * @param tssToPromotor
     */
    public void setTssToPromotor( HashMap<Integer, ArrayList<Integer[]>> tssToPromotor ) {
        this.tssToPromotor = tssToPromotor;
    }


    /**
     *
     * @param tss
     * @param minus35
     * @param minus10
     */
    public void addTssToPromotor( Integer tss, Integer[] minus35, Integer[] minus10 ) {
        List<Integer[]> list = new ArrayList<>();
        list.add( minus35 );
        list.add( minus10 );
        this.tssToPromotor.put( tss, (ArrayList<Integer[]>) list );
    }


    /**
     *
     * @param tss
     *            <p>
     * @return
     */
    public ArrayList<Integer[]> getPromotor( Integer tss ) {
        return this.tssToPromotor.get( tss );
    }


}
