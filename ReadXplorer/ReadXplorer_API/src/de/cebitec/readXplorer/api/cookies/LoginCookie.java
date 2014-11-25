package de.cebitec.readXplorer.api.cookies;

/**
 * Signals that the user is logged in.
 *
 * @author jwinneba
 */
public interface LoginCookie {

    /**
     * Never used, but nice to know you are logged in, isn't it?
     *
     * @return login status
     */
    public boolean isLoggedIn();

}
