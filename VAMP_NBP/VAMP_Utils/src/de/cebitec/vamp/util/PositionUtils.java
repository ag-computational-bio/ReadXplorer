package de.cebitec.vamp.util;

/**
 * @author rhilker
 * 
 * Designed for methods handling any kind of position specific functionality.
 */
public class PositionUtils {

    /**
     * Converts a position string to the corresponding integer position.
     * @param posString position as string, which might include a '_' 
     * @return corresponding position value as integer
     */
    public static int convertPosition(String posString) {
        if (posString.contains("_")) {
            posString = posString.substring(0, posString.length() - 2);
        }
        return Integer.parseInt(posString);
    }
}
