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

package de.cebitec.readxplorer.databackend.dataobjects;


/**
 * Base class for result entries for any kind of analyses carried out on a
 * specific track data set, which need to also store their chromosome id.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class TrackChromResultEntry extends TrackResultEntry {

    private final int chromId;


    /**
     * Base class for result entries for any kind of analyses carried out on a
     * specific track data set, which need to also store their chromosome id.
     * <p>
     * @param trackId The track id of the track to which this result entry
     *                belongs.
     * @param chromId The id of the chromosome, to which this entry belongs.
     */
    public TrackChromResultEntry( int trackId, int chromId ) {
        super( trackId );
        this.chromId = chromId;
    }


    /**
     * @return The id of the chromosome, to which this entry belongs.
     */
    public int getChromId() {
        return chromId;
    }


}
