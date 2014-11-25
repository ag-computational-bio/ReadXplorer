package de.cebitec.vamp.databackend.dataObjects;

/**
 * Base class for result entries for any kind of analyses carried out on a
 * specific track data set.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TrackResultEntry {
    
    private int trackId;

    /**
     * Base class for result entries for any kind of analyses carried out on a
     * specific track data set.
     * @param trackId The track id of the track to which this result entry belongs
     */
    public TrackResultEntry(int trackId) {
        this.trackId = trackId;
    }

    /**
     * @return The track id of the track to which this result entry belongs
     */
    public int getTrackId() {
        return trackId;
    }
}
