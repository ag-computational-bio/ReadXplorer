
package de.cebitec.readxplorer.api.constants;


/**
 * Global RServe constants.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public final class RServe {


    private RServe() {}


    /**
     * The RServe host we will connect to for GNU R computations.
     */
    public static final String RSERVE_HOST = "RSERVE_HOST";

    /**
     * Is Rserve manually configured to connect to a local server with custom
     * startup script.
     */
    public static final String RSERVE_MANUAL_LOCAL_SETUP = "RSERVE_MANUAL_LOCAL_SETUP";

    /**
     * Is Rserve manually configured to connect to a remote server.
     */
    public static final String RSERVE_MANUAL_REMOTE_SETUP = "RSERVE_MANUAL_REMOTE_SETUP";

    /**
     * The RServe password we will use for the connection.
     */
    public static final String RSERVE_PASSWORD = "RSERVE_PASSWORD";

    /**
     * The RServe port we will connect to for GNU R computations.
     */
    public static final String RSERVE_PORT = "RSERVE_PORT";

    /**
     * Holds the full path to the Rserve startup script if manual local setup is
     * used.
     */
    public static final String RSERVE_STARTUP_SCRIPT = "RSERVE_STARTUP_SCRIPT";

    /**
     * The RServe user we will use for the connection.
     */
    public static final String RSERVE_USER = "RSERVE_USER";

    /**
     * Use username and password for authentication.
     */
    public static final String RSERVE_USE_AUTH = "RSERVE_USE_AUTH";

}
