package de.cebitec.vamp.util;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Contains general use utilities.
 * 
 * @author -Rolf Hilker-
 */
public class GeneralUtils {

    /**
     * Calculates the percentage increase of value 1 to value 2. In case value1 is 0, 
     * the percentage is set to 1.5 times the absolute difference as a weight factor.
     * @param value1 smaller value
     * @param value2 larger value
     * @return the percentage increase 
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

    /**
     * @param parent the parent component
     * @return Any text found in the clipboard. If none is found, an empty
     * String is returned.
     */
    public static String getClipboardContents(Component parent) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String result = "";
        Transferable contents = clipboard.getContents(null);
        final boolean hasTransferableText = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                JOptionPane.showMessageDialog(parent, "Unsupported DataFlavor for clipboard copying.", "Paste Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "IOException occured during recovering of text from clipboard.", "Paste Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }
    
    /**
     * Checks if the input string is a valid number larger than 0.
     * @param input input string to check
     * @return <code>true</code> if it is a valid input string, <code>false</code> otherwise
     */
    public static boolean isValidPositiveNumberInput(String input) {
        try {
            return Integer.parseInt(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Checks if the input string is a valid number larger than or equal to 0.
     * @param input input string to check
     * @return <code>true</code> if it is a valid input
     * string, <code>false</code> otherwise
     */
    public static boolean isValidNumberInput(String input) {
        try {
            return Integer.parseInt(input) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Checks if the input string is a valid number between 1 and 100, so a valid
     * percentage value.
     * @param input input string to check
     * @return <code>true</code> if it is a valid percentage value, <code>false</code> otherwise
     */
    public static boolean isValidPercentage(String input) {
        if (GeneralUtils.isValidPositiveNumberInput(input)) {
            int value = Integer.valueOf(input);
            if (value <= 100) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculates the given time as 3 entries in an array list:
     * 0 = hours, 1 = minutes, 2 = seconds.
     * @param timeInMillis given time in milliseconds
     * @return time as hours, minutes and seconds
     */
    public static ArrayList<Integer> getTime(long timeInMillis) {
        ArrayList<Integer> timeList = new ArrayList<>();
        int remdr = (int) (timeInMillis % (24L * 60 * 60 * 1000));

        final int hours = remdr / (60 * 60 * 1000);

        remdr %= 60 * 60 * 1000;

        final int minutes = remdr / (60 * 1000);

        remdr %= 60 * 1000;

        final int seconds = remdr / 1000;
        timeList.add(0, hours);
        timeList.add(1, minutes);
        timeList.add(2, seconds);

        return timeList;
    }
    
    /**
     * Generates a string, which concatenates the map of strings for user friendly
     * displaying in the gui with an " and ".
     * @param stringMap the map of strings, which should be concatenated
     * @return the string containing all strings concatenated with "and"
     */
    public static String generateConcatenatedString(Map<Integer, String> stringMap) {
        StringBuilder concatString = new StringBuilder();
        Iterator<Integer> strings = stringMap.keySet().iterator();
        while (strings.hasNext()) {
            concatString = concatString.append(stringMap.get(strings.next())).append(" and ");
        }
        if (concatString.length() > 5) {
            concatString = concatString.delete(concatString.length() - 5, concatString.length());
        }
        return concatString.toString();
    }
}
