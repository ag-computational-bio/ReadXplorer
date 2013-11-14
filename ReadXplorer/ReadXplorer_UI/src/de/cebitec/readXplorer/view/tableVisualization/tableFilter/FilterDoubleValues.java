package de.cebitec.readXplorer.view.tableVisualization.tableFilter;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Class for filtering integer or double value entries from a table.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilterDoubleValues<T extends DefaultTableModel> implements FilterI<T> {
    
    private final FilterValuesI valueFilter;
    private final Class<T> classType;

    public FilterDoubleValues(FilterValuesI valueFilter, Class<T> classType) {
        this.valueFilter = valueFilter;
        this.classType = classType;
    }
    
    /**
     * Filters values which are lower than a given cutoff from the given column
     * of the table.
     * @param tableModel the table whose content shall be filtered
     * @param column the column, which shall be filtered
     * @param filterValue the minimum double value in the given column to keep
     * the current row in the table
     * @return the filtered table model only containing rows whose values in the
     * selected column exceed the given double filterValue
     */
    @Override
    @SuppressWarnings("unchecked")
    public T filterTable(T tableModel, int column, Object filterValue) {
        double cutoff = 0;
        if (filterValue instanceof Double) {
            cutoff = (double) filterValue;
        }
        TableFilterUtils<T> utils = new TableFilterUtils<>(classType);
        T filteredTableModel = utils.prepareNewTableModel(tableModel);
        Vector dataVector = tableModel.getDataVector();
        for (Iterator<Vector> it = dataVector.iterator(); it.hasNext();) {
            Vector row = it.next();
            Object currentEntry = row.get(column);
            if (currentEntry instanceof Integer) {
                int intValue = (int) currentEntry;
                valueFilter.filterTable(filteredTableModel, row, cutoff, (double) intValue);
            }
            if (currentEntry instanceof Double) {
                valueFilter.filterTable(filteredTableModel, row, cutoff, (double) currentEntry);
            }
        }
        if (filteredTableModel.getRowCount() < 1) {
            filteredTableModel.addRow(new Vector());
        }
        return filteredTableModel;
    }
}
