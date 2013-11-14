package de.cebitec.readXplorer.util;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openide.util.NbBundle;

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
            percentDiff = (int) Math.ceil(((double) value2 / (double) value1) * 100.0) - 100;
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
     * Generates a string, which concatenates the list of strings for user friendly
     * displaying in the gui with an " and ".
     * @param strings the list of strings, which should be concatenated
     * @param maxLength maximum length of the string to return or 0, if no
     * restriction of the length is desired
     * @return the string containing all strings concatenated with "and". If the
     * string is too long it is cut at the maxLength position and "..." is 
     * appended.
     */
    public static String generateConcatenatedString(List<String> strings, int maxLength) {
        /**StringBuilder concatString = new StringBuilder();
        for (String string : strings) {
            concatString = concatString.append(string).append(" and ");
        }
        if (concatString.length() > 5) {
            concatString = concatString.delete(concatString.length() - 5, concatString.length());
        }
        return concatString.toString();
        */
        //Evgeny:
        //generateConcatenatedString is a special case of the implode function, 
        //so i would suggest to use it here to reduce code duplications :
        String concatString = implode(" and ", strings.toArray());
        if (maxLength > 0 && concatString.length() > maxLength) {
            concatString = concatString.substring(0, maxLength).concat("...");
        }
        return concatString;
    }
    
    /**
     * Deletes the given file and if existent also the corresponding ".bai" 
     * index file.
     * @param lastWorkFile the file to delete
     * @return true, if the file could be deleted, false otherwise
     * @throws IOException  
     */
    public static boolean deleteOldWorkFile(File lastWorkFile) throws IOException {
        boolean deleted = false;
        if (lastWorkFile.canWrite()) {
            try {
                Files.delete(lastWorkFile.toPath());
                deleted = true;
                File indexFile = new File(lastWorkFile.getAbsolutePath().concat(Properties.BAM_INDEX_EXT));
                if (indexFile.canWrite()) {
                    Files.delete(indexFile.toPath());
                }
            } catch (IOException ex) {
                throw new IOException(NbBundle.getMessage(GeneralUtils.class, "MSG_GeneralUtils.FileDeletionError", lastWorkFile.getAbsolutePath()));
            }
        }
        return deleted;
    }
    
    /**
    * Joins array elements in a String.
    * @param delim Delimiter between each array element
    * @param array Array of elements
    * @return String
    */
    public static String implode(String delim, Object[] array) {
        String asImplodedString;
        if (array.length == 0) {
            asImplodedString = "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(array[0]);
            for (int i = 1; i < array.length; i++) {
                sb.append(delim);
                sb.append(array[i]);
            }
            asImplodedString = sb.toString();
        }
        return asImplodedString;
    }
    
    /**
    * Joins a map of elements in a String.
    * @param valueDelim Delimiter between key and value of an element
    * @param entryDelim Delimiter between each Entry element
    * @param map a map of elements
    * @return String
    */
    public static String implodeMap(String valueDelim, String entryDelim, Map map) {
        String asImplodedString;
        if ((map == null) || (map.isEmpty())) {
            asImplodedString = "";
        }
        else {
            StringBuilder sb = new StringBuilder();
            Boolean firstLine = true;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                if (!firstLine) { sb.append(entryDelim); }
                Map.Entry line = (Map.Entry) it.next();
                sb.append(line.getKey());
                sb.append(valueDelim);
                sb.append(line.getValue());
                firstLine = false;
            }
            asImplodedString = sb.toString();
        }
        return asImplodedString;
    }
    
    /**
     * format a number to show it to the user
     * @param number
     * @return a good readable string representation of the given number
     */
    public static String formatNumber(Integer number) {
        return NumberFormat.getInstance().format( number );
    }
    
    /**
     * format a number to show it to the user
     * @param number
     * @return a good readable string representation of the given number
     */
    public static String formatNumber(Long number) {
        return NumberFormat.getInstance().format( number );
    }
    
    /**
     * Preliminary method for enshorting an Illumina based read name from single
     * or paired end to a still unique name, which can save memory. 
     * Use with care!
     * @param readName the read name to enshorten
     * @return the short read name, if it was possible to shorten it. Otherwise
     * the original read name is returned
     */
    public static String enshortenReadName(String readName) {
        String shortReadName = readName;
        String[] nameArray;
        if (readName.startsWith("@")) {
            nameArray = readName.split(":");
            if (nameArray.length == 5) {
                shortReadName = nameArray[2] + nameArray[3] + nameArray[4];
                if (shortReadName.contains("#")) {
                    nameArray = shortReadName.split("#");
                    shortReadName = nameArray[0] + nameArray[1].split("/")[1];
                }
            } else if (nameArray.length == 10) {
                shortReadName = nameArray[4] + nameArray[5] + nameArray[6];
            }
        }
        return shortReadName;
    }
    
    public static String escapeHtml(String s) {
        return StringEscapeUtils.escapeHtml3(s);
    }
    
}
