package de.cebitec.vamp.view.tableVisualization.tableFilter;

import javax.swing.table.DefaultTableModel;

/**
 * Interface providing functionality for filtering a table.
 *
 * @param <E> some default table model implementation
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public interface FilterI<E extends DefaultTableModel> {
    
    /**
     * Filters the given table model by the given column and filter value.
     * @param tableModel the table model to filter
     * @param column the column to check for the filterValue
     * @param filterValue the filterValue to use for the given column
     * @return the filtered table model
     */
    public E filterTable(E tableModel, int column, Object filterValue);
}
