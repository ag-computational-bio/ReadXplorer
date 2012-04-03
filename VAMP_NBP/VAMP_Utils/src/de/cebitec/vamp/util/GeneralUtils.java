package de.cebitec.vamp.util;

/**
 * @author -Rolf Hilker-
 * 
 * Contains general use utilities.
 */
public class GeneralUtils {

    /**
     * Calculates the percentage increase of value 1 to value 2. In case value1 is 0, 
     * the percentage is set to 1.5 times the absolute difference as a weight factor.
     * @param value1 smaller value
     * @param value2 larger value
     */
    public static int calculatePercentageIncrease(int value1, int value2) {
        int percentDiff;
        if (value1 == 0) {
            int absoluteDiff = value2 - value1;
            percentDiff = (int) (absoluteDiff * 1.5); //weight factor
        } else {
            percentDiff = (int) (((double) value2 / (double) value1) * 100.0) - 100;
        }
        return percentDiff;
    }
    
}
