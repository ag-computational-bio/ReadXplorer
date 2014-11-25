package de.cebitec.readXplorer.util;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Default table model, which does not allow editing of any table cell.
 * 
 * @author kstaderm
 */
public class UneditableTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * Default table model, which does not allow editing of any table cell.
     */
    public UneditableTableModel() {
        super();
    }

    public UneditableTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public UneditableTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public UneditableTableModel(Vector data, Vector columnNames) {
        super(data, columnNames);
    }

    public UneditableTableModel(Vector columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public UneditableTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
