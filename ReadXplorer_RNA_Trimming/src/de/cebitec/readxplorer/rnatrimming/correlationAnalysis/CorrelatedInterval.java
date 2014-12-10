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

package de.cebitec.readxplorer.rnatrimming.correlationAnalysis;


import de.cebitec.readxplorer.databackend.dataObjects.TrackChromResultEntry;


/**
 * CorrelatedInterval is a data class, that saves the data about a correlation
 * between the data of two track in a defined interval
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class CorrelatedInterval extends TrackChromResultEntry {

    private byte strand;

    private int from;
    private int to;
    private int track2Id;
    private double correlation;
    private double minPeakCoverage;


    /**
     * CorrelatedInterval is a data class, that saves the data about a
     * correlation between the data of two tracks in a defined interval.
     * <p>
     * @param strand
     * @param track1Id
     * @param chromId
     * @param from
     * @param to
     * @param correlation
     * @param minPeakCoverage
     */
    public CorrelatedInterval( byte strand,
                               int track1Id, int track2Id, int chromId, int from, int to, double correlation, double minPeakCoverage ) {
        super( track1Id, chromId );
        this.strand = strand;
        this.from = from;
        this.to = to;
        this.correlation = correlation;
        this.minPeakCoverage = minPeakCoverage;
    }


    /**
     * @return the strand
     */
    public byte getDirection() {
        return strand;
    }


    /**
     * @param strand the strand to set
     */
    public void setDirection( byte strand ) {
        this.strand = strand;
    }


    /**
     * @return the from
     */
    public int getFrom() {
        return from;
    }


    /**
     * @param from the from to set
     */
    public void setFrom( int from ) {
        this.from = from;
    }


    /**
     * @return the to
     */
    public int getTo() {
        return to;
    }


    /**
     * @param to the to to set
     */
    public void setTo( int to ) {
        this.to = to;
    }


    /**
     * @return the correlation
     */
    public double getCorrelation() {
        return correlation;
    }


    /**
     * @param correlation the correlation to set
     */
    public void setCorrelation( double correlation ) {
        this.correlation = correlation;
    }


    /**
     * @return the minPeakCoverage
     */
    public double getMinPeakCoverage() {
        return minPeakCoverage;
    }


    /**
     * @param minPeakCoverage the minPeakCoverage to set
     */
    public void setMinPeakCoverage( double minPeakCoverage ) {
        this.minPeakCoverage = minPeakCoverage;
    }


    public int getTrack2Id() {
        return track2Id;
    }


}
