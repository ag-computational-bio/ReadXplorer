package de.cebitec.readXplorer.correlationAnalysis;

import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;

/**
 * CorrelatedInterval is a data class, that saves the data about a correlation 
 * between the data of two track in a defined interval
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class CorrelatedInterval extends TrackChromResultEntry {
    private CorrelationAnalysisProcessor.StrandDirection direction;
    
    private int from; 
    private int to;
    private int track2Id;
    private double correlation;
    private double minPeakCoverage;
    
    /**
     * CorrelatedInterval is a data class, that saves the data about a
     * correlation between the data of two tracks in a defined interval.
     * @param direction
     * @param track1Id
     * @param chromId
     * @param from
     * @param to
     * @param correlation
     * @param minPeakCoverage 
     */
    public CorrelatedInterval(CorrelationAnalysisProcessor.StrandDirection direction,
            int track1Id, int track2Id, int chromId, int from, int to, double correlation, double minPeakCoverage) {
        super(track1Id, chromId);
        this.direction = direction;
        this.from = from;
        this.to = to;
        this.correlation = correlation;
        this.minPeakCoverage = minPeakCoverage;
    }

    /**
     * @return the direction
     */
    public CorrelationAnalysisProcessor.StrandDirection getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(CorrelationAnalysisProcessor.StrandDirection direction) {
        this.direction = direction;
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
    public void setFrom(int from) {
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
    public void setTo(int to) {
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
    public void setCorrelation(double correlation) {
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
    public void setMinPeakCoverage(double minPeakCoverage) {
        this.minPeakCoverage = minPeakCoverage;
    }

    public int getTrack2Id() {
        return track2Id;
    }
}
