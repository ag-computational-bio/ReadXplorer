
package de.cebitec.readxplorer.api.constants;


/**
 * Global GUI constants.
 * <p>
 * This class provides global GUI / visualisation constants.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public final class GUI {


    private GUI() {
    }


    /**
     * Option for setting the maximum zoom level of the Alignment and
     * ReadPairViewer.
     */
    public static final String MAX_ZOOM = "MAX_ZOOM";

    /** Default max zoom level for the Alignment and ReadPairViewer. */
    public static final int DEFAULT_ZOOM = 70;


    /**
     * Property for the height of data viewers.
     */
    public static final String VIEWER_HEIGHT = "VIEWER_HEIGHT";

    /**
     * Medium/default viewer height = "200" pixels.
     */
    public static final int DEFAULT_HEIGHT = 200;

    /**
     * Large viewer height = "250" pixels.
     */
    public static final int MAX_HEIGHT = 250;

    /**
     * Minimum viewer height = "120" pixels.
     */
    public static final int MIN_HEIGHT = 120;


    /**
     * Property for auto scaling of viewers.
     */
    public static final String VIEWER_AUTO_SCALING = "AUTO_SCALING";

    /**
     * Property for showing all reads on fw strand.
     */
    public static final String VIEWER_ALL_FW_STRAND = "VIEWER_ALL_FW_STRAND";

    /**
     * Property for showing all reads on rv strand.
     */
    public static final String VIEWER_ALL_RV_STRAND = "VIEWER_ALL_RV_STRAND";

}
