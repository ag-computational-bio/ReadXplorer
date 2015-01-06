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

package de.cebitec.readxplorer.parser;


/**
 * Container for data necessary for parsing and storing sequence pair (paired
 * end, mate pair) tracks.
 * Contains two TrackJobs, the distance, deviation and orientation of the
 * ReadPairJobContainer.
 *
 * @author Rolf Hilker
 */
public class ReadPairJobContainer {

    private final TrackJob trackJob1;
    private final TrackJob trackJob2;
    private final int distance; //distance of the sequences in a sequence pair in bp
    private final short deviation; //deviation allowed from that distance in %
    private final byte orientation; //0 = fr, 1 = rf, 2 = ff/rr


    public ReadPairJobContainer( TrackJob trackJob1, TrackJob trackJob2, int distance, short deviation, byte orientation ) {
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
