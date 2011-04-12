package de.cebitec.vamp.api.cookies;

/**
 * Cookie class that signifies the capability of a track to be closed.
 *
 * @author joern
 */
public interface CloseTrackCookie{

    /**
     * Closes the track.
     *
     * @return true if track could be closed, false otherwise
     */
    public boolean close();

    /**
     * Gets the name of the track the can be closed.
     *
     * @return track name
     */
    public String getName();

}
