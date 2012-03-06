package de.cebitec.vamp.util;

/**
 * @author -Rolf Hilker-
 * 
 * Contains general use utilities.
 */
public class GeneralUtils {

    /**
     * Calculates the percentage increase of value 1 to value 2. In case the calculated
     * percentage is larger than 1.000.000 and the absolute difference between
     * both values is < 100 the percentage is set to 1.5 times the absolute difference.
     * @param value1 smaller value
     * @param value2 larger value
     */
    public static int calculatePercentageIncrease(int value1, int value2) {
        int percentDiff = (int) (((double) value2 / (double) value1) * 100.0) - 100;
        int absoluteDiff = value2 - value1;

        if (percentDiff > 1000000 && absoluteDiff < 100) {
            percentDiff = (int) (absoluteDiff * 1.5); //weight factor
        }
        return percentDiff;
    }
    
}
