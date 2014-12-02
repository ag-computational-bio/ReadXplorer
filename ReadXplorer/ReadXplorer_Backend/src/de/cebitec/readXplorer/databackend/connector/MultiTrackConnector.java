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
package de.cebitec.readXplorer.databackend.connector;

import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Connector for more than one tracks.
 *
 * @author ddoppmeier, rhilker
 */
public class MultiTrackConnector extends TrackConnector {

    MultiTrackConnector(PersistentTrack track) throws FileNotFoundException {
        super(track);
    }

    MultiTrackConnector(List<PersistentTrack> tracks) throws FileNotFoundException {
        super(9999, tracks, false);
    }
}
