package de.cebitec.vamp.view.tableVisualization.tableFilter;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Interface for filtering values of a table row by a given cutoff
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface FilterValuesI {
    
    /**
     * Filters a table row according to the given cutoff and current entry value.
     * @param filteredTableModel the table model to filter
     * @param row the currently checked row
     * @param cutoff the cutoff value
     * @param currentEntryValue the current entry value from the row
     */
    public void filterTable(DefaultTableModel filteredTableModel, Vector row, double cutoff, double currentEntryValue);
    
}
