package de.cebitec.vamp.view.tableVisualization.tableFilter;

import javax.swing.table.DefaultTableModel;
import org.openide.util.Exceptions;

/**
 * Provides some standard table filter util methods.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TableFilterUtils<E extends DefaultTableModel> {
    
    private final Class<E> classType;

    public TableFilterUtils(Class<E> classType) {
        this.classType = classType;
    }
    
    /**
     * Prepares a new table model of the same type and with the same headers,
     * as the given model.
     * @param tableModel the tabel model to recreate with headers, but without 
     * content.
     * @return the new table model
     */
    public E prepareNewTableModel(E tableModel) {
        E newTableModel = null;
        try {
            newTableModel = classType.newInstance();
            String[] columnNames = new String[tableModel.getColumnCount()];
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                columnNames[i] = tableModel.getColumnName(i);
            }
            newTableModel.setColumnIdentifiers(columnNames);
        } catch (InstantiationException | IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        return newTableModel;
    }
}
