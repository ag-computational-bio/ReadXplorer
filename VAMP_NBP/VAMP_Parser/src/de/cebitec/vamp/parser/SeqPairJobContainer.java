package de.cebitec.vamp.parser;

import de.cebitec.vamp.parser.TrackJob;

/**
 * Container for data necessary for parsing and storing sequence pair (paired end, mate pair) tracks.
 * Contains two TrackJobs, the distance, deviation and orientation of the SeqPairJobContainer.
 *
 * @author Rolf Hilker
 */
public class SeqPairJobContainer {
    
    private TrackJob trackJob1;
    private TrackJob trackJob2;
    private int distance; //distance of the sequences in a sequence pair in bp
    private short deviation; //deviation allowed from that distance in %
    private byte orientation; //0 = fr, 1 = rf, 2 = ff/rr
    
    public SeqPairJobContainer(TrackJob trackJob1, TrackJob trackJob2, int distance, short deviation, byte orientation){
        this.trackJob1 = trackJob1;
        this.trackJob2 = trackJob2;
        this.distance = distance;
        this.deviation = deviation;
        this.orientation = orientation;
    }

    
    public TrackJob getTrackJob1() {
        return trackJob1;
    }

    
    public TrackJob getTrackJob2() {
        return trackJob2;
    }

    
    public int getDistance() {
        return distance;
    }

    
    public short getDeviation() {
        return deviation;
    }

    
    public byte getOrientation() {
        return orientation;
    }
    
    
    
}
