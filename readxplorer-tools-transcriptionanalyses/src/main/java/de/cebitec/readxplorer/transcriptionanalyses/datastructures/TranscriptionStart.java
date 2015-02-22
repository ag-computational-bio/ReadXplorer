/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.transcriptionanalyses.datastructures;


import de.cebitec.readxplorer.databackend.dataobjects.TrackChromResultEntry;
import de.cebitec.readxplorer.utils.sequence.Region;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.MIN_VALUE;


/**
 * Data structure for storing a gene start with position, strand, initial
 * coverage
 * (coverage directly before predicted gene start) and start coverage (coverage
 * at predicted gene start).
 *
 * @author -Rolf Hilker-
 */
public class TranscriptionStart extends TrackChromResultEntry {

    private final int pos;
    private final boolean isFwdStrand;
    private final int readStarts;
    private final DetectedFeatures detFeatures;
    private final int percentIncrease;
    private final int coverageIncrease;
    private boolean isPrimaryTss;
    private TranscriptionStart primaryTss;
    private List<TranscriptionStart> mergedTssList;
    private Region startCodon;
    private Region stopCodon;


    /**
     * Data structure for storing a gene start.
     * <p>
     * @param pos              The position at which the gene start was detected
     * @param isFwdStrand      true, if the transcript start was detected on the
     *                         fwd strand, false otherwise.
     * @param readStarts       The number of read starts at the detected tss
     *                         position
     * @param percentIncrease  The coverage increase in percent from the
     *                         position
     *                         before the TSS to the detected TSS position
     * @param coverageIncrease the coverage increase from the position before
     *                         the TSS to the detected TSS position
     * @param detFeatures      object containing the features associated to this
     *                         predicted gene start
     * @param trackId          id of the analyzed track
     * @param chromId          id of the analyzed chromosome
     */
    public TranscriptionStart( int pos, boolean isFwdStrand, int readStarts, int percentIncrease,
                               int coverageIncrease, DetectedFeatures detFeatures, int trackId, int chromId ) {
        super( trackId, chromId );
        this.pos = pos;
        this.isFwdStrand = isFwdStrand;
        this.readStarts = readStarts;
        this.percentIncrease = percentIncrease;
        this.coverageIncrease = coverageIncrease;
        this.detFeatures = detFeatures;
        this.mergedTssList = new ArrayList<>();
    }


    /**
     * @return The position at which the gene start was detected
     */
    public int getPos() {
        return this.pos;
    }


    /**
     * @return The start codon associated to this TSS if there is one.
     *         Otherwise <code>null</code>.
     */
    public Region getStartCodon() {
        return startCodon;
    }


    /**
     * @param startCodon The start codon associated to this TSS if there is one.
     *                   Otherwise <code>null</code>.
     */
    public void setStartCodon( Region startCodon ) {
        this.startCodon = startCodon;
    }


    /**
     * @return <code>true</code> if a start codon is associated to this TSS,
     *         <code>false</code> otherwise.
     */
    public boolean hasStartCodon() {
        return getStartCodon() != null;
    }


    /**
     * @return The stop codon associated to this TSS if there is one. Otherwise
     *         <code>null</code>.
     */
    public Region getStopCodon() {
        return stopCodon;
    }


    /**
     * @param stopCodon The stop codon associated to this TSS if there is one.
     *                  Otherwise <code>null</code>.
     */
    public void setStopCodon( Region stopCodon ) {
        this.stopCodon = stopCodon;
    }


    /**
     * @return <code>true</code> if a stop codon is associated to this TSS,
     *         <code>false</code> otherwise.
     */
    public boolean hasStopCodon() {
        return getStopCodon() != null;
    }


    /**
     * @return Calculates and returns the length of the transcript defined by
     *         the start and stop codon. If one or both of the codons are not
     *         set, the method returns 0.
     */
    public int getCodonTranscriptLength() {
        int length = 0;
        if( startCodon != null && stopCodon != null ) {
            int codonStart = startCodon.getStartOnStrand();
            int codonStop = stopCodon.getStopOnStrand();

            length = Math.abs( codonStart - codonStop );
        }
        return length;
    }


    /**
     * @return Calculates and returns the absolute difference of the associated
     *         start codon start and the tss position (leader sequence). <br/>
     * 0 means both are identical and {@link Integer#MIN_VALUE} means that no
     * start codon is associated to this novel transcript.
     */
    public int getStartPosDifference() {
        int difference = MIN_VALUE;
        if( startCodon != null ) {
            difference = Math.abs( startCodon.getStartOnStrand() - pos );
        }
        return difference;
    }


    /**
     * @return true, if the transcript start was detected on the fwd strand,
     *         false otherwise.
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
     * @return The object containing the features associated to this predicted
     *         gene start.
     */
    public DetectedFeatures getDetFeatures() {
        return this.detFeatures;
    }


    /**
     * @return The coverage increase in percent from the position before the TSS
     *         to the detected TSS position
     */
    public int getPercentIncrease() {
        return this.percentIncrease;
    }


    /**
     * @return The coverage increase from the position before
     *         the TSS to the detected TSS position
     */
    public int getCoverageIncrease() {
        return this.coverageIncrease;
    }

    /**
     * A primary TSS is the most prominent of a gene, while all other ones are
     * secondary TSS.
     * @param isPrimaryTss <code>true</code>, if this is a primary TSS,
     * <code>false</code>, if it is a secondary TSS.
     */
    public void setIsPrimary(boolean isPrimaryTss) {
        this.isPrimaryTss = isPrimaryTss;
    }

    /**
     * A primary TSS is the most prominent of a gene, while all other ones are
     * secondary TSS.
     * @return <code>true</code>, if this is a primary TSS, <code>false</code>,
     * if it is a secondary TSS.
     */
    public boolean isPrimaryTss() {
        return isPrimaryTss;
    }

    /**
     * A primary TSS is the most prominent of a gene, while all other ones are
     * secondary TSS.
     * @param primaryTss If this is a secondary TSS, assign it a primary TSS
     * using this method.
     */
    public void setPrimaryTss(TranscriptionStart primaryTss) {
        this.primaryTss = primaryTss;
    }

    /**
     * A primary TSS is the most prominent of a gene, while all other ones are
     * secondary TSS.
     * @return If this is a secondary TSS, this method returns the associated
     * primary TSS.
     */
    public TranscriptionStart getPrimaryTss() {
        return this.primaryTss;
    }

    /**
     * Merges the <code>mergedTss</code> with the current one. All other merged
     * TSS associated with the <code>mergedTss</code> and within the given
     * <code>bpWindow</code> are added as well.
     * @param mergedTss A TSS merged with this TSS.
     * @param bpWindow The base pair window in which all TSS shall be merged
     */
    public void addAssociatedTss( TranscriptionStart mergedTss, int bpWindow ) {
        for ( TranscriptionStart otherMergedTss : mergedTss.getAssociatedTssList() ) {
            if ( otherMergedTss.getPos() + bpWindow >= pos && !mergedTssList.contains( otherMergedTss ) ) {
                mergedTssList.add(otherMergedTss);
            }
        }
        if ( !mergedTssList.contains( mergedTss ) ) {
            mergedTssList.add(mergedTss);
        }
    }

    /**
     * @return The list of TSS already merged with this TSS.
     */
    public List<TranscriptionStart> getAssociatedTssList() {
        return mergedTssList;
    }

    /**
     * @return The position of the TSS.
     */
    @Override
    public String toString() {
        return String.valueOf(pos);
    }

}
