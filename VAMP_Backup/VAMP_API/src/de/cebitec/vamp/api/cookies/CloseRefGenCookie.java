package de.cebitec.vamp.api.cookies;

/**
 * Cookie class that signifies the capability of a reference to be closed.
 *
 * @author jwinneba
 */
public interface CloseRefGenCookie{

    /**
     * Closes the reference.
     *
     * @return true if reference could be closed, false otherwise
     */
    public boolean close();

}
