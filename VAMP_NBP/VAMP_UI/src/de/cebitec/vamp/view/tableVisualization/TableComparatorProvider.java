package de.cebitec.vamp.view.tableVisualization;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.util.Comparator;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Provides different table row comparators for different sorting purposes.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TableComparatorProvider {
    
    
    /**
     * Creates and adds a PersistantTrack comparator to the given sorter and the
     * given table column.
     * @param sorter the table row sorter to which the comparator should be added
     * @param tableColumn the table column for which the comparator should be added
     */
    public static void setPersistantTrackComparator(TableRowSorter<TableModel> sorter, int tableColumn) {
        sorter.setComparator(tableColumn, new Comparator<PersistantTrack>() {
            @Override
            public int compare(PersistantTrack a, PersistantTrack b) {
                return a.toString().compareToIgnoreCase(b.toString());
            }
        });
    }
    
    /**
     * Creates and adds a position string comparator for positions, which might 
     * contain "_*" at the end of the position string.
     * @param sorter the table row sorter to which the comparator should be
     * added
     * @param tableColumn the table column for which the comparator should be
     * added
     */
    public static void setPositionComparator(TableRowSorter<TableModel> sorter, int tableColumn) {
        sorter.setComparator(tableColumn, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                if (a.contains("_")) {
                    a = a.substring(0, a.length() - 2);
                }
                if (b.contains("_")) {
                    b = b.substring(0, b.length() - 2);
                }
                Integer intA = Integer.parseInt(a);
                Integer intB = Integer.parseInt(b);
                return intA.compareTo(intB);
            }
        });
    }
    
    /**
     * Creates a String comparator, that cuts the string after the first line
     * break "\n" and compares it to the second string afterwards as an Integer.
     * If one of the values is not an integer, they are compared as strings.
     * @param sorter the table row sorter to which the comparator should be
     * added
     * @param tableColumn the table column for which the comparator should be
     * added
     */
    public static void setStringComparator(TableRowSorter<TableModel> sorter, int tableColumn) {

        sorter.setComparator(tableColumn, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                if (a.contains("\n")) {
                    a = a.substring(0, a.indexOf('\n'));
                }
                if (b.contains("\n")) {
                    b = b.substring(0, b.indexOf('\n'));
                }
                try {
                    Integer intA = Integer.parseInt(a);
                    Integer intB = Integer.parseInt(b);
                    return intA.compareTo(intB);
                } catch (NumberFormatException e) {
                    return a.compareTo(b);
                }
            }
        });
    }
}
