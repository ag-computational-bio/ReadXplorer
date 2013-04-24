package de.cebitec.vamp.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author kstaderm
 */
public class GenerateRowSorter {

    /**
     * Creates a row sorter for the underlying table model and table contents.
     * @param tableContents The contents used for table and table model generation
     * @param tm the table model of the table used to display the tableContents
     * @return row sorter for the table model
     */
    public static synchronized TableRowSorter<DefaultTableModel> createRowSorter(DefaultTableModel tm) {
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tm);
        Vector<Vector> tableContents = tm.getDataVector();
        Vector firstRow = tableContents.get(0);
        int columnCounter = 0;
        for (Iterator it1 = firstRow.iterator(); it1.hasNext(); columnCounter++) {
            Object object = it1.next();
            if (object instanceof Double) {
                rowSorter.setComparator(columnCounter, new Comparator<Double>() {
                    @Override
                    public int compare(Double o1, Double o2) {
                        if (o1.doubleValue() == o2.doubleValue()) {
                            return 0;
                        }
                        if (o1.doubleValue() > o2.doubleValue()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
            }
            if (object instanceof Integer) {
                rowSorter.setComparator(columnCounter, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        if (o1.intValue() == o2.intValue()) {
                            return 0;
                        }
                        if (o1.intValue() > o2.intValue()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
            }

        }
        return rowSorter;
    }
}
