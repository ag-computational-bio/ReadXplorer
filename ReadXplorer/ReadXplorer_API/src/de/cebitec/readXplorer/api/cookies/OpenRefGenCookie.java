package de.cebitec.readXplorer.api.cookies;

/**
 * Cookie class that signifies the capability of a reference to be opened.
 *
 * Deprecated since introduction of <code>LoginCookie</code> and the
 * possibility to open multiple references.
 *
 * @author jwinneba
 */
@Deprecated
public interface OpenRefGenCookie{

    /**
     * Opens the corresponding reference.
     */
    public void open();

}
