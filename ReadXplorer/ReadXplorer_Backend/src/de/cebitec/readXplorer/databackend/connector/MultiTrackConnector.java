package de.cebitec.readXplorer.databackend.connector;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Connector for more than one tracks.
 *
 * @author ddoppmeier, rhilker
 */
public class MultiTrackConnector extends TrackConnector {

    MultiTrackConnector(PersistantTrack track, String adapter) throws FileNotFoundException {
        super(track, adapter);
    }

    MultiTrackConnector(List<PersistantTrack> tracks, String adapter) throws FileNotFoundException {
        super(9999, tracks, adapter, false);
    }
}
