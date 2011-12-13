package de.cebitec.vamp.thumbnail;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import java.util.List;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class MultiTrackConnector extends TrackConnector {

    MultiTrackConnector(PersistantTrack track) {
        super(track);
    }

    MultiTrackConnector(List<PersistantTrack> tracks) {
        super(9999, tracks);
    }
}
