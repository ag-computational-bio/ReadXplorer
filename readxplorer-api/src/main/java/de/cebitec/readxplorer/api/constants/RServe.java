
package de.cebitec.readxplorer.api.constants;


/**
 * Global RServe constants.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public final class RServe {


    private RServe() {
    }


    /**
     * Is Rserve manually configured to connect to a local server with custom
     * startup script.
     */
    public static final String RSERVE_USE_STARTUP_SCRIPT_SETUP = "RSERVE_USE_STARTUP_SCRIPT_SETUP";

    /**
     * Is Rserve manually configured to connect to a remote server.
     */
    public static final String RSERVE_USE_REMOTE_SETUP = "RSERVE_USE_REMOTE_SETUP";
    
    /**
     * Is Rserve manually configured to connect to a remote server.
     */
    public static final String RSERVE_USE_AUTO_SETUP = "RSERVE_USE_AUTO_SETUP";

    /**
     * The RServe host we will connect to for GNU R computations.
     */
    public static final String RSERVE_REMOTE_HOST = "RSERVE_REMOTE_HOST";

    /**
     * The RServe port we will connect to for GNU R computations with via remote
     * connection.
     */
    public static final String RSERVE_REMOTE_PORT = "RSERVE_REMOTE_PORT";

    /**
     * The RServe port we will connect to for GNU R computations with via
     * startup script.
     */
    public static final String RSERVE_STARTUP_SCRIPT_PORT = "RSERVE_STARTUP_SCRIPT_PORT";

    /**
     * Use default Rserve startup script
     */
    public static final String RSERVE_STARTUP_SCRIPT_USE_DEFAULT_SCRIPT = "RSERVE_STARTUP_SCRIPT_USE_DEFAULT_SCRIPT";

    /**
     * Holds the full path to the Rserve startup script if manual local setup is
     * used.
     */
    public static final String RSERVE_STARTUP_SCRIPT_PATH = "RSERVE_STARTUP_SCRIPT_PATH";

    /**
     * Use username and password for authentication.
     */
    public static final String RSERVE_STARTUP_SCRIPT_USE_AUTH = "RSERVE_STARTUP_SCRIPT_USE_AUTH";

    /**
     * The RServe user we will use for the connection.
     */
    public static final String RSERVE_STARTUP_SCRIPT_USER = "RSERVE_STARTUP_SCRIPT_USER";

    /**
     * The RServe password we will use for the connection.
     */
    public static final String RSERVE_STARTUP_SCRIPT_PASSWORD = "RSERVE_STARTUP_SCRIPT_PASSWORD";

    /**
     * Use username and password for authentication.
     */
    public static final String RSERVE_REMOTE_USE_AUTH = "RSERVE_REMOTE_USE_AUTH";

    /**
     * The RServe user we will use for the connection.
     */
    public static final String RSERVE_REMOTE_USER = "RSERVE_REMOTE_USER";

    /**
     * The RServe password we will use for the connection.
     */
    public static final String RSERVE_REMOTE_PASSWORD = "RSERVE_REMOTE_PASSWORD";

}
