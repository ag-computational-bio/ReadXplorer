package de.cebitec.readXplorer.view.tableVisualization.tableFilter;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Class for filtering values which are larger than a given cutoff.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilterLargeValues implements FilterValuesI {
        
    /**
         * Filters values which are larger than a given cutoff from the given
         * column of the table.
         * @param tableModel the table whose content shall be filtered
         * @param column the column, which shall be filtered
         * @param filterValue the minimum double value in the given column to
         * keep the current row in the table
         * @return the filtered table model only containing rows whose values in
         * the selected column exceed the given double filterValue
         */
        @Override
        public void filterTable
        (DefaultTableModel filteredTableModel, Vector row, double cutoff, double currentEntryValue) {
        if (currentEntryValue <= cutoff) {
                filteredTableModel.addRow(row);
            }
        }
    
}
