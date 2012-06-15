package de.cebitec.vamp.databackend.connector;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.List;

/**
 * Connector for more than one tracks.
 *
 * @author ddoppmeier, rhilker
 */
public class MultiTrackConnector extends TrackConnector {

    MultiTrackConnector(PersistantTrack track, String adapter) {
        super(track, adapter);
    }

    MultiTrackConnector(List<PersistantTrack> tracks, String adapter) {
        super(9999, tracks, adapter, false);
    }
}
