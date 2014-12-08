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

package de.cebitec.readXplorer.transcriptionAnalyses.dataStructures;


import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;


/**
 * Data structure for storing a gene start with position, strand, initial
 * coverage
 * (coverage directly before predicted gene start) and start coverage (coverage
 * at predicted gene start).
 *
 * @author -Rolf Hilker-
 */
public class TranscriptionStart extends TrackChromResultEntry {

    private int pos;
    private boolean isFwdStrand;
    private int readStarts;
    private DetectedFeatures detFeatures;
    private int percentIncrease;
    private int coverageIncrease;


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
    }


    /**
     * @return The position at which the gene start was detected
     */
    public int getPos() {
        return this.pos;
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


}
