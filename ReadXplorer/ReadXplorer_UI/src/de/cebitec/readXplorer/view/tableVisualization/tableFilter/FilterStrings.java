package de.cebitec.readXplorer.view.tableVisualization.tableFilter;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Class for filtering a table for a given pattern/string.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class FilterStrings<T extends DefaultTableModel> implements FilterI<T>{
    
    private final Class<T> classType;

    public FilterStrings(Class<T> classType) {
        this.classType = classType;
    }
    
    /**
     * Only keeps entries in the table, which contain the given filterValue
     * in the given column.
     * @param tableModel the table model to filter
     * @param column the column to filter
     * @param filterValue the value to search for
     * @return The new table only containing rows, which contain the given
     * filterValue in the given column.
     */
    @Override
    public T filterTable(T tableModel, int column, Object filterValue) {
        String pattern = filterValue.toString();
        TableFilterUtils<T> utils = new TableFilterUtils(classType);
        T filteredTableModel = utils.prepareNewTableModel(tableModel);
        Vector dataVector = tableModel.getDataVector();
        for (Iterator<Vector> it = dataVector.iterator(); it.hasNext();) {
            Vector row = it.next();
            if (((String) row.get(column)).contains(pattern)) {
                filteredTableModel.addRow(row);
            }
        }
        if (filteredTableModel.getRowCount() < 1) {
            filteredTableModel.addRow(new Vector());
        }
        return filteredTableModel;
    }
    
}
