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
     * @param tm the table model of the table used to display the tableContents
     * @return row sorter for the table model
     */
    public static synchronized TableRowSorter<DefaultTableModel> createRowSorter(DefaultTableModel tm) {
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tm);
        if (tm.getRowCount() > 1) {
            Vector<Vector> tableContents = tm.getDataVector();
            Vector firstRow = tableContents.get(0);
            int columnCounter = 0;
            for (Iterator it1 = firstRow.iterator(); it1.hasNext(); columnCounter++) {
                Object object = it1.next();
                if (object instanceof Double) {
                    rowSorter.setComparator(columnCounter, new Comparator<Double>() {
                        @Override
                        public int compare(Double o1, Double o2) {
                            return o1.compareTo(o2);
                        }
                    });
                }
                if (object instanceof Integer) {
                    rowSorter.setComparator(columnCounter, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            return o1.compareTo(o2);
                        }
                    });
                }

            }
        }
        return rowSorter;
    }
}
