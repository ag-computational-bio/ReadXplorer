/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.utils;


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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openide.util.NbBundle;


/**
 * Contains general use utilities.
 * <p>
 * @author -Rolf Hilker-
 */
public final class GeneralUtils {

    /**
     * Calculates the percentage increase of value 1 to value 2. In case value1
     * is 0,
     * the percentage is set to 1.5 times the absolute difference as a weight
     * factor.
     * <p>
     * @param value1 smaller value
     * @param value2 larger value
     * <p>
     * @return the percentage increase
     */
    public static int calculatePercentageIncrease( final int value1, final int value2 ) {

        if( value1 == 0 ) {
            int absoluteDiff = value2 - value1;
            return (int) (absoluteDiff * 1.5d); //weight factor
        }
        else {
            return (int) Math.ceil( ((double) value2 / value1) * 100d ) - 100;
        }
    }


    /**
     * @param parent the parent component
     * <p>
     * @return Any text found in the clipboard. If none is found, an empty
     *         String is returned.
     */
    public static String getClipboardContents( Component parent ) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String result = "";
        Transferable contents = clipboard.getContents( null );
        final boolean hasTransferableText = (contents != null)
                                            && contents.isDataFlavorSupported( DataFlavor.stringFlavor );
        if( hasTransferableText ) {
            try {
                result = (String) contents.getTransferData( DataFlavor.stringFlavor );
            }
            catch( UnsupportedFlavorException ex ) {
                JOptionPane.showMessageDialog( parent, "Unsupported DataFlavor for clipboard copying.", "Paste Error", JOptionPane.ERROR_MESSAGE );
            }
            catch( IOException ex ) {
                JOptionPane.showMessageDialog( parent, "IOException occured during recovering of text from clipboard.", "Paste Error", JOptionPane.ERROR_MESSAGE );
            }
        }
        return result;
    }


    /**
     * Checks if the input string is a valid number larger than 0.
     * <p>
     * @param input input string to check
     * <p>
     * @return <code>true</code> if it is a valid input string,
     *         <code>false</code> otherwise
     */
    public static boolean isValidPositiveNumberInput( String input ) {
        try {
            return Integer.parseInt( input ) > 0;
        }
        catch( NumberFormatException e ) {
            return false;
        }
    }


    /**
     * Checks if the input string is a valid number larger than or equal to 0.
     * <p>
     * @param input input string to check
     * <p>
     * @return <code>true</code> if it is a valid input
     *         string, <code>false</code> otherwise
     */
    public static boolean isValidNumberInput( String input ) {
        try {
            return Integer.parseInt( input ) >= 0;
        }
        catch( NumberFormatException e ) {
            return false;
        }
    }


    /**
     * Checks if the input string is a valid position number between 1 and the
     * given maximum position.
     * <p>
     * @param input input string to check
     * @param max   maximum position value for the input
     * <p>
     * @return <code>true</code> if it is a valid input string,
     *         <code>false</code> otherwise
     */
    public static boolean isValidPositionInput( String input, int max ) {
        return GeneralUtils.isValidRangeInput( input, 1, max );
    }


    /**
     * Checks if the input string is a valid number in the range of the given
     * interval.
     * <p>
     * @param input input string to check
     * @param min   minimum position value for the input
     * @param max   maximum position value for the input
     * <p>
     * @return <code>true</code> if it is a valid input string,
     *         <code>false</code> otherwise
     */
    public static boolean isValidRangeInput( final String input, final int min, final int max ) {
        try {
            int tmp = Integer.parseInt( input );
            return tmp >= min  &&  tmp <= max;
        }
        catch( NumberFormatException e ) {
            return false;
        }
    }


    /**
     * Checks if the input string is a valid byte larger than or equal to 0.
     * <p>
     * @param text input string to check
     * <p>
     * @return <code>true</code> if it is a valid input string,
     *         <code>false</code> otherwise
     */
    public static boolean isValidByteInput( final String text ) {
        try {
            return Byte.parseByte( text ) >= 0;
        }
        catch( NumberFormatException e ) {
            return false;
        }
    }


    /**
     * Checks if the input string is a valid number between 1 and 100, so a
     * valid
     * percentage value.
     * <p>
     * @param input input string to check
     * <p>
     * @return <code>true</code> if it is a valid percentage value,
     *         <code>false</code> otherwise
     */
    public static boolean isValidPercentage( final String input ) {
        if( GeneralUtils.isValidPositiveNumberInput( input ) ) {
            int value = Integer.valueOf( input );
            if( value <= 100 ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Calculates the given time as 3 entries in an array list:
     * 0 = hours, 1 = minutes, 2 = seconds.
     * <p>
     * @param timeInMillis given time in milliseconds
     * <p>
     * @return time as hours, minutes and seconds
     */
    public static List<Integer> getTime( final long timeInMillis ) {
        List<Integer> timeList = new ArrayList<>( 3 );
        int remdr = (int) (timeInMillis % (24L * 60 * 60 * 1000));

        final int hours = remdr / (60 * 60 * 1000);

        remdr %= 60 * 60 * 1000;

        final int minutes = remdr / (60 * 1000);

        remdr %= 60 * 1000;

        final int seconds = remdr / 1000;
        timeList.add( 0, hours );
        timeList.add( 1, minutes );
        timeList.add( 2, seconds );

        return timeList;
    }


    /**
     * Generates a string, which concatenates the list of strings for user
     * friendly displaying in the gui with an " and ".
     * <p>
     * @param strings   the list of strings, which should be concatenated
     * @param maxLength maximum length of the string to return or 0, if no
     *                  restriction of the length is desired
     * <p>
     * @return the string containing all strings concatenated with "and". If the
     *         string is too long it is cut at the maxLength position and "..." is
     *         appended.
     */
    public static String generateConcatenatedString( final List<String> strings, final int maxLength ) {
        String concatString = implode( " and ", strings.toArray() );
        if( maxLength > 0 && concatString.length() > maxLength ) {
            concatString = concatString.substring( 0, maxLength ).concat( "..." );
        }
        return concatString;
    }


    /**
     * Deletes the given file and if existent also the corresponding ".bai"
     * index file.
     * <p>
     * @param lastWorkFile the file to delete
     * <p>
     * @return true, if the file could be deleted, false otherwise
     * <p>
     * @throws IOException
     */
    public static boolean deleteOldWorkFile( File lastWorkFile ) throws IOException {
        boolean deleted = false;
        if( lastWorkFile.canWrite() ) {
            try {
                Files.delete( lastWorkFile.toPath() );
                deleted = true;
                File indexFile = new File( lastWorkFile.getAbsolutePath().concat( Properties.BAM_INDEX_EXT ) );
                if( indexFile.canWrite() ) {
                    Files.delete( indexFile.toPath() );
                }
            }
            catch( IOException ex ) {
                throw new IOException( NbBundle.getMessage( GeneralUtils.class, "MSG_GeneralUtils.FileDeletionError", lastWorkFile.getAbsolutePath() ) );
            }
        }
        return deleted;
    }


    /**
     * Joins array elements in a String.
     * <p>
     * @param delim Delimiter between each array element
     * @param array Array of elements
     * <p>
     * @return String
     */
    public static String implode( final String delim, final Object[] array ) {

        if( array.length == 0 ) {
            return "";
        }
        else {
            StringBuilder sb = new StringBuilder( array.length * 20 );
            sb.append( array[0] );
            for( Object obj : array ) {
                sb.append( delim );
                sb.append( obj );
            }
            return sb.toString();
        }

    }


    /**
     * Joins a map of elements in a String.
     * <p>
     * @param valueDelim Delimiter between key and value of an element
     * @param entryDelim Delimiter between each Entry element
     * @param map        a map of elements
     * <p>
     * @return String
     */
    public static String implodeMap( final String valueDelim, final String entryDelim, final Map<?, ?> map ) {

        if( (map == null) || (map.isEmpty()) ) {
            return "";
        }
        else {
            StringBuilder sb = new StringBuilder( map.size() * 30 );
            Boolean firstLine = true;
            for( Map.Entry<?,?> line : map.entrySet() ) {
                if( !firstLine ) {
                    sb.append( entryDelim );
                }
                sb.append( line.getKey() );
                sb.append( valueDelim );
                sb.append( line.getValue() );
                firstLine = false;
            }
            return sb.toString();
        }

    }


    /**
     * Converts a given number into a number of the given classType. If this is
     * not possible, it throws a ClassCastException
     * <p>
     * @param <T>       one of the classes derived from Number
     * @param classType the type to convert the number into
     * @param number    the number to convert
     * <p>
     * @return The converted number
     */
    public static <T extends Number> T convertNumber( Class<T> classType, Number number ) throws ClassCastException {
        T convertedValue = null;
        if( classType.equals( Integer.class ) ) {
            convertedValue = classType.cast( number.intValue() );
        }
        else if( classType.equals( Double.class ) ) {
            convertedValue = classType.cast( number.doubleValue() );
        }
        else if( classType.equals( Long.class ) ) {
            convertedValue = classType.cast( number.longValue() );
        }
        else if( classType.equals( Float.class ) ) {
            convertedValue = classType.cast( number.floatValue() );
        }
        else if( classType.equals( Short.class ) ) {
            convertedValue = classType.cast( number.shortValue() );
        }
        else if( classType.equals( Byte.class ) ) {
            convertedValue = classType.cast( number.byteValue() );
        }

        if( convertedValue == null ) {
            throw new ClassCastException( "Cannot cast the given number into the given format." );
        }

        return convertedValue;
    }


    /**
     * format a number to show it to the user
     * <p>
     * @param number
     *               <p>
     * @return a good readable string representation of the given number
     */
    public static String formatNumber( Number number ) {
        return NumberFormat.getInstance().format( number );
    }


    /**
     * @param number A number to convert into a percent value
     * <p>
     * @return The percent representation of the given value in the format of
     *         the Java virtual machine's Locale.
     */
    public static String formatNumberAsPercent( Number number ) {
        Locale locale = Locale.getDefault();
        NumberFormat percentFormatter = NumberFormat.getPercentInstance( locale );
        percentFormatter.setMaximumFractionDigits( 2 );
        return percentFormatter.format( number );
    }


    /**
     * Preliminary method for enshorting an Illumina based read name from single
     * or paired end to a still unique name, which can save memory.
     * Use with care!
     * <p>
     * @param readName the read name to enshorten
     * <p>
     * @return the short read name, if it was possible to shorten it. Otherwise
     *         the original read name is returned
     */
    public static String enshortenReadName( String readName ) {
        String shortReadName = readName;
        if( readName.startsWith( "@" ) ) {
            String[] nameArray = readName.split( ":" );
            if( nameArray.length == 5 ) {
                shortReadName = nameArray[2] + nameArray[3] + nameArray[4];
                if( shortReadName.contains( "#" ) ) {
                    nameArray = shortReadName.split( "#" );
                    shortReadName = nameArray[0] + nameArray[1].split( "/" )[1];
                }
            }
            else if( nameArray.length == 10 ) {
                shortReadName = nameArray[4] + nameArray[5] + nameArray[6];
            }
        }
        return shortReadName;
    }


    public static String escapeHtml( String s ) {
        return StringEscapeUtils.escapeHtml3( s );
    }


    /**
     * Enumeration of read name styles.
     */
    public static enum NameStyle {

        /**
         * Style useable for all reads.
         */
        STYLE_STANDARD,
        /**
         * Style useable for Illumina reads.
         */
        STYLE_ILLUMINA;

    }


    /**
     * Splits the given readname by the given style.
     * <p>
     * @param readName     The readname to split
     * @param specialStyle The style to use for splitting
     * <p>
     * @return The splitted read name array
     */
    public static String[] splitReadName( String readName, NameStyle specialStyle ) {

        if( specialStyle == NameStyle.STYLE_ILLUMINA ) {
            return readName.split( ":|#" );
        }
        else {
            int length = readName.length() / 5 + 1;
            String[] nameArray = new String[length];
            for( int i = 0; i < length; i++ ) {
                int index = i * 5;
                int end = index + 5;
                if( end < readName.length() )
                    nameArray[i] = readName.substring( index, end );
                else
                    nameArray[i] = readName.substring( index, readName.length() );
            }
            return nameArray;
        }

    }

//    /**
//     * For a given map of strings to other maps or objects, this method
//     * recursively adds or creates a mapping of the input array to the value to
//     * store. The depth of the HashMaps is dependent on the elements in the input
//     * array. The value to store is stored in the deepest level (leaf), which is
//     * hashed to the last element of the input array.<br>E.g. the read name arrays:<br>
//     * {HWI-ST486_0090, 5, 1101, 17454, 23711, ACTTGA/1}<br>
//     * {HWI-ST486_0090, 5, 1101, 17454, 23712, ACTTGA/1}<br>
//     * is transformed into a mapping of:<br>
//     * {HWI-ST486_0090{5, {1101, {17454, {23711, {ACTTGA/1, valueToStore}, {23712, {ACTTGA/1, valueToStore}}}}}}}<br>
//     * This method helps to significantly reduce the memory footprint for a huge amount of partially identical Strings.
//     * @param map The map (in)to which the new input shall be associated/integrated
//     * @param input The input array of Strings to integrate in the map
//     * @param valueToStore The value to store for the last element of the input
//     * array
//     * @return The updated map
//     */
//    public static HashMap<String, Object> generateStringMap(HashMap<String, Object> map, String[] input, Object valueToStore) {
//        if (input.length > 1) {
//            String key = input[0];
//            String[] next = new String[input.length - 1];
//            System.arraycopy(input, 1, next, 0, input.length - 1);
//            if (!map.containsKey(key)) {
//                map.put(key, GeneralUtils.generateStringMap(new HashMap<String, Object>(), next, valueToStore));
//            } else {
//                Object value = map.get(key);
//                if (value instanceof HashMap) {
//                    @SuppressWarnings("unchecked")
//                    HashMap<String, Object> subMap = (HashMap<String, Object>) value;
//                    GeneralUtils.generateStringMap(subMap, next, valueToStore);
//                } else {
//
//                }
//            }
//        } else if (input.length == 1) {
//            map.put(input[0], valueToStore);
//        }
//        return map;
//    }
//
//    /**
//     * Convenience method for first splitting a read name and then storing it
//     * and the given valueToStore in the given map.
//     * @param map The map (in)to which the new input shall be associated/integrated
//     * @param readName The readname to split and store
//     * @param valueToStore The value to store for the last element of the
//     * splitted read name array
//     * @param style The style to use for splitting
//     */
//    public static void splitReadNameAndAddToMap(HashMap<String, Object> map, String readName, Object valueToStore, NameStyle style) {
//        String[] splittedName = GeneralUtils.splitReadName(readName, style);
//        GeneralUtils.generateStringMap(map, splittedName, valueToStore);
//    }

}
