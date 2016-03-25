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

package de.cebitec.readxplorer.databackend.connector;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.io.FileNotFoundException;
import java.util.List;


/**
 * Connector for more than one tracks.
 *
 * @author ddoppmeier, rhilker
 */
public class MultiTrackConnector extends TrackConnector {

    /**
     * A track connector for multiple tracks. It handles all data requests for
     * these tracks.
     * <p>
     * @param track the single initial track for which this connector is created
     * <p>
     * @throws FileNotFoundException
     * @throws DatabaseException     An exception during data queries. It has
     *                               already been logged.
     */
    MultiTrackConnector( PersistentTrack track ) throws FileNotFoundException, DatabaseException {
        super( track );
    }


    /**
     * A multi track connector for a list of tracks. It handles all data
     * requests for these tracks.
     * <p>
     * @param id            id of the track
     * @param tracks        the list of tracks for which this connector is
     *                      created
     * @param combineTracks true, if the data of these tracks is to be combined,
     *                      false if it should be kept separated
     * <p>
     * @throws FileNotFoundException
     * @throws DatabaseException     An exception during data queries. It has
     *                               already been logged.
     */
    MultiTrackConnector( List<PersistentTrack> tracks ) throws FileNotFoundException, DatabaseException {
        super( 9999, tracks, false );
    }


}
