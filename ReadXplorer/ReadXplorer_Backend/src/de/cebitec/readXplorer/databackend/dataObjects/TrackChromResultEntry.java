package de.cebitec.readXplorer.databackend.dataObjects;

/**
 * Base class for result entries for any kind of analyses carried out on a
 * specific track data set, which need to also store their chromosome id.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class TrackChromResultEntry extends TrackResultEntry {
    
    private final int chromId;

    /**
     * Base class for result entries for any kind of analyses carried out on a
     * specific track data set, which need to also store their chromosome id.
     * @param trackId The track id of the track to which this result entry belongs.
     * @param chromId The id of the chromosome, to which this entry belongs.
     */
    public TrackChromResultEntry(int trackId, int chromId) {
        super(trackId);
        this.chromId = chromId;
    }

    /**
     * @return The id of the chromosome, to which this entry belongs.
     */
    public int getChromId() {
        return chromId;
    }
    
}
