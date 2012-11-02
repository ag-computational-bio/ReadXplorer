package de.cebitec.vamp.util;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

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
     * Checks if the input string is a valid number.
     * @param input input string to check
     * @return <code>true</code> if it is a valid input string, <code>false</code> otherwise
     */
    public static boolean isValidNumberInput(String input) {
        try {
            int tmp = Integer.parseInt(input);
            if (tmp > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Calculates the given time as 3 entries in an array list:
     * 0 = hours, 1 = minutes, 2 = seconds.
     * @param timeInMillis given time in milliseconds
     * @return time as hours, minutes and seconds
     */
    public static ArrayList<Integer> getTime(long timeInMillis) {
        ArrayList<Integer> timeList = new ArrayList<Integer>();
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
}
